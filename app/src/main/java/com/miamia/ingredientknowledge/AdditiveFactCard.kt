package com.miamia.ingredientknowledge

/**
 * Fiche référence additif.
 *
 * **Clé primaire : [eNumber]** (IKB-A-FR-012). Les [aliases] sont des **alias de recherche**
 * rattachés à la même fiche canonique — ils ne constituent **pas** une règle de synonyme métier
 * (EquivalencePolicy v1 stricte du core inchangée, IKB-A-FR-006).
 */
data class AdditiveFactCard(
    val eNumber: String,
    val canonicalName: String,
    val aliases: List<String> = emptyList(),
    val role: String? = null,
    val riskLevel: RiskLevel,
    val source: KbSource,
)
