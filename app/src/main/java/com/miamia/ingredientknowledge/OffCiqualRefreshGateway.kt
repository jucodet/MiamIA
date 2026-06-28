package com.miamia.ingredientknowledge

import com.miamia.ingredientknowledge.dto.CiqualTableDto
import com.miamia.ingredientknowledge.dto.OffAdditivesTaxonomyDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Implémentation [KbRefreshGateway] de production : fetch de la taxonomie additive OpenFoodFacts
 * (couverture exhaustive) + des attributs Ciqual quand disponibles, via `HttpURLConnection`
 * (pattern `GemmaModelDownloader` — principe V, aucune nouvelle dépendance HTTP).
 *
 * Anti-corruption : valide à l'ingestion (E-number unique, `risk_level` valide, attributs Ciqual
 * cohérents), rejette/trace les entrées incohérentes (IKB-B-FR-011), n'invente rien
 * (IKB-B-FR-007). Les allergènes réglementaires UE ([baselineAllergens]) sont stables et
 * reportés tels quels dans le payload (non concernés par le refresh OFF/Ciqual).
 *
 * Le parsing est isolé dans [parse] (internal) pour les tests JVM pur sans réseau.
 */
class OffCiqualRefreshGateway(
    private val baselineAllergens: List<AllergenFactCard>,
    private val config: KbRefreshConfig = KbRefreshConfig(),
    private val fetcher: HttpFetcher = OffCiqualRefreshGateway.DefaultHttpFetcher,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : KbRefreshGateway {

    /** Abstraction réseau — testable avec un fake (JVM pur). */
    fun interface HttpFetcher {
        suspend fun fetchText(url: String, config: KbRefreshConfig): String
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetch(): KbRefreshPayload = withContext(Dispatchers.IO) {
        val offJson = try {
            fetcher.fetchText(config.offAdditivesUrl, config)
        } catch (e: Throwable) {
            throw KbRefreshUnavailable("Source OFF indisponible: ${e.message}", e)
        }
        // Source Ciqual optionnelle : indisponibilité → payload partiel, attributs omis
        // (repli silencieux, IKB-B-FR-011 — aucune invention).
        val ciqualJson = try {
            fetcher.fetchText(config.ciqualUrl, config)
        } catch (e: Throwable) {
            ""
        }
        parse(offJson, ciqualJson)
    }

    /**
     * Parse + valide les flux amont. Rejette/trace : E-number vide/dupliqué, `risk_level` invalide.
     * Omet les attributs Ciqual absents/incohérents. Lève [KbRefreshUnavailable] si OFF est illisible.
     */
    internal fun parse(offJson: String, ciqualJson: String): KbRefreshPayload {
        val off = runCatching { json.decodeFromString(OffAdditivesTaxonomyDto.serializer(), offJson) }
            .getOrElse { throw KbRefreshUnavailable("Taxonomie OFF illisible: ${it.message}", it) }
        val ciq = if (ciqualJson.isBlank()) CiqualTableDto()
        else runCatching { json.decodeFromString(CiqualTableDto.serializer(), ciqualJson) }
            .getOrDefault(CiqualTableDto())

        val ciqualByE = ciq.substances.associateBy { it.eNumber.trim().uppercase() }
        val seen = HashSet<String>()
        val additives = ArrayList<AdditiveFactCard>()
        val baseVersion = off.version.ifBlank { "off-${clock()}" }
        var rejected = 0

        for (entry in off.additives) {
            val key = entry.eNumber.trim().uppercase()
            if (key.isEmpty()) {
                rejected++
                continue
            }
            if (!seen.add(key)) {
                rejected++ // E-number dupliqué : on garde la première, on trace le rejet.
                continue
            }
            val risk = runCatching { RiskLevel.valueOf(entry.riskLevel.trim().uppercase()) }.getOrNull()
            if (risk == null) {
                rejected++ // risk_level invalide : entrée rejetée/tracée.
                continue
            }
            val canonical = entry.names["fr"]
                ?: entry.names.values.firstOrNull()
                ?: key
            val ciqual = ciqualByE[key]?.energyKcal?.let {
                CiqualAttributes(
                    energyKcal = it,
                    source = KbSource(KbSource.Origin.CIQUAL, baseVersion, config.ciqualUrl),
                )
            }
            additives += AdditiveFactCard(
                eNumber = key,
                canonicalName = canonical,
                aliases = entry.aliases,
                role = entry.role,
                riskLevel = risk,
                source = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, baseVersion, config.offAdditivesUrl),
                ciqual = ciqual,
            )
        }

        val ciqualAvailable = ciqualJson.isNotBlank() && ciq.substances.isNotEmpty()
        val sources = buildList {
            add("OFF_ADDITIVES_TAXONOMY")
            if (ciqualAvailable) add("CIQUAL")
        }

        return KbRefreshPayload(
            additives = additives,
            allergens = baselineAllergens,
            baseVersion = baseVersion,
            sourcesConsulted = sources,
            rejectedEntries = rejected,
            partial = !ciqualAvailable,
        )
    }

    companion object {
        /**
         * Fetcher par défaut — `HttpURLConnection`, timeouts et plafond depuis [KbRefreshConfig].
         */
        object DefaultHttpFetcher : HttpFetcher {
            override suspend fun fetchText(url: String, config: KbRefreshConfig): String {
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = config.connectTimeoutMs
                    readTimeout = config.readTimeoutMs
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                }
                try {
                    val code = conn.responseCode
                    if (code !in 200..299) {
                        throw IOException("HTTP $code pour $url")
                    }
                    val bytes = conn.inputStream.use { it.readBytes() }
                    if (bytes.size > config.maxPayloadBytes) {
                        throw IOException("Payload hors bornes (> ${config.maxPayloadBytes} octets) pour $url")
                    }
                    return String(bytes, Charsets.UTF_8)
                } finally {
                    conn.disconnect()
                }
            }
        }
    }
}
