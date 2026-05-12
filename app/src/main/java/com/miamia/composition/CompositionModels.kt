package com.miamia.composition

enum class GemmaErrorCode {
    GEMMA_NOT_FOUND,
    GEMMA_LOAD_FAILED,
    GEMMA_TIMEOUT
}

data class IngredientHealthImpact(
    val level: String,
    val ingredient: String,
    val note: String,
)

data class CompositionBilan(
    val ingredientLines: List<String>,
    val identifiedProduct: String? = null,
    val productConfidence: Int? = null,
    val compositionAnalysis: String,
    val disclaimer: String,
    val healthImpacts: List<IngredientHealthImpact> = emptyList(),
    val promptVersion: String = GemmaModelPaths.PROMPT_VERSION,
)

sealed class AnalyzeCompositionResult {
    data class BilanSuccess(val bilan: CompositionBilan) : AnalyzeCompositionResult()

    data class GemmaError(
        val code: GemmaErrorCode,
        val message: String
    ) : AnalyzeCompositionResult()

    data class CompositionLimit(val message: String) : AnalyzeCompositionResult()
}
