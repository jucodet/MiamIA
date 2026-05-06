package com.foodgpt.recognition

import com.foodgpt.analysis.ingredientsegment.IngredientSegmentPreparationService

class IngredientExtractionPipeline(
    private val segmentPrep: IngredientSegmentPreparationService = IngredientSegmentPreparationService()
) {
    private val lineStartAnchorRegex =
        Regex("""(?m)(^\s*)(?i)(ingr[ée]dients|ingr[ée]dient|ingredients|ingredient)\b""")

    fun detectAnchors(scanId: String, rawText: String): IngredientAnchorDetectionResult {
        val candidates = lineStartAnchorRegex.findAll(rawText).map { match ->
            val g2 = match.groups[2]!!
            IngredientAnchorCandidate(
                startIndex = g2.range.first,
                rawMatch = g2.value,
                isCanonical = true
            )
        }.toList()
        val selected = candidates.minByOrNull { it.startIndex }
        return IngredientAnchorDetectionResult(
            scanId = scanId,
            candidates = candidates,
            selectionRule = "FIRST_LINE_START_ANCHOR",
            selectedStartIndex = selected?.startIndex,
            anchorFound = selected != null,
            blockedReason = if (selected == null) "NO_CANONICAL_ANCHOR" else "NONE"
        )
    }

    fun extractOrderedItems(rawText: String, confidence: Float): List<IngredientRecognitionItem> {
        val prep = segmentPrep.prepare("extract-$confidence", rawText)
        val segment = prep.segmentText?.trim().orEmpty()
        val listPart = if (segment.isNotEmpty()) {
            extractListPayload(segment)
        } else {
            rawText
        }
        return listPart
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

    private fun extractListPayload(segment: String): String {
        val afterColon = segment.substringAfter(":", "").trim()
        if (afterColon.isNotEmpty()) return afterColon
        return segment.replaceFirst(
            Regex("""(?i)^\s*(ingr[ée]dients|ingr[ée]dient|ingredients|ingredient)\b\s*:?\s*"""),
            ""
        ).trim()
    }
}
