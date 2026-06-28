package com.miamia.ingredientknowledge

/**
 * Désignation d'ingrédient issue du [ValidatedIngredientSegment] (domaine amont
 * `ingredient-normalization-validation`), candidate au lookup.
 *
 * [rawText] : texte tel que présent dans le segment.
 * [normalized] : forme normalisée mécaniquement (casse, espaces, accents) via [MechanicalNormalizer].
 *
 * La normalisation est **mécanique et listée** uniquement (IKB-A-FR-002) ; aucune autre
 * transformation, aucune règle de synonyme.
 */
data class IngredientDesignation(
    val rawText: String,
    val normalized: String,
) {
    companion object {
        fun fromRaw(rawText: String): IngredientDesignation =
            IngredientDesignation(
                rawText = rawText,
                normalized = MechanicalNormalizer.normalize(rawText),
            )
    }
}
