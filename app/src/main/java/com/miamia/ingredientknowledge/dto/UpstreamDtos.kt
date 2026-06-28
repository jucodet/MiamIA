package com.miamia.ingredientknowledge.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO amont — taxonomie additive OpenFoodFacts (couverture exhaustive des E-numbers, IKB-B-FR-001).
 *
 * Format simplifié et stable consommé par [com.miamia.ingredientknowledge.OffCiqualRefreshGateway] ;
 * `ignoreUnknownKeys = true` absorbe les variations entre dumps OFF (research.md §9/16).
 */
@Serializable
data class OffAdditivesTaxonomyDto(
    val version: String = "",
    val additives: List<OffAdditiveEntryDto> = emptyList(),
)

@Serializable
data class OffAdditiveEntryDto(
    @SerialName("e_number") val eNumber: String,
    val names: Map<String, String> = emptyMap(),
    val aliases: List<String> = emptyList(),
    val role: String? = null,
    @SerialName("risk_level") val riskLevel: String,
)

/**
 * DTO amont — table de composition Ciqual (attributs nutritionnels par substance, IKB-B-FR-008).
 * Correspondance par `e_number` ; attributs absents/incohérents → omis (repli silencieux,
 * IKB-B-FR-011).
 */
@Serializable
data class CiqualTableDto(
    val substances: List<CiqualEntryDto> = emptyList(),
)

@Serializable
data class CiqualEntryDto(
    @SerialName("e_number") val eNumber: String,
    @SerialName("energy_kcal") val energyKcal: Double? = null,
)
