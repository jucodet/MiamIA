package com.miamia.composition

/**
 * Corrections **mot entier** pour erreurs OCR fréquentes sur les listes d’ingrédients (FR).
 * Utilisé pour l’ancrage et pour l’affichage post-parse (liste + verdict + analyse).
 */
object IngredientOcrLexicon {

    private data class WholeWordFix(val pattern: Regex, val replacement: String)

    private val FIXES: List<WholeWordFix> = listOf(
        WholeWordFix(Regex("\\bomidon\\b", RegexOption.IGNORE_CASE), "amidon"),
        WholeWordFix(Regex("\\b0midon\\b", RegexOption.IGNORE_CASE), "amidon"),
        WholeWordFix(Regex("\\bpolmiste\\b", RegexOption.IGNORE_CASE), "palmiste"),
    )

    fun apply(text: String): String {
        var s = text
        for ((pattern, replacement) in FIXES) {
            s = pattern.replace(s, replacement)
        }
        return s
    }

    /** Segment OCR tel qu’envoyé au modèle, étendu pour les contrôles d’ancrage. */
    fun expandForAnchoring(segmentText: String): String = apply(segmentText)

    fun applyToBilan(bilan: CompositionBilan): CompositionBilan =
        bilan.copy(
            ingredientLines = bilan.ingredientLines.map { apply(it) },
            compositionAnalysis = apply(bilan.compositionAnalysis),
            identifiedProduct = bilan.identifiedProduct?.let { apply(it) },
            healthImpacts = bilan.healthImpacts.map { it.copy(ingredient = apply(it.ingredient)) },
        )
}
