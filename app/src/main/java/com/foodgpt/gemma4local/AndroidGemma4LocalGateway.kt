package com.foodgpt.gemma4local

import android.content.Context
import android.util.Log
import com.foodgpt.composition.GemmaModelLocation
import com.foodgpt.composition.GemmaModelLocator
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Point d'integration local Gemma4 via runtime LLM on-device (LiteRT-LM).
) : Gemma4LocalApiGateway, Gemma4LocalAvailabilityProbe {

    override suspend fun analyzeText(inputText: String): String {
        // Timeout unifie: gere uniquement par Gemma4LocalClient.withTimeout(...)
        return withContext(Dispatchers.IO) { runAnalyze(inputText) }
    }

    private fun runAnalyze(inputText: String): String {
        val modelFile = resolveModelFileForInference()

        val backendErrors = mutableListOf<String>()
        for (backend in prioritizedBackends()) {
            val backendName = backendName(backend)
            val startedAt = System.currentTimeMillis()
            val output = runCatching {
                runAnalyzeOnBackend(
                    modelPath = modelFile.absolutePath,
                    backend = backend,
                    inputText = inputText
                )
            }.getOrElse { t ->
                val elapsedMs = System.currentTimeMillis() - startedAt
                backendErrors += "$backendName:${t.javaClass.simpleName}:${t.message.orEmpty()}"
                Log.e(
                    TAG,
                    "diag_backend_fail backend=$backendName elapsedMs=$elapsedMs throwable=${t::class.java.simpleName} message=${t.message}",
                    t
                )
                null
            }
            if (!output.isNullOrBlank()) {
                val elapsedMs = System.currentTimeMillis() - startedAt
                Log.i(
                    TAG,
                    "diag_backend_success backend=$backendName elapsedMs=$elapsedMs outputChars=${output.length}"
                )
                return output
            }
            Log.w(TAG, "diag_backend_empty backend=$backendName")
        }
        throw IllegalStateException(
            "Execution Gemma4 locale en echec sur tous les backends (${backendErrors.joinToString(",")})."
        )
    }

    private fun resolveModelFileForInference(): java.io.File {
        val locatedModel = modelLocator.resolve()
        val modelFile = (locatedModel as? GemmaModelLocation.Ready)?.modelFile
            ?: throw IllegalStateException("Modele Gemma local indisponible")
        if (!modelFile.isFile || modelFile.length() <= 0L) {
            throw IllegalStateException("Modele Gemma local invalide (fichier absent ou vide).")
        }
        if (!modelFile.name.endsWith(".litertlm", ignoreCase = true)) {
            throw IllegalStateException(
                "Format modele invalide: ${modelFile.name}. Extension .litertlm requise."
            )
        }
        Log.i(
            TAG,
            "diag_model_selected path=${modelFile.absolutePath} bytes=${modelFile.length()} format=.litertlm"
        )
        return modelFile
    }

    private fun runAnalyzeOnBackend(
        modelPath: String,
        backend: Backend,

    override suspend fun ping(): Boolean {
        return withContext(Dispatchers.IO) {
            val modelFile = resolveModelFileForInference()
            val healthPrompt = "ok"
            val output = runAnalyzeOnBackend(
                modelPath = modelFile.absolutePath,
                backend = Backend.CPU(),
                inputText = healthPrompt
            )
            val healthy = output.isNotBlank()
            Log.i(
                TAG,
                "diag_health_ping healthy=$healthy outputChars=${output.length} backend=CPU"
            )
            healthy
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
            .joinToString("\n")

    companion object {
        private const val TAG = "Gemma4LocalGateway"
    }
}