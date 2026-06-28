package com.miamia.composition

import android.util.Log
import com.miamia.gemma4local.Gemma4LocalClient
import com.miamia.gemma4local.model.AnalyseTextuelleErrorType
import com.miamia.gemma4local.model.AnalyseTextuelleStatus

class Gemma4LocalCompositionEngine(
    private val localClient: Gemma4LocalClient
) : CompositionAnalysisEngine {
    override suspend fun analyze(
        rawText: String,
        maxInferenceMs: Long,
        onStreamPartial: ((String) -> Unit)?
    ): AnalyzeCompositionResult {
        val localResult = localClient.analyze(rawText, onStreamPartial)
        return if (localResult.status == AnalyseTextuelleStatus.SUCCESS && !localResult.outputText.isNullOrBlank()) {
            Log.d(TAG, "gemma_raw_output length=${localResult.outputText.length} text=[${localResult.outputText.take(500)}]")
            val parsed = GemmaBilanParser.parse(localResult.outputText)
            if (parsed != null) AnalyzeCompositionResult.BilanSuccess(
                bilan = parsed,
                rawModelOutput = localResult.outputText.orEmpty(),
                backend = localResult.backend,
            )
            else {
                Log.w(TAG, "parse_failed full_output=[${localResult.outputText}]")
                AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
            }
        } else {
            val mappedCode = when (localResult.errorType) {
                AnalyseTextuelleErrorType.TIMEOUT -> GemmaErrorCode.GEMMA_TIMEOUT
                AnalyseTextuelleErrorType.INVALID_RESPONSE -> return AnalyzeCompositionResult.CompositionLimit(
                    localResult.userMessage.ifBlank { CompositionMessages.COMPOSITION_LIMIT_GENERIC }
                )
                else -> GemmaErrorCode.GEMMA_NOT_FOUND
            }
            AnalyzeCompositionResult.GemmaError(
                mappedCode,
                localResult.userMessage.ifBlank { CompositionMessages.GEMMA_LOAD_FAILED_USER }
            )
        }
    }

    companion object {
        private const val TAG = "CompositionEngine"
    }
}
