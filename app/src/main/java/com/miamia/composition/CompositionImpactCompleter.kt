package com.miamia.composition

/**
 * Garantit une ligne de verdict [IngredientHealthImpact] pour chaque entrée de [CompositionBilan.ingredientLines]
 * lorsque le modèle en omet (ex. impact santé tronqué).
 */
object CompositionImpactCompleter {

    private fun impactMatchKey(s: String): String =
        IngredientLabelNormalizer.normalizeForMatching(s)

    fun completeMissingHealthImpacts(bilan: CompositionBilan): CompositionBilan {
        val lines = bilan.ingredientLines
        if (lines.isEmpty()) return bilan
        val impactKeys = bilan.healthImpacts.map { impactMatchKey(it.ingredient) }.toMutableSet()
        val additions = mutableListOf<IngredientHealthImpact>()
        for (line in lines) {
            if (line.trim().length < 2) continue
            val k = impactMatchKey(line)
            if (k !in impactKeys) {
                impactKeys.add(k)
                additions += IngredientHealthImpact(
                    level = "INCERTAIN",
                    ingredient = line,
                    note = "Verdict non fourni par le modèle pour cet ingrédient.",
                )
            }
        }
        if (additions.isEmpty()) return bilan
        return bilan.copy(healthImpacts = bilan.healthImpacts + additions)
    }
}
