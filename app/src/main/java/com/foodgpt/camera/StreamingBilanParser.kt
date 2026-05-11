package com.foodgpt.camera

/**
 * Parser progressif : extrait les sections au fur et à mesure du streaming.
 * Contrairement à [GemmaBilanParser] qui attend le texte complet,
 * celui-ci retourne un [StreamingBilanState.Streaming] partiel à chaque mise à jour.
 */
object StreamingBilanParser {

    private val LISTE_PATTERN = Regex("""#{1,4}\s*LISTE\s*:?""", RegexOption.IGNORE_CASE)
    private val ANALYSE_PATTERN = Regex("""#{1,4}\s*ANALYSE\s*:?""", RegexOption.IGNORE_CASE)

    fun parsePartial(partialText: String): StreamingBilanState.Streaming {
        if (partialText.isBlank()) {
            return StreamingBilanState.Streaming(partialText = partialText)
        }

        val listMatch = LISTE_PATTERN.find(partialText)
        if (listMatch == null) {
            return StreamingBilanState.Streaming(
                partialText = partialText,
                sectionReached = StreamingSection.NONE
            )
        }

        val analysisMatch = ANALYSE_PATTERN.find(partialText)

        val ingredients: List<String>
        val analysis: String?
        val section: StreamingSection

        if (analysisMatch != null && analysisMatch.range.first > listMatch.range.last) {
            val listBlock = partialText.substring(listMatch.range.last + 1, analysisMatch.range.first).trim()
            ingredients = extractLines(listBlock)
            val afterAnalysis = partialText.substring(analysisMatch.range.last + 1).trim()
            analysis = afterAnalysis.ifBlank { null }
            section = StreamingSection.ANALYSE
        } else {
            val listBlock = partialText.substring(listMatch.range.last + 1).trim()
            ingredients = extractLines(listBlock)
            analysis = null
            section = StreamingSection.LISTE
        }

        return StreamingBilanState.Streaming(
            partialText = partialText,
            partialIngredients = ingredients,
            partialAnalysis = analysis,
            sectionReached = section
        )
    }

    private fun extractLines(block: String): List<String> =
        block.lines()
            .map { it.trim().removePrefix("-").removePrefix("*").removePrefix("•").trim() }
            .filter { it.isNotEmpty() }
}
