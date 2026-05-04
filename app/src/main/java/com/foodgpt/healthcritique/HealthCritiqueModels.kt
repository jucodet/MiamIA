package com.foodgpt.healthcritique

enum class PopulationKey {
    ENFANTS,
    FEMMES_ENCEINTES,
    ADULTES,
    PERSONNES_AGEES,
}

enum class InputInvalidReason {
    EMPTY,
    TOO_SHORT,
    /** Aucun segment ingrédients validé issu du scan (contrat `no_validated_segment`). */
    NO_VALIDATED_SEGMENT,
}

enum class HealthInferenceErrorCode {
    GEMMA_NOT_FOUND,
    GEMMA_LOAD_FAILED,
    GEMMA_TIMEOUT,
    INFERENCE_FAILED,
}

sealed class HealthCritiqueResult {
    data class CritiqueReady(
        val requestId: String,
        val llmRawText: String,
        val sections: Map<PopulationKey, String>,
        val parseWarnings: List<String>,
        val disclaimer: String,
        val processedAtEpochMs: Long,
    ) : HealthCritiqueResult()

    data class InferenceError(
        val requestId: String,
        val errorCode: HealthInferenceErrorCode,
        val message: String,
        val processedAtEpochMs: Long,
    ) : HealthCritiqueResult()

    data class InputInvalid(
        val requestId: String,
        val reasonCode: InputInvalidReason,
        val message: String,
        val processedAtEpochMs: Long,
    ) : HealthCritiqueResult()
}

data class LastHealthAnalysisSnapshot(
    val savedAtEpochMs: Long,
    val ingredientRaw: String,
    val resultRaw: String,
    val systemPromptSnapshot: String,
)
