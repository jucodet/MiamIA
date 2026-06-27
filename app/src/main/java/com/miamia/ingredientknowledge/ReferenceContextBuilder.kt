package com.miamia.ingredientknowledge

/**
 * Constitue un [ReferenceContext] borné (plafond N) et priorisé :
 *  - allergènes d'abord,
 *  - puis additifs par risque décroissant (ELEVE → MODERE → FAIBLE),
 *  - fiches au-delà du plafond : omises (repli silencieux, IKB-A-FR-011).
 *
 * Aucune formulation de fait étiquette (qualification GENERAL, IKB-A-FR-004/005).
 */
class ReferenceContextBuilder(
    private val maxCards: Int = DEFAULT_MAX_CARDS,
) {
    fun build(outcome: LookupOutcome): ReferenceContext {
        val allergenEntries = outcome.matchedAllergens.map { ReferenceContextEntry.from(it) }
        val additiveEntries = outcome.matchedAdditives
            .sortedBy { it.riskLevel.priorityOrder }
            .map { ReferenceContextEntry.from(it) }

        val ordered = allergenEntries + additiveEntries
        val capped = ordered.take(maxCards)

        return ReferenceContext(
            baseVersion = outcome.baseVersion,
            cards = capped,
        )
    }

    companion object {
        const val DEFAULT_MAX_CARDS: Int = 12
    }
}
