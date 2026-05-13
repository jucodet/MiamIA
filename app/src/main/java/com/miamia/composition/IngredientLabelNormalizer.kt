package com.miamia.composition

/**
 * Harmonise les libellés d'ingrédients pour l'UI et le rapprochement liste ↔ verdict :
 * les pourcentages seuls entre parenthèses (issus de l'étiquette / OCR) ne doivent pas
 * empêcher de reconnaître la même matière première (ex. `farine de blé (50,2 %)` ≈ `farine de blé`).
 */
object IngredientLabelNormalizer {

    /** Même idée que [SegmentAnchoringV1] : `(12 %)` / `(3,5%)` / `(50.2%)`. */
    private val percentOnlyParen = Regex("""\(\s*[0-9]+(?:[.,][0-9]+)?\s*%\s*\)""", RegexOption.IGNORE_CASE)

    /** Parenthèse ouverte en fin de chaîne sans fermeture (OCR / génération tronquée). */
    private val trailingIncompleteParen = Regex("""\([^)]*$""")

    fun normalizeForMatching(s: String): String {
        var t = IngredientOcrLexicon.apply(s).trim()
        t = percentOnlyParen.replace(t, " ")
        t = trailingIncompleteParen.replace(t, "")
        return t.replace(Regex("\\s+"), " ").trim().lowercase()
    }

    /**
     * Libellé affiché : retire pourcentages entre parenthèses et restes `(…` non fermés,
     * en conservant le reste du texte (ex. « huile (palme, colza) » reste tel quel si `)` présente).
     */
    fun stripPercentAndBrokenParentheticals(s: String): String {
        var t = IngredientOcrLexicon.apply(s).trim()
        t = percentOnlyParen.replace(t, " ")
        t = trailingIncompleteParen.replace(t, "")
        return t.replace(Regex("\\s+"), " ").trim()
    }

    fun normalizeBilanIngredientLabels(bilan: CompositionBilan): CompositionBilan =
        bilan.copy(
            ingredientLines = bilan.ingredientLines.map { stripPercentAndBrokenParentheticals(it) },
            healthImpacts = bilan.healthImpacts.map { impact ->
                impact.copy(ingredient = stripPercentAndBrokenParentheticals(impact.ingredient))
            },
        )
}
