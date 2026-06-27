package com.miamia.ingredientknowledge

/**
 * Implémentation [ReferenceKb] alimentée par fixtures en code — JVM pur, sans Android assets.
 *
 * Usage : tests unitaires / jeu fixe (US-IKB-A3). Permet une exécution isolée hors capture/OCR/
 * runtime LLM, dans l'esprit de `GemmaBilanParserTest` (US-IKB-A3 / IKB-A-FR-008).
 */
class InMemoryReferenceKb(
    private val baseVersion: String,
    private val additives: List<AdditiveFactCard>,
    private val allergens: List<AllergenFactCard>,
) : ReferenceKb {

    override fun lookup(designations: List<IngredientDesignation>): LookupOutcome =
        IngredientKbLookup.match(
            designations = designations,
            additives = additives,
            allergens = allergens,
            baseVersion = baseVersion,
        )

    override fun baseVersion(): String = baseVersion
}
