package com.foodgpt.recognition

class IngredientExtractionPipeline {
    private val canonicalAnchorRegex = Regex("ingr[ée]dients?\\s*:", RegexOption.IGNORE_CASE)

    fun detectAnchors(scanId: String, rawText: String): IngredientAnchorDetectionResult {
        val candidates = canonicalAnchorRegex.findAll(rawText).map {
            IngredientAnchorCandidate(
                startIndex = it.range.first,
                rawMatch = it.value,
                isCanonical = true
            )
        }.toList()
        val selected = candidates.minByOrNull { it.startIndex }
        return IngredientAnchorDetectionResult(
            scanId = scanId,
            candidates = candidates,
            selectionRule = "FIRST_CANONICAL_MATCH",
            selectedStartIndex = selected?.startIndex,
            anchorFound = selected != null,
            blockedReason = if (selected == null) "NO_CANONICAL_ANCHOR" else "NONE"
        )
    }

    fun extractOrderedItems(rawText: String, confidence: Float): List<IngredientRecognitionItem> {
        val selectedStart = canonicalAnchorRegex.find(rawText)?.range?.first
        val anchoredText = if (selectedStart != null) rawText.substring(selectedStart) else rawText
        return anchoredText
            .substringAfter(":", anchoredText)
            .split(",", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .mapIndexed { index, token ->
                IngredientRecognitionItem(
                    position = index,
                    rawText = token,
                    normalizedText = token.lowercase(),
                    confidence = confidence,
                    languageTag = "auto",
                    isAllergenMarked = token.any { it.isUpperCase() } || token.contains("ble", ignoreCase = true)
                )
            }
    }
}
