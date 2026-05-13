package com.miamia.composition

/**
 * Harmonise les libellés d'ingrédients pour l'UI et le rapprochement liste ↔ verdict :
 * - pourcentages seuls entre parenthèses ;
 * - parenthèses mal fermées / `)` orphelins en fin d'OCR ;
 * - regroupements du type « farine de blé et de seigle » → deux entrées distinctes.
 */
object IngredientLabelNormalizer {

    /** Même idée que [SegmentAnchoringV1] : `(12 %)` / `(3,5%)` / `(50.2%)`. */
    private val percentOnlyParen = Regex("""\(\s*[0-9]+(?:[.,][0-9]+)?\s*%\s*\)""", RegexOption.IGNORE_CASE)

    /** Parenthèse ouverte en fin de chaîne sans fermeture (OCR / génération tronquée). */
    private val trailingIncompleteParen = Regex("""\([^)]*$""")

    /**
     * Étiquettes UE fréquentes : « farine(s) de blé et de seigle » → deux lignes
     * « farine de blé », « farine de seigle ».
     */
    private val farineDeEtDe = Regex(
        """(?i)^farines?\s+de\s+(.+?)\s+et\s+de\s+(.+)$""",
    )

    fun normalizeForMatching(s: String): String =
        preprocessOneLine(s).lowercase()

    internal fun preprocessOneLine(line: String): String {
        var t = IngredientOcrLexicon.apply(line).trim()
        t = percentOnlyParen.replace(t, " ")
        t = trailingIncompleteParen.replace(t, "")
        t = stripSurplusClosingParens(t)
        return t.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Retire des `)` en trop en fin de chaîne lorsque les parenthèses sont déséquilibrées
     * (ex. `huile de colza)` sans `(` correspondant).
     */
    private fun stripSurplusClosingParens(s: String): String {
        var t = s.trimEnd()
        while (t.endsWith(')') && t.count { it == '(' } < t.count { it == ')' }) {
            t = t.dropLast(1).trimEnd()
        }
        return t
    }

    /**
     * Si la ligne correspond à « farine(s) de X et de Y », renvoie deux libellés ;
     * sinon une seule entrée (déjà prétraitée).
     */
    internal fun expandCombinedFlourLines(preprocessedLine: String): List<String> {
        val t = preprocessedLine.trim()
        val m = farineDeEtDe.matchEntire(t) ?: return listOf(t)
        val a = m.groupValues[1].trim().trimEnd(',', '.')
        val b = m.groupValues[2].trim().trimEnd(',', '.')
        if (a.isEmpty() || b.isEmpty()) return listOf(t)
        return listOf("farine de $a", "farine de $b")
    }

    /**
     * Prétraitement seul (sans éclatement farine) — pour libellés déjà atomiques.
     */
    fun stripPercentAndBrokenParentheticals(s: String): String = preprocessOneLine(s)

    fun normalizeBilanIngredientLabels(bilan: CompositionBilan): CompositionBilan {
        val newLines = bilan.ingredientLines.flatMap { line ->
            expandCombinedFlourLines(preprocessOneLine(line))
        }
        val newImpacts = bilan.healthImpacts.flatMap { impact ->
            val parts = expandCombinedFlourLines(preprocessOneLine(impact.ingredient))
            parts.map { part -> impact.copy(ingredient = part) }
        }
        return bilan.copy(ingredientLines = newLines, healthImpacts = newImpacts)
    }
}
