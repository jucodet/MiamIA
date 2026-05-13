package com.miamia.composition

/**
 * Post-valide le bilan par rapport au texte source (US2 — pas d’ingrédients manifestement hors texte).
 * Feature C : ancrage **strict** v1 via [SegmentAnchoringV1] (tout ou rien sur les lignes checkables).
 * Le libellé [CompositionBilan.identifiedProduct] est souvent une catégorie (prompt ###PRODUIT) :
 * s’il n’est pas ancré dans le segment, il est retiré plutôt que de faire échouer liste + analyse.
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
            if (ing.length >= 2 && !SegmentAnchoringV1.isAnchoredInSegment(ing, segmentText)) {
                return AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
            }
        }
        val productClaim = bilan.identifiedProduct?.trim()?.takeIf { it.length >= 3 }
        val bilanForSuccess =
            if (productClaim != null && !SegmentAnchoringV1.isSubstringAnchored(productClaim, segmentText)) {
                bilan.copy(identifiedProduct = null, productConfidence = null)
            } else {
                bilan
            }
        return AnalyzeCompositionResult.BilanSuccess(bilan = bilanForSuccess, rawModelOutput = rawModelOutput)
    }
}
