package com.miamia.ingredientknowledge.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdditiveDto(
    @SerialName("e_number") val eNumber: String,
    @SerialName("canonical_name") val canonicalName: String,
    val aliases: List<String> = emptyList(),
    val role: String? = null,
    @SerialName("risk_level") val riskLevel: String,
)

@Serializable
data class AllergenDto(
    val id: String,
    @SerialName("regulatory_name") val regulatoryName: String,
    val aliases: List<String> = emptyList(),
)

@Serializable
data class KbVersionDto(
    val version: String,
    @SerialName("additives_source") val additivesSource: String,
    @SerialName("allergens_source") val allergensSource: String,
)
