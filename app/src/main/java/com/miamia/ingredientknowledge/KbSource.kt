package com.miamia.ingredientknowledge

/**
 * Provenance et version d'une fiche référence / de la base (IKB-A-FR-009).
 */
data class KbSource(
    val origin: Origin,
    val baseVersion: String,
    val sourceRef: String? = null,
) {
    enum class Origin {
        OFF_ADDITIVES_TAXONOMY,
        EU_ALLERGEN_LIST,
    }
}
