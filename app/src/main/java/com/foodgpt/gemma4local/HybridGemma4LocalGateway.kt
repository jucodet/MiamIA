package com.foodgpt.gemma4local

import android.content.Context
import android.util.Log
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlinx.coroutines.flow.last

/**
 * Gateway utilisant le modele Gemini Nano geré par Google AI Core / ML Kit GenAI.
 * Le modele est telecharge et maintenu par le systeme (Google Play Services),
 * aucun fichier .litertlm local n'est requis.
 */
class HybridGemma4LocalGateway(
    context: Context
) : Gemma4LocalApiGateway, Gemma4LocalAvailabilityProbe {

    private val managedModel: GenerativeModel by lazy { Generation.getClient() }

    override suspend fun analyzeText(inputText: String): String {
        ensureModelReady()
        val response = managedModel.generateContent(
            inputText.trim().take(Gemma4LocalConfig.MAX_INPUT_CHARS)
        )
        val text = response.candidates.firstOrNull()?.text.orEmpty().trim()
        if (text.isBlank()) {
            throw IllegalStateException("Reponse Gemini Nano vide.")
        }
        return text
    }

    override suspend fun ping(): Boolean {
        return runCatching {
            ensureModelReady()
            val response = managedModel.generateContent("ok")
            val healthy = response.candidates.firstOrNull()?.text.orEmpty().isNotBlank()
            Log.i(TAG, "managed_health_check healthy=$healthy")
            healthy
        }.onFailure { t ->
            Log.w(TAG, "managed_health_check_failed ${t::class.java.simpleName}: ${t.message}")
        }.getOrDefault(false)
    }

    private suspend fun ensureModelReady() {
        when (val status = managedModel.checkStatus()) {
            FeatureStatus.AVAILABLE -> {
                Log.d(TAG, "managed_status=AVAILABLE")
            }
            FeatureStatus.DOWNLOADABLE -> {
                Log.i(TAG, "managed_status=DOWNLOADABLE — triggering download")
                managedModel.download().collect { dlStatus ->
                    when (dlStatus) {
                        is DownloadStatus.DownloadStarted ->
                            Log.i(TAG, "download_started")
                        is DownloadStatus.DownloadProgress ->
                            Log.i(TAG, "download_progress bytes=${dlStatus.totalBytesDownloaded}")
                        DownloadStatus.DownloadCompleted ->
                            Log.i(TAG, "download_completed")
                        is DownloadStatus.DownloadFailed ->
                            throw IllegalStateException(
                                "Telechargement du modele echoue: ${dlStatus.e.message}"
                            )
                    }
                }
            }
            FeatureStatus.DOWNLOADING -> {
                Log.i(TAG, "managed_status=DOWNLOADING — waiting for completion")
                managedModel.download().last()
            }
            FeatureStatus.UNAVAILABLE -> {
                throw IllegalStateException(
                    "Gemini Nano non supporte sur cet appareil."
                )
            }
            else -> {
                throw IllegalStateException(
                    "Statut ML Kit GenAI inconnu: $status"
                )
            }
        }
    }

    companion object {
        private const val TAG = "HybridGemma4Gateway"
    }
}
