package com.foodgpt.composition

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Inférence locale Gemma via **LiteRT-LM** ([litertlm-android](https://github.com/google-ai-edge/LiteRT-LM)).
 * Le modèle doit être au format `.litertlm` dans `assets/gemma/` (voir [GemmaModelPaths]).
 *
 * Un [Engine] **initialisé** est conservé entre les analyses (même fichier modèle) pour éviter
 * un cold `initialize()` à chaque capture. Chaque requête crée une **nouvelle** conversation
 * (pas de fuite de contexte entre OCR). Les accès au moteur sont **sérialisés** (un seul
 * inférence à la fois). En cas de **timeout**, le moteur mis en cache est relâché car
 * l’état JNI peut être incertain.
 *
 * La sortie modèle peut être lue en **streaming** ([Conversation.sendMessageAsync]) lorsque
 * [CompositionAnalysisEngine.analyze] fournit [onStreamPartial]. En cas d’[IllegalStateException]
 * (cycle de vie conversation / backend), **repli automatique** : nouvelle conversation + [sendMessage] synchrone.
 *
 * Ordre des backends : **NPU**, **GPU**, **CPU** (aligné sur le code actuel du projet).
 */
class LiteRtGemmaEngine(
    private val context: Context,
    private val locator: GemmaModelLocator
) : CompositionAnalysisEngine {

    private val inferenceLock = Any()
    private var retainedEngine: Engine? = null
    private var retainedModelAbsolutePath: String? = null

    override suspend fun analyze(
        rawText: String,
        maxInferenceMs: Long,
        onStreamPartial: ((String) -> Unit)?
    ): AnalyzeCompositionResult =
        withContext(Dispatchers.Default) {
            if (rawText.isBlank()) {
                return@withContext AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
            }
            when (val located = locator.resolve()) {
                GemmaModelLocation.NotFound -> AnalyzeCompositionResult.GemmaError(
                    GemmaErrorCode.GEMMA_NOT_FOUND,
                    CompositionMessages.GEMMA_NOT_FOUND_USER
                )
                is GemmaModelLocation.LoadFailed -> AnalyzeCompositionResult.GemmaError(
                    GemmaErrorCode.GEMMA_LOAD_FAILED,
                    CompositionMessages.GEMMA_LOAD_FAILED_USER
                )
                is GemmaModelLocation.Ready ->
                    runLitertLmWithDeadline(located.modelFile, rawText, maxInferenceMs, onStreamPartial)
            }
        }

    /**
     * L’appel LiteRT/JNI est bloquant : [kotlinx.coroutines.withTimeout] ne l’interrompt pas.
     * On borne donc l’attente avec [CompletableFuture.get] pour que l’UI ne reste pas figée.
     */
    private suspend fun runLitertLmWithDeadline(
        modelFile: File,
        rawText: String,
        maxInferenceMs: Long,
        onStreamPartial: ((String) -> Unit)?
    ): AnalyzeCompositionResult {
        val deadlineMs = maxInferenceMs.coerceAtLeast(1L)
        val abandonRetainedAfterRun = AtomicBoolean(false)
        return withContext(Dispatchers.IO) {
            val future = CompletableFuture.supplyAsync(
                {
                    synchronized(inferenceLock) {
                        try {
                            runLitertLm(modelFile, rawText, onStreamPartial)
                        } finally {
                            if (abandonRetainedAfterRun.get()) {
                                disposeRetainedLocked()
                            }
                        }
                    }
                },
                inferenceExecutor
            )
            try {
                future.get(deadlineMs, TimeUnit.MILLISECONDS)
            } catch (_: TimeoutException) {
                abandonRetainedAfterRun.set(true)
                future.cancel(true)
                AnalyzeCompositionResult.GemmaError(
                    GemmaErrorCode.GEMMA_TIMEOUT,
                    CompositionMessages.GEMMA_TIMEOUT_USER
                )
            } catch (e: ExecutionException) {
                val cause = e.cause
                if (cause is Exception) {
                    AnalyzeCompositionResult.GemmaError(
                        GemmaErrorCode.GEMMA_LOAD_FAILED,
                        "${CompositionMessages.GEMMA_LOAD_FAILED_USER} (${cause.javaClass.simpleName})"
                    )
                } else {
                    AnalyzeCompositionResult.GemmaError(
                        GemmaErrorCode.GEMMA_LOAD_FAILED,
                        CompositionMessages.GEMMA_LOAD_FAILED_USER
                    )
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                abandonRetainedAfterRun.set(true)
                future.cancel(true)
                AnalyzeCompositionResult.GemmaError(
                    GemmaErrorCode.GEMMA_TIMEOUT,
                    CompositionMessages.GEMMA_TIMEOUT_USER
                )
            }
        }
    }

    private fun disposeRetainedLocked() {
        retainedEngine?.close()
        retainedEngine = null
        retainedModelAbsolutePath = null
    }

    private fun litertLmBackendChain(): List<Backend> {
        val npuDir = context.applicationInfo.nativeLibraryDir
        return buildList {
            add(Backend.NPU(nativeLibraryDir = npuDir))
            add(Backend.GPU())
            add(Backend.CPU())
        }
    }

    /**
     * Réutilise [retainedEngine] si le chemin modèle correspond ; sinon parcourt les backends.
     * Toujours appelé sous [inferenceLock].
     */
    private fun runLitertLm(
        modelFile: File,
        rawText: String,
        onStreamPartial: ((String) -> Unit)?
    ): AnalyzeCompositionResult {
        val path = modelFile.absolutePath
        if (retainedModelAbsolutePath != null && retainedModelAbsolutePath != path) {
            disposeRetainedLocked()
        }
        val warm = retainedEngine
        if (warm != null && retainedModelAbsolutePath == path) {
            try {
                return runInferenceOnEngine(warm, rawText, onStreamPartial)
            } catch (_: Exception) {
                disposeRetainedLocked()
            }
        }
        var lastGemmaError: AnalyzeCompositionResult.GemmaError? = null
        for (backend in litertLmBackendChain()) {
            when (val result = tryLoadAndInfer(modelFile, backend, rawText, onStreamPartial)) {
                is AnalyzeCompositionResult.BilanSuccess -> return result
                is AnalyzeCompositionResult.CompositionLimit -> return result
                is AnalyzeCompositionResult.GemmaError -> lastGemmaError = result
            }
        }
        return lastGemmaError ?: AnalyzeCompositionResult.GemmaError(
            GemmaErrorCode.GEMMA_LOAD_FAILED,
            CompositionMessages.GEMMA_LOAD_FAILED_USER
        )
    }

    /**
     * Crée un moteur, [Engine.initialize], infère ; en cas de succès (bilan ou limite métier),
     * remplace le cache sans fermer ce moteur.
     */
    private fun tryLoadAndInfer(
        modelFile: File,
        backend: Backend,
        rawText: String,
        onStreamPartial: ((String) -> Unit)?
    ): AnalyzeCompositionResult {
        val engineConfig = EngineConfig(
            modelPath = modelFile.absolutePath,
            backend = backend,
            cacheDir = context.cacheDir.absolutePath
        )
        val engine = Engine(engineConfig)
        return try {
            engine.initialize()
            when (val result = runInferenceOnEngine(engine, rawText, onStreamPartial)) {
                is AnalyzeCompositionResult.BilanSuccess,
                is AnalyzeCompositionResult.CompositionLimit -> {
                    disposeRetainedLocked()
                    retainedEngine = engine
                    retainedModelAbsolutePath = modelFile.absolutePath
                    result
                }
                is AnalyzeCompositionResult.GemmaError -> {
                    engine.close()
                    result
                }
            }
        } catch (e: Exception) {
            engine.close()
            AnalyzeCompositionResult.GemmaError(
                GemmaErrorCode.GEMMA_LOAD_FAILED,
                "${CompositionMessages.GEMMA_LOAD_FAILED_USER} (${e.javaClass.simpleName})"
            )
        }
    }

    private fun runInferenceOnEngine(
        engine: Engine,
        rawText: String,
        onStreamPartial: ((String) -> Unit)?
    ): AnalyzeCompositionResult {
        val systemInstruction = buildString {
            appendLine("Tu analyses des listes d'ingrédients alimentaires (contexte UE, français).")
            appendLine("Tu ne dois pas inventer d'ingrédients absents du texte source.")
            appendLine("Réponds uniquement avec la structure suivante (marqueurs exacts) :")
            appendLine("###LISTE")
            appendLine("- ingrédient 1")
            appendLine("- ingrédient 2")
            appendLine("###ANALYSE")
            appendLine(
                "Sous ###ANALYSE : au plus 3 phrases courtes (≈80 mots max), factuelles et prudentes ; " +
                    "nuancer si doute. Pas d'introduction hors marqueurs."
            )
            appendLine("###ADDITIFS_RISQUE")
            appendLine(
                "Sous ###ADDITIFS_RISQUE : une ligne par additif pertinent, format exact NIVEAU|nom_additif|justification_courte " +
                    "(une seule ligne par additif ; pas de texte libre hors lignes). " +
                    "NIVEAU ∈ {VERT, ORANGE, ROUGE, INCERTAIN}."
            )
            appendLine("###IMPACT_SANTE")
            appendLine(
                "Sous ###IMPACT_SANTE : une ligne par ingrédient de ###LISTE, format exact NIVEAU|nom_ingredient|note_courte " +
                    "(une seule phrase, ≤15 mots). NIVEAU ∈ {VERT, ORANGE, ROUGE, INCERTAIN}."
            )
            appendLine("Aucun texte avant la ligne ###LISTE.")
        }
        val conversationConfig = ConversationConfig(
            systemInstruction = Contents.of(systemInstruction)
        )
        val userMessage = buildString {
            appendLine("Texte capturé (OCR) :")
            appendLine(rawText.trim().take(12_000))
        }
        val text = try {
            openConversationAndReadAssistantText(
                engine,
                conversationConfig,
                userMessage,
                stream = onStreamPartial != null,
                onStreamPartial
            )
        } catch (e: IllegalStateException) {
            if (onStreamPartial != null) {
                openConversationAndReadAssistantText(
                    engine,
                    conversationConfig,
                    userMessage,
                    stream = false,
                    onStreamPartial
                )
            } else {
                throw e
            }
        }.trim()
        return when {
            text.isEmpty() ->
                AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
            else -> {
                val bilan = GemmaBilanParser.parse(text)
                if (bilan == null) {
                    AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
                } else {
                    AnalyzeCompositionResult.BilanSuccess(bilan)
                }
            }
        }
    }

    /**
     * Une conversation par appel ; [stream] utilise [sendMessageAsync] (fragile selon backend / build),
     * sinon [sendMessage] synchrone. En mode non-stream avec [onStreamPartial] non nul, un seul rappel
     * à la fin avec le texte complet.
     */
    private fun openConversationAndReadAssistantText(
        engine: Engine,
        conversationConfig: ConversationConfig,
        userMessage: String,
        stream: Boolean,
        onStreamPartial: ((String) -> Unit)?
    ): String {
        return engine.createConversation(conversationConfig).use { conversation ->
            when {
                stream && onStreamPartial != null ->
                    collectAssistantTextStreaming(conversation, userMessage, onStreamPartial)
                onStreamPartial != null -> {
                    val t = conversation.sendMessage(userMessage).let(::textFromMessage)
                    onStreamPartial.invoke(t)
                    t
                }
                else -> conversation.sendMessage(userMessage).let(::textFromMessage)
            }
        }
    }

    /**
     * Agrège les morceaux du flux (préfixe cumulatif ou deltas) pour reconstruire le texte final.
     */
    private fun collectAssistantTextStreaming(
        conversation: Conversation,
        userMessage: String,
        onStreamPartial: (String) -> Unit
    ): String {
        var reconciled = ""
        runBlocking {
            conversation.sendMessageAsync(userMessage)
                .catch { throw it }
                .collect { chunk ->
                    val piece = textFromMessage(chunk)
                    if (piece.isEmpty()) return@collect
                    reconciled = if (piece.startsWith(reconciled)) piece else reconciled + piece
                    onStreamPartial(reconciled)
                }
        }
        return reconciled
    }

    private fun textFromMessage(message: Message): String =
        message.contents.contents
            .asSequence()
            .filterIsInstance<Content.Text>()
            .map { it.text }
            .joinToString("\n")

    companion object {
        private val inferenceExecutor: ExecutorService = Executors.newCachedThreadPool(
            object : ThreadFactory {
                private val seq = AtomicInteger(0)
                override fun newThread(r: Runnable): Thread {
                    val t = Thread(r, "foodgpt-gemma-${seq.incrementAndGet()}")
                    t.isDaemon = true
                    return t
                }
            }
        )
    }
}
