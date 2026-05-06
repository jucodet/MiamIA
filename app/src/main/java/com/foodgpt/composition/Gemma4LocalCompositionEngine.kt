package com.foodgpt.composition

import com.foodgpt.gemma4local.Gemma4LocalClient
import com.foodgpt.gemma4local.model.AnalyseTextuelleErrorType
import com.foodgpt.gemma4local.model.AnalyseTextuelleStatus

class Gemma4LocalCompositionEngine(
    private val localClient: Gemma4LocalClient
) : CompositionAnalysisEngine {
    override suspend fun analyze(
        rawText: String,
        maxInferenceMs: Long,
        onStreamPartial: ((String) -> Unit)?
    ): AnalyzeCompositionResult {
        val localResult = localClient.analyze(rawText)
        return if (localResult.status == AnalyseTextuelleStatus.SUCCESS && !localResult.outputText.isNullOrBlank()) {
            val parsed = GemmaBilanParser.parse(localResult.outputText)
            if (parsed != null) AnalyzeCompositionResult.BilanSuccess(parsed)
            else AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
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
}
