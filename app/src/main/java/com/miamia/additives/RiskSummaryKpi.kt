package com.miamia.additives

data class RiskSummaryKpi(
    val totalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val unknownCount: Int,
    val globalLevel: AdditiveRiskLevel?,
)
