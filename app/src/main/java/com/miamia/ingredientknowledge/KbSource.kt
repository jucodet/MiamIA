package com.miamia.ingredientknowledge

import kotlinx.serialization.Serializable

/**
 * Provenance et version d'une fiche référence / de la base (IKB-A-FR-009).
 *
 * `@Serializable` pour la persistance du cache offline (IKB-B — `FileKbCacheStore`).
 */
@Serializable
data class KbSource(
    val origin: Origin,
    val baseVersion: String,
    val sourceRef: String? = null,
) {
    enum class Origin {
        OFF_ADDITIVES_TAXONOMY,
        EU_ALLERGEN_LIST,
        /** Attributs nutritionnels Ciqual rattachés à un additif (IKB-B-FR-008). */
        CIQUAL,
    }
}
