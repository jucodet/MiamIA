package com.miamia.composition

/**
 * Extrait [CompositionBilan] depuis une sortie texte du modèle (sections balisées).
 * Tolère les variations de formatage courantes (espaces, `#` multiples, deux-points).
 */
object GemmaBilanParser {

    private val LISTE_PATTERN = Regex("""#{1,4}\s*LISTE\s*:?""", RegexOption.IGNORE_CASE)
    private val PRODUIT_PATTERN = Regex("""#{1,4}\s*PRODUIT\s*:?""", RegexOption.IGNORE_CASE)
    private val ANALYSE_PATTERN = Regex("""#{1,4}\s*ANALYSE\s*:?""", RegexOption.IGNORE_CASE)
    private val ADDITIFS_PATTERN = Regex("""#{1,4}\s*ADDITIFS[_\s]*RISQUE\s*:?""", RegexOption.IGNORE_CASE)
    private val IMPACT_SANTE_PATTERN = Regex("""#{1,4}\s*IMPACT[_\s]*SANT[EÉ]\s*:?""", RegexOption.IGNORE_CASE)

    private val VALID_LEVELS = setOf("VERT", "ORANGE", "ROUGE", "INCERTAIN")

    fun parse(modelOutput: String, disclaimer: String = CompositionMessages.DISCLAIMER_DEFAULT): CompositionBilan? {
        val trimmed = modelOutput.trim()
        if (trimmed.isEmpty()) return null

        val listMatch = LISTE_PATTERN.find(trimmed)
        val analysisMatch = ANALYSE_PATTERN.find(trimmed)

        if (listMatch == null || analysisMatch == null) return null
        if (analysisMatch.range.first <= listMatch.range.last) return null

        val produitMatch = PRODUIT_PATTERN.find(trimmed)

        val listEnd = listOfNotNull(
            produitMatch?.range?.first,
            analysisMatch.range.first
        ).filter { it > listMatch.range.last }.minOrNull() ?: analysisMatch.range.first

        val listBlock = trimmed.substring(listMatch.range.last + 1, listEnd).trim()

        val produitRaw = if (produitMatch != null &&
            produitMatch.range.first > listMatch.range.last &&
            produitMatch.range.first < analysisMatch.range.first
        ) {
            val produitEnd = analysisMatch.range.first
            trimmed.substring(produitMatch.range.last + 1, produitEnd).trim().lines()
                .firstOrNull { it.isNotBlank() }?.trim()
        } else {
            null
        }
        val (identifiedProduct, productConfidence) = parseProductLine(produitRaw)

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

        val healthImpacts = parseHealthImpacts(trimmed)

        return CompositionBilan(
            ingredientLines = lines,
            identifiedProduct = identifiedProduct,
            productConfidence = productConfidence,
            compositionAnalysis = analysisBlock,
            disclaimer = disclaimer,
            healthImpacts = healthImpacts,
        )
    }

    internal fun parseProductLine(raw: String?): Pair<String?, Int?> {
        if (raw.isNullOrBlank()) return null to null
        val parts = raw.split("|", limit = 2)
        val name = parts[0].trim().ifBlank { null }
        val confidence = parts.getOrNull(1)?.trim()?.toIntOrNull()?.coerceIn(0, 100)
        return name to confidence
    }

    private fun parseHealthImpacts(fullText: String): List<IngredientHealthImpact> {
        val impactMatch = IMPACT_SANTE_PATTERN.find(fullText) ?: return emptyList()
        val blockStart = impactMatch.range.last + 1
        if (blockStart >= fullText.length) return emptyList()
        val block = fullText.substring(blockStart).trim()

        return block.lines()
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
                IngredientHealthImpact(level = level, ingredient = ingredient, note = note)
            }
    }
}
