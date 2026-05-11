package com.foodgpt.composition

/**
 * Extrait [CompositionBilan] depuis une sortie texte du modèle (sections balisées).
 * Tolère les variations de formatage courantes (espaces, `#` multiples, deux-points).
 */
object GemmaBilanParser {

    private val LISTE_PATTERN = Regex("""#{1,4}\s*LISTE\s*:?""", RegexOption.IGNORE_CASE)
    private val ANALYSE_PATTERN = Regex("""#{1,4}\s*ANALYSE\s*:?""", RegexOption.IGNORE_CASE)
    private val ADDITIFS_PATTERN = Regex("""#{1,4}\s*ADDITIFS[_\s]*RISQUE\s*:?""", RegexOption.IGNORE_CASE)

    fun parse(modelOutput: String, disclaimer: String = CompositionMessages.DISCLAIMER_DEFAULT): CompositionBilan? {
        val trimmed = modelOutput.trim()
        if (trimmed.isEmpty()) return null

        val listMatch = LISTE_PATTERN.find(trimmed)
        val analysisMatch = ANALYSE_PATTERN.find(trimmed)

        if (listMatch == null || analysisMatch == null) return null
        if (analysisMatch.range.first <= listMatch.range.last) return null

        val listBlock = trimmed.substring(listMatch.range.last + 1, analysisMatch.range.first).trim()
        val afterAnalysisStart = trimmed.substring(analysisMatch.range.last + 1)
        val additivesMatch = ADDITIFS_PATTERN.find(afterAnalysisStart)
        val analysisBlock = if (additivesMatch == null) {
            afterAnalysisStart.trim()
        } else {
            afterAnalysisStart.substring(0, additivesMatch.range.first).trim()
        }

        val rawLines = listBlock.lines()
            .map { it.trim().removePrefix("-").removePrefix("*").removePrefix("•").trim() }
            .filter { it.isNotEmpty() }

        val lines = rawLines.flatMap { line ->
            val cleaned = line.removePrefix("ingrédients").removePrefix("ingredients")
                .trimStart { it == ':' || it == ' ' }
            if (cleaned.count { it == ',' } >= 2) {
                cleaned.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                listOf(cleaned.ifEmpty { line })
            }
        }.filter { it.isNotEmpty() }

        if (lines.isEmpty() || analysisBlock.isBlank()) return null

        return CompositionBilan(
            ingredientLines = lines,
            compositionAnalysis = analysisBlock,
            disclaimer = disclaimer
        )
    }
}
