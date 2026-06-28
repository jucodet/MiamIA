package com.miamia.ingredientknowledge

import java.util.concurrent.TimeUnit

/**
 * Configuration des sources amont du refresh (IKB-B-FR-001).
 *
 * Centralise les URLs (taxonomie additive OpenFoodFacts + table Ciqual), les timeouts et la
 * taille max de payload acceptée. Réutilise `HttpURLConnection` (pattern `GemmaModelDownloader`,
 * principe V — aucune nouvelle dépendance HTTP).
 */
data class KbRefreshConfig(
    val offAdditivesUrl: String = DEFAULT_OFF_ADDITIVES_URL,
    val ciqualUrl: String = DEFAULT_CIQUAL_URL,
    val connectTimeoutMs: Int = TimeUnit.SECONDS.toMillis(15).toInt(),
    val readTimeoutMs: Int = TimeUnit.SECONDS.toMillis(20).toInt(),
    val maxPayloadBytes: Int = DEFAULT_MAX_PAYLOAD_BYTES,
) {
    companion object {
        // Taxonomie additive OFF (couverture exhaustive des E-numbers).
        const val DEFAULT_OFF_ADDITIVES_URL: String =
            "https://world.openfoodfacts.org/data/taxonomies/additives.json"

        // Table de composition Ciqual (open data ANSES) — attributs nutritionnels par substance.
        const val DEFAULT_CIQUAL_URL: String =
            "https://ciqual.anses.fr/les-donnees-ciqal"

        // Filet de sécurité : plafond raisonnable pour un dump statique (~Mo).
        const val DEFAULT_MAX_PAYLOAD_BYTES: Int = 20 * 1024 * 1024
    }
}
