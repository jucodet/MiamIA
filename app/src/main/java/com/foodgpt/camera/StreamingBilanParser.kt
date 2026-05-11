package com.foodgpt.camera

/**
 * Parser progressif : extrait les sections au fur et à mesure du streaming.
 * Contrairement à [GemmaBilanParser] qui attend le texte complet,
 * celui-ci retourne un [StreamingBilanState.Streaming] partiel à chaque mise à jour.
 */
object StreamingBilanParser {

    private val LISTE_PATTERN = Regex("""#{1,4}\s*LISTE\s*:?""", RegexOption.IGNORE_CASE)
    private val ANALYSE_PATTERN = Regex("""#{1,4}\s*ANALYSE\s*:?""", RegexOption.IGNORE_CASE)
    private val ADDITIFS_PATTERN = Regex("""#{1,4}\s*ADDITIFS[_\s]*RISQUE\s*:?""", RegexOption.IGNORE_CASE)
    private val IMPACT_PATTERN = Regex("""#{1,4}\s*IMPACT[_\s]*SANT[EÉ]\s*:?""", RegexOption.IGNORE_CASE)

    private val VALID_LEVELS = setOf("VERT", "ORANGE", "ROUGE", "INCERTAIN")

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
        val additivesMatch = ADDITIFS_PATTERN.find(partialText)
        val impactMatch = IMPACT_PATTERN.find(partialText)

        val ingredients: List<String>
        val analysis: String?
        val healthImpacts: List<StreamingHealthImpact>
        val section: StreamingSection

        if (analysisMatch != null && analysisMatch.range.first > listMatch.range.last) {
            val listBlock = partialText.substring(listMatch.range.last + 1, analysisMatch.range.first).trim()
            ingredients = extractLines(listBlock)

            val analysisEnd = findNextSectionStart(partialText, analysisMatch.range.last + 1, additivesMatch, impactMatch)
            val analysisBlock = partialText.substring(analysisMatch.range.last + 1, analysisEnd).trim()
            analysis = analysisBlock.ifBlank { null }

            if (impactMatch != null && impactMatch.range.first > analysisMatch.range.last) {
                val impactBlock = partialText.substring(impactMatch.range.last + 1).trim()
                healthImpacts = parseImpactLines(impactBlock)
                section = StreamingSection.IMPACT_SANTE
            } else if (additivesMatch != null && additivesMatch.range.first > analysisMatch.range.last) {
                healthImpacts = emptyList()
                section = StreamingSection.ANALYSE
            } else {
                healthImpacts = emptyList()
                section = StreamingSection.ANALYSE
            }
        } else {
            val listBlock = partialText.substring(listMatch.range.last + 1).trim()
            ingredients = extractLines(listBlock)
            analysis = null
            healthImpacts = emptyList()
            section = StreamingSection.LISTE
        }

        return StreamingBilanState.Streaming(
            partialText = partialText,
            partialIngredients = ingredients,
            partialAnalysis = analysis,
            partialHealthImpacts = healthImpacts,
            sectionReached = section
        )
    }

    private fun findNextSectionStart(
        text: String,
        afterIndex: Int,
        additivesMatch: MatchResult?,
        impactMatch: MatchResult?
    ): Int {
        val candidates = listOfNotNull(
            additivesMatch?.range?.first,
            impactMatch?.range?.first
        ).filter { it > afterIndex }
        return candidates.minOrNull() ?: text.length
    }

    private fun parseImpactLines(block: String): List<StreamingHealthImpact> =
        block.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val parts = line.split("|", limit = 3)
                if (parts.size < 3) return@mapNotNull null
                val level = parts[0].trim().uppercase()
                if (level !in VALID_LEVELS) return@mapNotNull null
                val ingredient = parts[1].trim()
                val note = parts[2].trim()
                if (ingredient.isEmpty()) return@mapNotNull null
                StreamingHealthImpact(level = level, ingredient = ingredient, note = note)
            }

    private fun extractLines(block: String): List<String> =
        block.lines()
            .map { it.trim().removePrefix("-").removePrefix("*").removePrefix("•").trim() }
            .filter { it.isNotEmpty() }
}
