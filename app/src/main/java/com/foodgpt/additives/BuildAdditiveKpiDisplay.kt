package com.foodgpt.additives

import com.foodgpt.composition.CompositionBilan

/**
 * Construit le résultat affichable KPI additifs à partir du bilan composition et du texte brut modèle.
 * Les compteurs [RiskSummaryKpi] sont dérivés strictement de [AnalysisDisplayResult.itemsOrdered] (SC-003).
 */
object BuildAdditiveKpiDisplay {

    operator fun invoke(
        @Suppress("UNUSED_PARAMETER") bilan: CompositionBilan,
        rawLlmText: String,
    ): AnalysisDisplayResult {
        val outcome = AdditiveKpiParser.parse(rawLlmText)
        val items = outcome.items
        val isEmpty = items.isEmpty()
        val summary = buildSummary(items)
        return AnalysisDisplayResult(
            sourceRawLlmText = rawLlmText,
            itemsOrdered = items,
            summary = summary,
            parseErrors = outcome.warnings.toList(),
            isEmptyState = isEmpty,
        )
    }

    private fun buildSummary(items: List<AdditiveRiskItem>): RiskSummaryKpi {
        var h = 0
        var m = 0
        var l = 0
        var u = 0
        for (it in items) {
            when (it.level) {
                AdditiveRiskLevel.HIGH -> h++
                AdditiveRiskLevel.MEDIUM -> m++
                AdditiveRiskLevel.LOW -> l++
                AdditiveRiskLevel.UNKNOWN -> u++
            }
        }
        val total = items.size
        val okItems = items.filter { it.confidence == AdditiveLineConfidence.OK }
        val globalLevel = when {
            okItems.isEmpty() -> null
            okItems.all { it.level == AdditiveRiskLevel.UNKNOWN } -> null
            else -> okItems.minByOrNull { it.level.sortKey }!!.level
        }
        return RiskSummaryKpi(
            totalCount = total,
            highCount = h,
            mediumCount = m,
            lowCount = l,
            unknownCount = u,
            globalLevel = globalLevel,
        )
    }
}
