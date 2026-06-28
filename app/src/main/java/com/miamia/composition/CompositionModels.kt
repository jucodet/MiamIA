package com.miamia.composition

import com.miamia.gemma4local.model.BackendExecution

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
    /** kcal pour 100 g, indicatif — null si absent ou hors bornes après garde-fous (Feature K). */
    val estimatedKcalPer100g: Int? = null,
    val promptVersion: String = GemmaModelPaths.PROMPT_VERSION,
)

sealed class AnalyzeCompositionResult {
    /**
     * @param rawModelOutput Texte brut renvoyé par le modèle (parse additifs / traçabilité).
     *        Peut être vide si le backend ne le fournit pas.
     */
    data class BilanSuccess(
        val bilan: CompositionBilan,
        val rawModelOutput: String = "",
        val backend: BackendExecution = BackendExecution.INDETERMINATE,
    ) : AnalyzeCompositionResult()

    data class GemmaError(
        val code: GemmaErrorCode,
        val message: String
    ) : AnalyzeCompositionResult()

    data class CompositionLimit(val message: String) : AnalyzeCompositionResult()
}
