package com.miamia.composition

/**
 * Post-valide le bilan par rapport au texte source (US2 — pas d’ingrédients manifestement hors texte).
 * Feature C : ancrage v1 via [SegmentAnchoringV1] (tout ou rien sur les lignes checkables),
 * incluant reformulations « probables » lorsqu’un fragment OCR s’y retrouve encore.
 * Le segment est d’abord passé par [IngredientOcrLexicon.expandForAnchoring] (corrections OCR mot entier
 * pour l’ancrage uniquement).
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
        val segmentForChecks = IngredientOcrLexicon.expandForAnchoring(segmentText)
        if (!SegmentAnchoringV1.allCheckableIngredientLinesAnchored(bilan.ingredientLines, segmentForChecks)) {
            return AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
        }
        for (impact in bilan.healthImpacts) {
            val ing = impact.ingredient.trim()
            if (ing.length >= 2 && !SegmentAnchoringV1.isGroundedIngredientLine(ing, segmentForChecks)) {
                return AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
            }
        }
        val productClaim = bilan.identifiedProduct?.trim()?.takeIf { it.length >= 3 }
        val bilanForSuccess =
            if (productClaim != null && !SegmentAnchoringV1.isSubstringAnchored(productClaim, segmentForChecks)) {
                bilan.copy(identifiedProduct = null, productConfidence = null)
            } else {
                bilan
            }
        return AnalyzeCompositionResult.BilanSuccess(bilan = bilanForSuccess, rawModelOutput = rawModelOutput)
    }
}
