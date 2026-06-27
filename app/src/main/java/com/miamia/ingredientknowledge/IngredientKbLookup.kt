package com.miamia.ingredientknowledge

/**
 * Moteur de matching lookup : sous-chaîne littérale après normalisations mécaniques
 * (casse, espaces, accents) — IKB-A-FR-002. Déduplication par E-number (additifs) et par id
 * (allergènes) — IKB-A-FR-012. Aucune invention, aucun synonyme métier (IKB-A-FR-003/006).
 */
object IngredientKbLookup {

    fun match(
        designations: List<IngredientDesignation>,
        additives: List<AdditiveFactCard>,
        allergens: List<AllergenFactCard>,
        baseVersion: String,
    ): LookupOutcome {
        val matchedAdditives = LinkedHashMap<String, AdditiveFactCard>()
        val matchedAllergens = LinkedHashMap<String, AllergenFactCard>()
        val unmatched = mutableListOf<IngredientDesignation>()

        for (designation in designations) {
            val haystack = designation.normalized
            if (haystack.isEmpty()) {
                unmatched += designation
                continue
            }

            val additiveHit = additives.firstOrNull { card ->
                additiveKeys(card).any { key -> key.isNotEmpty() && haystack.contains(key) }
            }
            val allergenHit = allergens.firstOrNull { card ->
                allergenKeys(card).any { key -> key.isNotEmpty() && haystack.contains(key) }
            }

            val hit = additiveHit != null || allergenHit != null
            if (additiveHit != null) matchedAdditives[additiveHit.eNumber] = additiveHit
            if (allergenHit != null) matchedAllergens[allergenHit.id] = allergenHit
            if (!hit) unmatched += designation
        }

        return LookupOutcome(
            matchedAdditives = matchedAdditives.values.toList(),
            matchedAllergens = matchedAllergens.values.toList(),
            unmatchedDesignations = unmatched,
            baseVersion = baseVersion,
        )
    }

    private fun additiveKeys(card: AdditiveFactCard): List<String> =
        buildList {
            add(card.eNumber)
            add(card.canonicalName)
            addAll(card.aliases)
        }.map { MechanicalNormalizer.normalize(it) }

    private fun allergenKeys(card: AllergenFactCard): List<String> =
        buildList {
            add(card.id)
            add(card.regulatoryName)
            addAll(card.aliases)
        }.map { MechanicalNormalizer.normalize(it) }
}
