package com.miamia.additives

import com.miamia.composition.CompositionBilan
import com.miamia.composition.SegmentAnchoringV1

/**
 * Construit le résultat affichable KPI additifs à partir du bilan composition et du texte brut modèle.
 * Les compteurs [RiskSummaryKpi] sont dérivés strictement de [AnalysisDisplayResult.itemsOrdered] (SC-003).
 *
 * @param rawLlmTextForParsing Sortie texte du modèle (sections ###ADDITIFS_RISQUE, etc.).
 * @param validatedIngredientSegment Segment ingrédients validé ; sert à filtrer les lignes non ancrées (**IHI-C-FR-007**).
 */
object BuildAdditiveKpiDisplay {

    operator fun invoke(
        bilan: CompositionBilan,
        rawLlmTextForParsing: String,
        validatedIngredientSegment: String = rawLlmTextForParsing,
    ): AnalysisDisplayResult {
        val outcome = AdditiveKpiParser.parse(rawLlmTextForParsing)
        val before = outcome.items
        val filtered = before.filter { item -> isAdditiveAnchoredInSegment(item, validatedIngredientSegment) }
        val warnings = outcome.warnings.toMutableList()
        if (before.isNotEmpty() && filtered.size < before.size) {
            warnings += "Certains additifs détectés ne figurent pas sur l'étiquette validée et ont été masqués."
        }
        val isEmpty = filtered.isEmpty()
        val summary = buildSummary(filtered)
        return AnalysisDisplayResult(
            sourceRawLlmText = rawLlmTextForParsing,
            itemsOrdered = filtered,
            summary = summary,
            parseErrors = warnings,
            isEmptyState = isEmpty,
        )
    }

    private fun isAdditiveAnchoredInSegment(item: AdditiveRiskItem, segment: String): Boolean {
        val candidates = listOf(item.displayName, item.canonicalName)
            .map { it.trim() }
            .filter { it.length >= 2 }
        if (candidates.isEmpty()) return false
        return candidates.any { SegmentAnchoringV1.isSubstringAnchored(it, segment) }
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
