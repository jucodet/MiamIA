package com.miamia.additives

/**
 * Extrait les lignes additifs depuis la sortie modèle (bloc [MARKER_ADDITIVES]).
 * Format attendu par ligne : `NIVEAU|nom_additif|justification_courte`
 * avec NIVEAU ∈ {VERT, ORANGE, ROUGE, INCERTAIN}.
 */
object AdditiveKpiParser {

    const val MARKER_ADDITIVES = "###ADDITIFS_RISQUE"

    private val strongRiskTokens = listOf(
        "cancérogène",
        "cancerogene",
        "mutagène",
        "mutagene",
        "perturbateur endocrinien",
        "toxique",
        "cancérigène",
        "cancerigene",
    )

    fun parse(rawLlmText: String): AdditiveParseOutcome {
        val trimmed = rawLlmText.trim()
        if (trimmed.isEmpty()) {
            return AdditiveParseOutcome(
                items = emptyList(),
                warnings = listOf("Réponse modèle vide."),
            )
        }
        val lower = trimmed.lowercase()
        val markerIdx = lower.indexOf(MARKER_ADDITIVES.lowercase())
        if (markerIdx == -1) {
            return AdditiveParseOutcome(
                items = emptyList(),
                warnings = listOf("Section additifs absente ; aucun classement structuré extrait."),
            )
        }
        val blockStart = markerIdx + MARKER_ADDITIVES.length
        val afterMarker = trimmed.substring(blockStart).trim()
        val nextSection = Regex("^###[A-Z0-9_]+", RegexOption.MULTILINE)
            .find(afterMarker)?.range?.first
        val block = if (nextSection != null && nextSection > 0) {
            afterMarker.substring(0, nextSection).trim()
        } else {
            afterMarker.trim()
        }
        val rawLines = block.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("###") }

        val parsed = rawLines.mapNotNull { line -> parseLine(line) }
        val merged = mergeDuplicates(parsed)
        val ordered = merged.sortedWith(
            compareBy<AdditiveRiskItem> { it.level.sortKey }
                .thenBy { it.displayName.lowercase() },
        )
        val warnings = mutableListOf<String>()
        if (rawLines.isNotEmpty() && merged.isEmpty()) {
            warnings += "Lignes additifs présentes mais non lisibles (format attendu : NIVEAU|nom|justification)."
        }
        return AdditiveParseOutcome(items = ordered, warnings = warnings)
    }

    private fun parseLine(line: String): AdditiveRiskItem? {
        val parts = line.split('|', limit = 3).map { it.trim() }
        if (parts.size < 2) return null
        val levelToken = parts[0].uppercase()
        val name = parts.getOrNull(1).orEmpty()
        val justification = parts.getOrNull(2).orEmpty()
        if (name.isBlank()) return null

        val level = when (levelToken) {
            "ROUGE" -> AdditiveRiskLevel.HIGH
            "ORANGE" -> AdditiveRiskLevel.MEDIUM
            "VERT" -> AdditiveRiskLevel.LOW
            "INCERTAIN" -> AdditiveRiskLevel.UNKNOWN
            else -> return null
        }

        var confidence = when {
            justification.isBlank() -> AdditiveLineConfidence.NEEDS_CONFIRMATION
            else -> AdditiveLineConfidence.OK
        }

        if (confidence == AdditiveLineConfidence.OK &&
            level == AdditiveRiskLevel.LOW &&
            containsStrongRisk(justification)
        ) {
            confidence = AdditiveLineConfidence.INCOHERENT
        }

        val displayName = name
        val canonical = normalizeName(name)
        return AdditiveRiskItem(
            canonicalName = canonical,
            displayName = displayName,
            level = level,
            justification = justification,
            confidence = confidence,
        )
    }

    private fun containsStrongRisk(text: String): Boolean {
        val n = text.lowercase()
        return strongRiskTokens.any { n.contains(it) }
    }

    fun normalizeName(name: String): String =
        name.trim().lowercase().replace(Regex("\\s+"), " ")

    private fun worse(a: AdditiveRiskLevel, b: AdditiveRiskLevel): AdditiveRiskLevel =
        if (a.sortKey <= b.sortKey) a else b

    private fun mergeDuplicates(items: List<AdditiveRiskItem>): List<AdditiveRiskItem> {
        if (items.isEmpty()) return items
        val byKey = LinkedHashMap<String, AdditiveRiskItem>()
        for (item in items) {
            val existing = byKey[item.canonicalName]
            if (existing == null) {
                byKey[item.canonicalName] = item
            } else {
                val mergedLevel = worse(existing.level, item.level)
                val pickJust = when {
                    existing.justification.isNotBlank() && item.justification.isNotBlank() &&
                        existing.justification != item.justification ->
                        "${existing.justification} · ${item.justification}"
                    existing.justification.isNotBlank() -> existing.justification
                    else -> item.justification
                }
                val mergedConfidence = when {
                    existing.confidence == AdditiveLineConfidence.INCOHERENT ||
                        item.confidence == AdditiveLineConfidence.INCOHERENT ->
                        AdditiveLineConfidence.INCOHERENT
                    existing.confidence == AdditiveLineConfidence.NEEDS_CONFIRMATION ||
                        item.confidence == AdditiveLineConfidence.NEEDS_CONFIRMATION ->
                        AdditiveLineConfidence.NEEDS_CONFIRMATION
                    else -> AdditiveLineConfidence.DUPLICATE_MERGED
                }
                byKey[item.canonicalName] = AdditiveRiskItem(
                    canonicalName = item.canonicalName,
                    displayName = if (existing.displayName.length >= item.displayName.length) {
                        existing.displayName
                    } else {
                        item.displayName
                    },
                    level = mergedLevel,
                    justification = pickJust,
                    confidence = mergedConfidence,
                )
            }
        }
        return byKey.values.toList()
    }
}

data class AdditiveParseOutcome(
    val items: List<AdditiveRiskItem>,
    val warnings: List<String>,
)
