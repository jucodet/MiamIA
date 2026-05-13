package com.miamia.camera

import com.miamia.composition.GemmaBilanParser

/**
 * Parser progressif : extrait les sections au fur et à mesure du streaming.
 * Contrairement à [GemmaBilanParser] qui attend le texte complet,
 * celui-ci retourne un [StreamingBilanState.Streaming] partiel à chaque mise à jour.
 */
object StreamingBilanParser {

    private val LISTE_PATTERN = Regex("""#{1,4}\s*LISTE\s*:?""", RegexOption.IGNORE_CASE)
    private val PRODUIT_PATTERN = Regex("""#{1,4}\s*PRODUIT\s*:?""", RegexOption.IGNORE_CASE)
    private val ANALYSE_PATTERN = Regex("""#{1,4}\s*(?:ANALYSE|ANALYSIS)\s*:?""", RegexOption.IGNORE_CASE)
    private val ADDITIFS_PATTERN = Regex("""#{1,4}\s*ADDITIFS[_\s]*RISQUE\s*:?""", RegexOption.IGNORE_CASE)
    private val ENERGIE_PATTERN = Regex("""#{1,4}\s*ENERGIE(?:_ESTIMEE)?\s*:?""", RegexOption.IGNORE_CASE)
    private val IMPACT_PATTERN = Regex("""#{1,4}\s*IMPACT[_\s]*SANT[EÉ]\s*:?""", RegexOption.IGNORE_CASE)

    private val VALID_LEVELS = setOf("VERT", "ORANGE", "ROUGE", "INCERTAIN")

    fun parsePartial(partialText: String): StreamingBilanState.Streaming {
        val normalizedPartial = GemmaBilanParser.prepareCompositionRawOutput(partialText)
        if (normalizedPartial.isBlank()) {
            return StreamingBilanState.Streaming(partialText = partialText)
        }

        val listMatch = LISTE_PATTERN.find(normalizedPartial)
        if (listMatch == null) {
            return StreamingBilanState.Streaming(
                partialText = partialText,
                sectionReached = StreamingSection.NONE
            )
        }

        val produitMatch = PRODUIT_PATTERN.find(normalizedPartial)
        val analysisMatch = ANALYSE_PATTERN.find(normalizedPartial)
        val additivesMatch = ADDITIFS_PATTERN.find(normalizedPartial)
        val energyMatch = ENERGIE_PATTERN.find(normalizedPartial)
        val impactMatch = IMPACT_PATTERN.find(normalizedPartial)

        val ingredients: List<String>
        val product: String?
        val productConfidence: Int?
        val analysis: String?
        val healthImpacts: List<StreamingHealthImpact>
        val section: StreamingSection

        if (analysisMatch != null && analysisMatch.range.first > listMatch.range.last) {
            val listEnd = listOfNotNull(
                produitMatch?.range?.first,
                analysisMatch.range.first
            ).filter { it > listMatch.range.last }.min()
            val listBlock = normalizedPartial.substring(listMatch.range.last + 1, listEnd).trim()
            ingredients = extractLines(listBlock)

            val produitRaw = if (produitMatch != null &&
                produitMatch.range.first > listMatch.range.last &&
                produitMatch.range.first < analysisMatch.range.first
            ) {
                val produitEnd = analysisMatch.range.first
                normalizedPartial.substring(produitMatch.range.last + 1, produitEnd).trim().lines()
                    .firstOrNull { it.isNotBlank() }?.trim()
            } else {
                null
            }
            val parsed = GemmaBilanParser.parseProductLine(produitRaw)
            product = parsed.first
            productConfidence = parsed.second

            val analysisEnd = findNextSectionStart(
                normalizedPartial,
                analysisMatch.range.last + 1,
                energyMatch,
                additivesMatch,
                impactMatch,
            )
            val analysisBlock = normalizedPartial.substring(analysisMatch.range.last + 1, analysisEnd).trim()
            analysis = analysisBlock.ifBlank { null }

            if (impactMatch != null && impactMatch.range.first > analysisMatch.range.last) {
                val impactBlock = normalizedPartial.substring(impactMatch.range.last + 1).trim()
                healthImpacts = parseImpactLines(impactBlock)
                section = StreamingSection.IMPACT_SANTE
            } else if (additivesMatch != null && additivesMatch.range.first > analysisMatch.range.last) {
                healthImpacts = emptyList()
                section = StreamingSection.ANALYSE
            } else {
                healthImpacts = emptyList()
                section = StreamingSection.ANALYSE
            }
        } else if (produitMatch != null && produitMatch.range.first > listMatch.range.last) {
            val listBlock = normalizedPartial.substring(listMatch.range.last + 1, produitMatch.range.first).trim()
            ingredients = extractLines(listBlock)
            val produitBlock = normalizedPartial.substring(produitMatch.range.last + 1).trim()
            val produitRaw = produitBlock.lines().firstOrNull { it.isNotBlank() }?.trim()
            val parsed = GemmaBilanParser.parseProductLine(produitRaw)
            product = parsed.first
            productConfidence = parsed.second
            analysis = null
            healthImpacts = emptyList()
            section = StreamingSection.PRODUIT
        } else {
            val listBlock = normalizedPartial.substring(listMatch.range.last + 1).trim()
            ingredients = extractLines(listBlock)
            product = null
            productConfidence = null
            analysis = null
            healthImpacts = emptyList()
            section = StreamingSection.LISTE
        }

        return StreamingBilanState.Streaming(
            partialText = partialText,
            partialIngredients = ingredients,
            partialProduct = product,
            partialProductConfidence = productConfidence,
            partialAnalysis = analysis,
            partialHealthImpacts = healthImpacts,
            sectionReached = section
        )
    }

    private fun findNextSectionStart(
        text: String,
        afterIndex: Int,
        energyMatch: MatchResult?,
        additivesMatch: MatchResult?,
        impactMatch: MatchResult?,
    ): Int {
        val addFirst = additivesMatch?.range?.first ?: Int.MAX_VALUE
        val orderedEnergy = energyMatch?.takeIf { m ->
            m.range.first > afterIndex && m.range.first < addFirst
        }
        val candidates = listOfNotNull(
            orderedEnergy?.range?.first,
            additivesMatch?.range?.first,
            impactMatch?.range?.first,
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
