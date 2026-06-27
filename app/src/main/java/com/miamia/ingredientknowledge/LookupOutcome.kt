package com.miamia.ingredientknowledge

/**
 * Résultat du lookup depuis une liste d'ingrédients.
 *
 * Aucune fiche inventée (IKB-A-FR-003). [unmatchedDesignations] regroupe les désignations non
 * référencées et **ne déclenche aucun blocage** du flux d'analyse (repli silencieux, IKB-A-FR-007).
 */
data class LookupOutcome(
    val matchedAdditives: List<AdditiveFactCard>,
    val matchedAllergens: List<AllergenFactCard>,
    val unmatchedDesignations: List<IngredientDesignation>,
    val baseVersion: String,
)
