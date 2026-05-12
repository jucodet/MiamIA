package com.miamia.composition

/**
 * Post-valide le bilan par rapport au texte source (US2 — pas d’ingrédients manifestement hors texte).
 */
object CompositionResultValidator {

    /** Rejet explicite d’un bilan « vide » présenté comme complet (FR-012). */
    fun rejectEmptyStructure(bilan: CompositionBilan): AnalyzeCompositionResult.CompositionLimit? {
        if (bilan.ingredientLines.isEmpty() || bilan.compositionAnalysis.isBlank()) {
            return AnalyzeCompositionResult.CompositionLimit(CompositionMessages.COMPOSITION_LIMIT_GENERIC)
        }
        return null
    }

    fun validateAgainstSource(bilan: CompositionBilan, rawText: String): AnalyzeCompositionResult {
        val normalizedSource = rawText.lowercase()
        val checkable = bilan.ingredientLines.filter { it.trim().length >= 4 }
        if (checkable.isEmpty()) return AnalyzeCompositionResult.BilanSuccess(bilan)

        val missingCount = checkable.count { line ->
            val token = line.lowercase().trim()
            !normalizedSource.contains(token)
        }
        val missingRatio = missingCount.toDouble() / checkable.size
        if (missingRatio > 0.5) {
            return AnalyzeCompositionResult.CompositionLimit(
                CompositionMessages.COMPOSITION_LIMIT_GENERIC
            )
        }
        return AnalyzeCompositionResult.BilanSuccess(bilan)
    }
}
