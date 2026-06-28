package com.miamia.ingredientknowledge

/**
 * Frontière d'anti-corruption vers les sources amont OpenFoodFacts / Ciqual
 * (contracts/kb-refresh-gateway-contract.md).
 *
 * Isole le réseau : testable avec un fake (JVM pur). L'implémentation de production
 * [OffCiqualRefreshGateway] réutilise `HttpURLConnection` (pattern `GemmaModelDownloader`).
 */
interface KbRefreshGateway {
    /**
     * Récupère + valide la taxonomie OFF (couverture exhaustive) et les attributs Ciqual quand
     * disponibles. Rejette/trace les entrées incohérentes (IKB-B-FR-011). N'invente rien
     * (IKB-B-FR-007). Lève une [KbRefreshUnavailable] en cas d'indisponibilité réseau/parse
     * (le coordinator transforme en `KbRefreshOutcome.OFFLINE_FALLBACK`).
     */
    suspend fun fetch(): KbRefreshPayload
}

/**
 * Échec d'accès aux sources amont (réseau, parse, payload hors bornes).
 * Erreur domaine — non bloquante pour l'app (repli offline en cascade).
 */
class KbRefreshUnavailable(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

/**
 * Payload rafraîchi produit par le gateway.
 *
 * @param additives taxonomie OFF (couverture exhaustive).
 * @param allergens allergènes réglementaires UE (source inchangée).
 * @param baseVersion version rafraîchie (depuis OFF / bumpée).
 * @param sourcesConsulted libellés des sources réellement consultées.
 * @param rejectedEntries nombre d'entrées incohérentes rejetées à l'ingestion (IKB-B-FR-011).
 * @param partial true si une source était indisponible (couverture réduite).
 */
data class KbRefreshPayload(
    val additives: List<AdditiveFactCard>,
    val allergens: List<AllergenFactCard>,
    val baseVersion: String,
    val sourcesConsulted: List<String>,
    val rejectedEntries: Int,
    val partial: Boolean,
)
