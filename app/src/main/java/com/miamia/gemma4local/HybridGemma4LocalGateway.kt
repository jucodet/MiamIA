package com.miamia.gemma4local

import android.content.Context
import android.util.Log
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

/**
 * Gateway LiteRT-LM avec telechargement automatique du modele Gemma 4 E2B
 * depuis HuggingFace. Au premier appel, le modele est telecharge et cache
 * dans filesDir. Les appels suivants reutilisent le cache local.
 */
class HybridGemma4LocalGateway(
    private val context: Context,
    private val downloader: GemmaModelDownloader = GemmaModelDownloader(context)
) : Gemma4LocalApiGateway, Gemma4LocalAvailabilityProbe {

    /**
     * Telecharge le modele si absent. Appeler AVANT analyzeText (au demarrage app).
     * Le telechargement peut prendre plusieurs minutes pour ~2.6 GB.
     */
    suspend fun ensureModelDownloaded() {
        downloader.ensureModelAvailable()
    }

    override suspend fun analyzeText(inputText: String): String {
        return analyzeTextStreaming(inputText, null)
    }

    suspend fun analyzeTextStreaming(inputText: String, onPartial: ((String) -> Unit)?): String {
        val modelFile = downloader.resolveLocalModel()
            ?: throw IllegalStateException("Modele Gemma local indisponible")
        return withContext(Dispatchers.IO) { runAnalyze(modelFile, inputText, onPartial) }
    }

    override suspend fun ping(): Boolean {
        return runCatching {
            val modelFile = downloader.resolveLocalModel() ?: return false
            withContext(Dispatchers.IO) {
                val output = runAnalyzeOnBackend(
                    modelPath = modelFile.absolutePath,
                    backend = Backend.CPU(),
                    inputText = "ok",
                    onPartial = null
                )
                val healthy = output.isNotBlank()
                Log.i(TAG, "health_ping healthy=$healthy backend=CPU")
                healthy
            }
        }.onFailure { t ->
            Log.w(TAG, "health_ping_failed ${t::class.java.simpleName}: ${t.message}")
        }.getOrDefault(false)
    }

    private fun runAnalyze(modelFile: File, inputText: String, onPartial: ((String) -> Unit)?): String {
        val backendErrors = mutableListOf<String>()
        for (backend in prioritizedBackends()) {
            val name = backendName(backend)
            val started = System.currentTimeMillis()
            val output = runCatching {
                runAnalyzeOnBackend(modelFile.absolutePath, backend, inputText, onPartial)
            }.getOrElse { t ->
                val elapsed = System.currentTimeMillis() - started
                backendErrors += "$name:${t.javaClass.simpleName}:${t.message.orEmpty()}"
                Log.e(TAG, "backend_fail $name ${elapsed}ms ${t::class.java.simpleName}: ${t.message}", t)
                null
            }
            if (!output.isNullOrBlank()) {
                Log.i(TAG, "backend_success $name ${System.currentTimeMillis() - started}ms chars=${output.length}")
                return output
            }
        }
        throw IllegalStateException(
            "Inference Gemma echouee sur tous les backends (${backendErrors.joinToString(",")})"
        )
    }

    private fun runAnalyzeOnBackend(
        modelPath: String,
        backend: Backend,
        inputText: String,
        onPartial: ((String) -> Unit)?
    ): String {
        val engine = Engine(
            EngineConfig(
                modelPath = modelPath,
                backend = backend,
                cacheDir = context.cacheDir.absolutePath
            )
        )
        return try {
            engine.initialize()
            val systemInstruction = Contents.of(
                "Tu analyses des listes d'ingredients alimentaires. " +
                    "Reponds uniquement avec 4 sections dans cet ordre : ###LISTE, ###ANALYSE, ###ADDITIFS_RISQUE, ###IMPACT_SANTE. " +
                    "Exemple : ###LISTE\n- eau\n- sucre\n- E300\n###ANALYSE\nProduit simple. Peu d'additifs.\n" +
                    "###ADDITIFS_RISQUE\nVERT|E300|Vitamine C naturelle\n" +
                    "###IMPACT_SANTE\nVERT|eau|Hydratation essentielle\nORANGE|sucre|Exces lie au surpoids\nVERT|E300|Sans risque aux doses alimentaires\n" +
                    "Regles : ###LISTE un ingredient par ligne avec -. ###ANALYSE 3 phrases max. " +
                    "###ADDITIFS_RISQUE et ###IMPACT_SANTE : chaque ligne commence par VERT ou ORANGE ou ROUGE ou INCERTAIN puis | puis nom puis | puis note courte. " +
                    "Si aucun additif, laisser ###ADDITIFS_RISQUE vide."
            )
            val conversationConfig = ConversationConfig(systemInstruction = systemInstruction)
            val prompt = "Texte capture (OCR):\n${inputText.trim().take(Gemma4LocalConfig.MAX_INPUT_CHARS)}"
            engine.createConversation(conversationConfig).use { conversation ->
                if (onPartial != null) {
                    collectStreaming(conversation, prompt, onPartial)
                } else {
                    textFromMessage(conversation.sendMessage(prompt)).trim()
                }
            }
        } finally {
            engine.close()
        }
    }

    private fun collectStreaming(
        conversation: Conversation,
        prompt: String,
        onPartial: (String) -> Unit
    ): String {
        var reconciled = ""
        runBlocking {
            conversation.sendMessageAsync(prompt)
                .catch { e ->
                    Log.w(TAG, "stream_error ${e::class.java.simpleName}: ${e.message}")
                    throw e
                }
                .collect { chunk ->
                    val piece = textFromMessage(chunk)
                    if (piece.isEmpty()) return@collect
                    reconciled = if (piece.startsWith(reconciled)) piece else reconciled + piece
                    onPartial(reconciled)
                }
        }
        return reconciled.trim()
    }

    private fun prioritizedBackends(): List<Backend> {
        val nativeLibDir = context.applicationInfo.nativeLibraryDir
        return buildList {
            add(Backend.NPU(nativeLibraryDir = nativeLibDir))
            add(Backend.GPU())
            add(Backend.CPU())
        }
    }

    private fun backendName(backend: Backend): String = when (backend) {
        is Backend.NPU -> "NPU"
        is Backend.GPU -> "GPU"
        is Backend.CPU -> "CPU"
        else -> backend.javaClass.simpleName
    }

    private fun textFromMessage(message: Message): String =
        message.contents.contents
            .asSequence()
            .filterIsInstance<Content.Text>()
            .map { it.text }
            .joinToString("\n")

    companion object {
        private const val TAG = "HybridGemma4Gateway"
    }
}
