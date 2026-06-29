package com.miamia.healthcritique

import android.content.Context
import android.util.Log
import com.miamia.BuildConfig
import com.miamia.composition.GemmaModelLocation
import com.miamia.composition.GemmaModelLocator
import com.miamia.gemma4local.HybridGemma4LocalGateway
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * Inférence Gemma locale pour la critique santé (spec 002).
 *
 * Réutilise le **même chemin d'inférence** que la composition via [HybridGemma4LocalGateway.inferStreaming]
 * (boucle backends NPU→GPU→CPU, engine fermé en `finally`, streaming `sendMessageAsync`). Un wrapper
 * `CompletableFuture`/`synchronized`/rétention d'engine dédié s'était révélé lever une
 * `IllegalStateException` (cycle de vie conversation/backend) sur tous les backends ; la délégation au
 * gateway éprouvé de la composition évite ce piège.
 *
 * Délai borné via [kotlinx.coroutines.withTimeout] (Feature A). Le modèle est résolu via
 * [GemmaModelLocator] pour distinguer `GEMMA_NOT_FOUND` / `GEMMA_LOAD_FAILED` avant l'inférence.
 */
class LiteRtHealthCritiqueRunner(
    context: Context,
    private val locator: GemmaModelLocator = GemmaModelLocator(context),
    private val gateway: HybridGemma4LocalGateway = HybridGemma4LocalGateway(context),
) : HealthCritiqueLlmRunner {

    override suspend fun generate(
        systemInstruction: String,
        userMessage: String,
        maxInferenceMs: Long,
        onStreamPartial: ((String) -> Unit)?,
    ): HealthCritiqueLlmGenerateResult {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "health_critique_llm_userMessage_len=${userMessage.length}")
        }
        return when (val located = locator.resolve()) {
            GemmaModelLocation.NotFound ->
                HealthCritiqueLlmGenerateResult.Failure(
                    HealthInferenceErrorCode.GEMMA_NOT_FOUND,
                    HealthCritiqueMessages.GEMMA_NOT_FOUND_USER,
                )

            is GemmaModelLocation.LoadFailed ->
                HealthCritiqueLlmGenerateResult.Failure(
                    HealthInferenceErrorCode.GEMMA_LOAD_FAILED,
                    HealthCritiqueMessages.GEMMA_LOAD_FAILED_USER,
                )

            is GemmaModelLocation.Ready -> runWithTimeout(
                systemInstruction,
                userMessage,
                maxInferenceMs,
                onStreamPartial,
            )
        }
    }

    private suspend fun runWithTimeout(
        systemInstruction: String,
        userMessage: String,
        maxInferenceMs: Long,
        onStreamPartial: ((String) -> Unit)?,
    ): HealthCritiqueLlmGenerateResult = try {
        val text = withTimeout(maxInferenceMs.coerceAtLeast(1L)) {
            gateway.inferStreaming(systemInstruction, userMessage, onStreamPartial)
        }.trim()
        if (text.isEmpty()) {
            HealthCritiqueLlmGenerateResult.Failure(
                HealthInferenceErrorCode.INFERENCE_FAILED,
                "Réponse vide du modèle. Réessayez.",
            )
        } else {
            HealthCritiqueLlmGenerateResult.Success(text)
        }
    } catch (_: TimeoutCancellationException) {
        HealthCritiqueLlmGenerateResult.Failure(
            HealthInferenceErrorCode.GEMMA_TIMEOUT,
            HealthCritiqueMessages.GEMMA_TIMEOUT_USER,
        )
    } catch (e: Exception) {
        Log.e(TAG, "critique_inference_failed ${e::class.java.simpleName}: ${e.message}", e)
        HealthCritiqueLlmGenerateResult.Failure(
            HealthInferenceErrorCode.GEMMA_LOAD_FAILED,
            "${HealthCritiqueMessages.GEMMA_LOAD_FAILED_USER} (${e.javaClass.simpleName})",
        )
    }

    companion object {
        private const val TAG = "LiteRtHealthCritique"
    }
}
