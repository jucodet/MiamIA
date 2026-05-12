package com.miamia.additives

data class AnalysisDisplayResult(
    val sourceRawLlmText: String,
    val itemsOrdered: List<AdditiveRiskItem>,
    val summary: RiskSummaryKpi,
    val parseErrors: List<String>,
    val isEmptyState: Boolean,
)
