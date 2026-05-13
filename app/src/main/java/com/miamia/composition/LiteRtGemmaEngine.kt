package com.miamia.composition

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
            appendLine("Rédige toute ta réponse en français.")
            appendLine("Tu ne dois pas inventer d'ingrédients absents du texte source.")
            appendLine(
                "Pour ###LISTE : un ingrédient par ligne avec - ; à partir du texte OCR, " +
                    "reformule chaque libellé vers la graphie / dénomination **la plus probable** " +
                    "(orthographe correcte, accents, formulation UE courante) tout en restant fidèle au contenu " +
                    "réellement lu (même ordre logique que sur l’étiquette ; ne pas fusionner ni scinder des entrées distinctes). " +
                    "**N’inclus pas** les pourcentages seuls entre parenthèses issus de l’étiquette dans le libellé " +
                    "(ex. écrire « Farine de blé », pas « Farine de blé (50,2 %) ») ; le champ **nom** des verdicts suit la même règle. " +
                    "Corrige les fautes OCR évidentes sur les huiles (ex. **polmiste** → **palmiste**). " +
                    "Si l’étiquette regroupe **farine(s) de X et de Y** sur une seule ligne, produis **deux** lignes `- farine de X` et `- farine de Y`. " +
                    "Ne laisse pas de parenthèse fermante seule en fin de libellé (ex. `colza)` → `colza`)."
            )
            appendLine("Réponds uniquement avec les 6 sections ci-dessous, dans cet ordre exact.")
            appendLine("Aucun texte avant ###LISTE.")
            appendLine()
            appendLine("Exemple de réponse attendue :")
            appendLine("###LISTE")
            appendLine("- eau")
            appendLine("- sucre")
            appendLine("- acide citrique")
            appendLine("- E300")
            appendLine("###PRODUIT")
            appendLine("Limonade ou soda sucré|80")
            appendLine("###ANALYSE")
            appendLine("Produit simple. Le sucre est l'ingrédient principal après l'eau. Peu d'additifs.")
            appendLine("###ENERGIE_ESTIMEE")
            appendLine("38")
            appendLine("###ADDITIFS_RISQUE")
            appendLine("VERT|E300|Vitamine C, antioxydant naturel courant")
            appendLine("###IMPACT_SANTE")
            appendLine("VERT|eau|Essentiel à l'hydratation")
            appendLine("ORANGE|sucre|Consommation excessive liée au surpoids")
            appendLine("VERT|E300|Vitamine C sans risque aux doses alimentaires")
            appendLine()
            appendLine("Règles :")
            appendLine(
                "- ###LISTE : une entrée par ligne avec - ; appliquer la reformulation « la plus probable » " +
                    "décrite plus haut, sans inventer d'ingrédient."
            )
            appendLine(
                "- ###ADDITIFS_RISQUE : une ligne par additif, format VERT|nom|raison ou ORANGE|nom|raison ou ROUGE|nom|raison ou INCERTAIN|nom|raison ; " +
                    "le champ **nom** doit reprendre le **même intitulé normalisé** que dans ###LISTE pour ce composé."
            )
            appendLine("- ###PRODUIT : une seule ligne, format nom_du_produit|pourcentage_certitude (0–100). Le produit alimentaire le plus probable auquel ces ingrédients appartiennent (ex. « Yaourt aux fruits|85 », « Biscuit fourré chocolat|60 »).")
            appendLine("- ###ANALYSE : 3 phrases max, factuelles, prudentes")
            appendLine(
                "- ###ENERGIE_ESTIMEE : **une seule ligne** avec l'estimation entière des **kcal pour 100 g** " +
                    "dérivée **uniquement** de la composition (liste + produit identifié si présent), **à titre indicatif** ; " +
                    "pas de décimales ; si tu ne peux pas estimer de façon raisonnable, écris exactement `NA` sur cette ligne."
            )
            appendLine(
                "- ###IMPACT_SANTE : une ligne par ingrédient, même format (VERT ou ORANGE ou ROUGE ou INCERTAIN puis | puis nom puis | puis note courte) ; " +
                    "le champ **nom** doit être le **même intitulé normalisé** que dans ###LISTE pour cet ingrédient. " +
                    "**Aucune omission** : une ligne de verdict pour **chaque** entrée de ###LISTE, dans le **même ordre**."
            )
            appendLine(
                "Erreurs OCR fréquentes (liste UE) : par ex. **omidon** → **amidon** ; applique la même correction dans ###IMPACT_SANTE et ###ADDITIFS_RISQUE."
            )
            appendLine("- Si aucun additif, écrire ###ADDITIFS_RISQUE suivi d'une ligne vide")
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
                    AnalyzeCompositionResult.BilanSuccess(
                        bilan = bilan,
                        rawModelOutput = text,
                    )
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
                    val t = Thread(r, "miamia-gemma-${seq.incrementAndGet()}")
                    t.isDaemon = true
                    return t
                }
            }
        )
    }
}
