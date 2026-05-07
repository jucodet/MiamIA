package com.foodgpt.gemma4local

import android.content.Context
import android.util.Log
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation

/**
 * Gateway hybride:
 * 1) utilise le runtime local ML Kit GenAI (mode "device-managed").
 */
class HybridGemma4LocalGateway(
    context: Context
) : Gemma4LocalApiGateway, Gemma4LocalAvailabilityProbe {

    private val managedModel by lazy { Generation.getClient() }

    override suspend fun analyzeText(inputText: String): String {
        val output = runManagedInferenceOrNull(inputText)
        if (!output.isNullOrBlank()) return output
        throw IllegalStateException("Runtime GenAI local indisponible ou reponse vide.")
    }

    override suspend fun ping(): Boolean {
        return runManagedHealthCheck()
    }

    private suspend fun runManagedInferenceOrNull(inputText: String): String? {
        return runCatching {
            if (!isManagedFeatureReady()) return null
            val response = managedModel.generateContent(inputText.trim().take(Gemma4LocalConfig.MAX_INPUT_CHARS))
            extractTextFromResponse(response).trim()
        }.onFailure { t ->
            Log.w(
                TAG,
                "managed_inference_failed throwable=${t::class.java.simpleName} message=${t.message}"
            )
        }.getOrNull()
    }

    private suspend fun runManagedHealthCheck(): Boolean {
        return runCatching {
            if (!isManagedFeatureReady()) return false
            val healthResponse = managedModel.generateContent("ok")
            val healthy = extractTextFromResponse(healthResponse).isNotBlank()
            Log.i(TAG, "managed_health_check healthy=$healthy")
            healthy
        }.onFailure { t ->
            Log.w(
                TAG,
                "managed_health_check_failed throwable=${t::class.java.simpleName} message=${t.message}"
            )
        }.getOrDefault(false)
    }

    private suspend fun isManagedFeatureReady(): Boolean {
        val status = managedModel.checkStatus()
        return when (status) {
            FeatureStatus.AVAILABLE -> true
            FeatureStatus.DOWNLOADABLE -> {
                Log.i(TAG, "managed_status=DOWNLOADABLE")
                false
            }
            FeatureStatus.DOWNLOADING -> {
                Log.i(TAG, "managed_status=DOWNLOADING")
                false
            }
            FeatureStatus.UNAVAILABLE -> {
                Log.i(TAG, "managed_status=UNAVAILABLE")
                false
            }
            else -> false
        }
    }

    /**
     * Compat API beta: certains artefacts n'exposent pas `response.text`.
     * On lit d'abord candidates[0].text si disponible, sinon toString().
     */
    private fun extractTextFromResponse(response: Any): String {
        return runCatching {
            val candidatesMethod = response.javaClass.methods.firstOrNull { it.name == "getCandidates" }
                ?: return@runCatching response.toString()
            val candidates = candidatesMethod.invoke(response) as? List<*>
            val first = candidates?.firstOrNull() ?: return@runCatching response.toString()
            val candidateTextMethod = first.javaClass.methods.firstOrNull { it.name == "getText" }
                ?: return@runCatching response.toString()
            (candidateTextMethod.invoke(first) as? String).orEmpty()
        }.getOrDefault(response.toString())
    }

    companion object {
        private const val TAG = "HybridGemma4Gateway"
    }
}

