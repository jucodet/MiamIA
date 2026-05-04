package com.foodgpt.composition

/**
 * Extrait [CompositionBilan] depuis une sortie texte du modèle (sections balisées).
 */
object GemmaBilanParser {

    private const val MARKER_LIST = "###LISTE"
    private const val MARKER_ANALYSIS = "###ANALYSE"
    /** Aligné avec [com.foodgpt.additives.AdditiveKpiParser.MARKER_ADDITIVES] (spec 003). */
    private const val MARKER_ADDITIVES_RISK = "###ADDITIFS_RISQUE"

    fun parse(modelOutput: String, disclaimer: String = CompositionMessages.DISCLAIMER_DEFAULT): CompositionBilan? {
        val trimmed = modelOutput.trim()
        if (trimmed.isEmpty()) return null
        val lower = trimmed.lowercase()
        val listIdx = lower.indexOf(MARKER_LIST.lowercase())
        val analysisIdx = lower.indexOf(MARKER_ANALYSIS.lowercase())
        if (listIdx == -1 || analysisIdx == -1 || analysisIdx <= listIdx) return null

        val listBlock = trimmed.substring(listIdx + MARKER_LIST.length, analysisIdx).trim()
        val afterAnalysisStart = trimmed.substring(analysisIdx + MARKER_ANALYSIS.length)
        val addiRelIdx = afterAnalysisStart.lowercase().indexOf(MARKER_ADDITIVES_RISK.lowercase())
        val analysisBlock = if (addiRelIdx == -1) {
            afterAnalysisStart.trim()
        } else {
            afterAnalysisStart.substring(0, addiRelIdx).trim()
        }

        val lines = listBlock.lines()
            .map { it.trim().removePrefix("-").trim() }
            .filter { it.isNotEmpty() }

        if (lines.isEmpty() || analysisBlock.isBlank()) return null

        return CompositionBilan(
            ingredientLines = lines,
            compositionAnalysis = analysisBlock,
            disclaimer = disclaimer
        )
    }
}
