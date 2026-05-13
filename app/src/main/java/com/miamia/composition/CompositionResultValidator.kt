package com.miamia.composition

/**
 * Post-valide le bilan par rapport au texte source (US2 — pas d’ingrédients manifestement hors texte).
 * Feature C : ancrage **strict** v1 via [SegmentAnchoringV1] (tout ou rien sur les lignes checkables).
 */
object CompositionResultValidator {

    /** Rejet explicite d’un bilan « vide » présenté comme complet (FR-012). */
    fun rejectEmptyStructure(bilan: CompositionBilan): AnalyzeCompositionResult.CompositionLimit? {
        if (bilan.ingredientLines.isEmpty() || bilan.compositionAnalysis.isBlank()) {
            return AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
        }
        return null
    }

    fun validateAgainstSource(
        bilan: CompositionBilan,
        segmentText: String,
        rawModelOutput: String = "",
    ): AnalyzeCompositionResult {
        if (!SegmentAnchoringV1.allCheckableIngredientLinesAnchored(bilan.ingredientLines, segmentText)) {
            return AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
        }
        for (impact in bilan.healthImpacts) {
            val ing = impact.ingredient.trim()
            if (ing.length >= 2 && !SegmentAnchoringV1.isSubstringAnchored(ing, segmentText)) {
                return AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
            }
        }
        bilan.identifiedProduct?.trim()?.takeIf { it.length >= 3 }?.let { product ->
            if (!SegmentAnchoringV1.isSubstringAnchored(product, segmentText)) {
                return AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
            }
        }
        return AnalyzeCompositionResult.BilanSuccess(bilan = bilan, rawModelOutput = rawModelOutput)
    }
}
