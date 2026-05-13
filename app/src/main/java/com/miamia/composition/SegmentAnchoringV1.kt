package com.miamia.composition

/**
 * Politique d’ancrage textuel **v1** (spec Feature C / clarify 2026-05-13) :
 * normalisations **mécaniques** uniquement (casse, espaces) puis test de sous-chaîne
 * dans le segment validé. Aucun synonyme implicite.
 */
object SegmentAnchoringV1 {

    const val MIN_INGREDIENT_LINE_LENGTH: Int = 4

    fun normalizeMechanical(text: String): String =
        text.lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()

    /** Vrai si [needleRaw], une fois normalisé mécaniquement, est une sous-chaîne du segment normalisé. */
    fun isSubstringAnchored(needleRaw: String, segmentRaw: String): Boolean {
        val needle = normalizeMechanical(needleRaw)
        if (needle.isEmpty()) return true
        val haystack = normalizeMechanical(segmentRaw)
        return haystack.contains(needle)
    }

    /**
     * Toutes les lignes d’ingrédients « checkables » (longueur ≥ [MIN_INGREDIENT_LINE_LENGTH])
     * doivent être ancrées dans le segment (**100 %**, pas de ratio partiel).
     */
    fun allCheckableIngredientLinesAnchored(
        ingredientLines: List<String>,
        segmentRaw: String,
    ): Boolean {
        val checkable = ingredientLines.map { it.trim() }.filter { it.length >= MIN_INGREDIENT_LINE_LENGTH }
        if (checkable.isEmpty()) return true
        return checkable.all { line -> isSubstringAnchored(line, segmentRaw) }
    }
}
