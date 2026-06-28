package com.miamia.ingredientknowledge

import kotlinx.serialization.Serializable

/**
 * Fiche référence additif.
 *
 * **Clé primaire : [eNumber]** (IKB-A-FR-012). Les [aliases] sont des **alias de recherche**
 * rattachés à la même fiche canonique — ils ne constituent **pas** une règle de synonyme métier
 * (EquivalencePolicy v1 stricte du core inchangée, IKB-A-FR-006).
 *
 * [ciqual] : attributs nutritionnels optionnels issus de Ciqual (IKB-B-FR-008), traçables via
 * `ciqual.source.origin = CIQUAL`. `null` si aucun attribut disponible (repli silencieux).
 *
 * `@Serializable` pour la persistance du cache offline (IKB-B — `FileKbCacheStore`).
 */
@Serializable
data class AdditiveFactCard(
    val eNumber: String,
    val canonicalName: String,
    val aliases: List<String> = emptyList(),
    val role: String? = null,
    val riskLevel: RiskLevel,
    val source: KbSource,
    val ciqual: CiqualAttributes? = null,
)
