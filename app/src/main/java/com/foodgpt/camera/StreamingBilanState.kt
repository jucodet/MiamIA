package com.foodgpt.camera

import com.foodgpt.composition.CompositionBilan

sealed class StreamingBilanState {
    data object Idle : StreamingBilanState()

    data class Streaming(
        val partialText: String = "",
        val partialIngredients: List<String> = emptyList(),
        val partialAnalysis: String? = null,
        val partialHealthImpacts: List<StreamingHealthImpact> = emptyList(),
        val sectionReached: StreamingSection = StreamingSection.NONE
    ) : StreamingBilanState()

    data class Complete(
        val bilan: CompositionBilan,
        val rawTranscript: String
    ) : StreamingBilanState()

    data class Error(
        val message: String,
        val errorCategory: String? = null
    ) : StreamingBilanState()
}

data class StreamingHealthImpact(
    val level: String,
    val ingredient: String,
    val note: String
)

enum class StreamingSection {
    NONE,
    LISTE,
    ANALYSE,
    IMPACT_SANTE,
    DONE
}
