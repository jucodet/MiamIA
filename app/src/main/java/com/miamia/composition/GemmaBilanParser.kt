package com.miamia.composition

/**
 * Extrait [CompositionBilan] depuis une sortie texte du modèle (sections balisées).
 * Tolère les variations de formatage courantes (espaces, `#` multiples, deux-points).
 */
object GemmaBilanParser {

    private val LISTE_PATTERN = Regex("""#{1,4}\s*LISTE\s*:?""", RegexOption.IGNORE_CASE)
    private val PRODUIT_PATTERN = Regex("""#{1,4}\s*PRODUIT\s*:?""", RegexOption.IGNORE_CASE)
    /** Tolère « ANALYSIS » (mélange EN) et fautes de frappe courantes sur le marqueur. */
    private val ANALYSE_PATTERN = Regex("""#{1,4}\s*(?:ANALYSE|ANALYSIS)\s*:?""", RegexOption.IGNORE_CASE)
    private val ADDITIFS_PATTERN = Regex("""#{1,4}\s*ADDITIFS[_\s]*RISQUE\s*:?""", RegexOption.IGNORE_CASE)
    private val IMPACT_SANTE_PATTERN = Regex("""#{1,4}\s*IMPACT[_\s]*SANT[EÉ]\s*:?""", RegexOption.IGNORE_CASE)

    private val VALID_LEVELS = setOf("VERT", "ORANGE", "ROUGE", "INCERTAIN")

    /**
     * Nettoie / répare une sortie modèle tronquée ou mal fermée avant [parse]
     * (ex. `##]` en fin de flux, section analyse vide avant additifs).
     */
    internal fun prepareCompositionRawOutput(raw: String): String {
        var t = raw.trim()
        if (t.isEmpty()) return t
        t = patchEmptyAnalysisBeforeAdditifs(t)
        t = stripTrailingGenerationArtifacts(t)
        return t.trim()
    }

    private fun stripTrailingGenerationArtifacts(text: String): String {
        var t = text.trimEnd()
        while (true) {
            val next = t.replace(Regex("""(?s)\s*##\]\s*$"""), "")
                .replace(Regex("""(?s)\s*##\s*$"""), "")
                .trimEnd()
            if (next == t) break
            t = next
        }
        return t
    }

    /**
     * Si le modèle enchaîne `###ANALYSE` → `###ADDITIFS_RISQUE` sans texte, [parse] échouait
     * ([compositionAnalysis] vide). On insère une phrase de repli minimaliste.
     */
    private fun patchEmptyAnalysisBeforeAdditifs(text: String): String {
        val analyseMatch = ANALYSE_PATTERN.find(text) ?: return text
        val headEnd = analyseMatch.range.last + 1
        if (headEnd >= text.length) return text
        val tail = text.substring(headEnd)
        val addMatch = ADDITIFS_PATTERN.find(tail) ?: return text
        val between = tail.substring(0, addMatch.range.first).trim()
        if (between.isNotEmpty()) return text
        val placeholder =
            "Synthèse indisponible (réponse du modèle incomplète après la section analyse)."
        return text.take(headEnd) + "\n" + placeholder + "\n" + text.substring(headEnd)
    }

    private fun stripAnalysisNoise(text: String): String =
        text.trim()
            .replace(Regex("""(?s)\s*##\]\s*$"""), "")
            .replace(Regex("""(?s)\s*##\s*$"""), "")
            .trim()

    fun parse(modelOutput: String, disclaimer: String = CompositionMessages.DISCLAIMER_DEFAULT): CompositionBilan? {
        val trimmed = prepareCompositionRawOutput(modelOutput)
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
        val analysisBlockRaw = if (additivesMatch == null) {
            afterAnalysisStart.trim()
        } else {
            afterAnalysisStart.substring(0, additivesMatch.range.first).trim()
        }
        val analysisBlock = stripAnalysisNoise(analysisBlockRaw)

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
