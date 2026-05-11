package com.foodgpt.gemma4local

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.Dispatchers
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
        val modelFile = downloader.resolveLocalModel()
            ?: throw IllegalStateException("Modele Gemma local indisponible")
        return withContext(Dispatchers.IO) { runAnalyze(modelFile, inputText) }
    }

    override suspend fun ping(): Boolean {
        return runCatching {
            val modelFile = downloader.resolveLocalModel() ?: return false
            withContext(Dispatchers.IO) {
                val output = runAnalyzeOnBackend(
                    modelPath = modelFile.absolutePath,
                    backend = Backend.CPU(),
                    inputText = "ok"
                )
                val healthy = output.isNotBlank()
                Log.i(TAG, "health_ping healthy=$healthy backend=CPU")
                healthy
            }
        }.onFailure { t ->
            Log.w(TAG, "health_ping_failed ${t::class.java.simpleName}: ${t.message}")
        }.getOrDefault(false)
    }

    private fun runAnalyze(modelFile: File, inputText: String): String {
        val backendErrors = mutableListOf<String>()
        for (backend in prioritizedBackends()) {
            val name = backendName(backend)
            val started = System.currentTimeMillis()
            val output = runCatching {
                runAnalyzeOnBackend(modelFile.absolutePath, backend, inputText)
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

    private fun runAnalyzeOnBackend(modelPath: String, backend: Backend, inputText: String): String {
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
                    "Reponds uniquement avec les sections dans l'ordre : ###LISTE puis ###ANALYSE puis ###ADDITIFS_RISQUE. " +
                    "Sous ###LISTE : lignes - ingredient. Sous ###ANALYSE : au plus 3 phrases courtes, factuelles. " +
                    "Sous ###ADDITIFS_RISQUE : une ligne par additif au format exact NIVEAU|nom_additif|justification_courte ; " +
                    "NIVEAU dans {VERT, ORANGE, ROUGE, INCERTAIN}. Pas de texte libre hors ces lignes dans cette section."
            )
            val conversationConfig = ConversationConfig(systemInstruction = systemInstruction)
            val prompt = "Texte capture (OCR):\n${inputText.trim().take(Gemma4LocalConfig.MAX_INPUT_CHARS)}"
            engine.createConversation(conversationConfig).use { conversation ->
                textFromMessage(conversation.sendMessage(prompt)).trim()
            }
        } finally {
            engine.close()
        }
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
