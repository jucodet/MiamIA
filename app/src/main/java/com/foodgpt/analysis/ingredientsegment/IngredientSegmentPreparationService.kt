package com.foodgpt.analysis.ingredientsegment

class IngredientSegmentPreparationService(
    private val normalizer: IngredientAnchorNormalizer = IngredientAnchorNormalizer(),
    private val boundaryResolver: IngredientSegmentBoundaryResolver = IngredientSegmentBoundaryResolver()
) {
    private val canonicalAnchorRegex = Regex("ingr[ée]dients?\\s*:", RegexOption.IGNORE_CASE)

    fun prepare(scanId: String, ocrText: String): IngredientSegmentExtraction {
        val anchorIndex = canonicalAnchorRegex.find(ocrText)?.range?.first
            ?: normalizer.findFirstAnchorIndex(ocrText)
            ?: return IngredientSegmentExtraction(
                scanId = scanId,
                anchorFound = false,
                anchorIndex = null,
                endIndex = null,
                segmentText = null,
                fallbackMode = IngredientSegmentFallbackMode.ANCHOR_MISSING_BLOCKED
            )

        val boundary = boundaryResolver.resolveEnd(ocrText, anchorIndex)
        val segment = ocrText.substring(anchorIndex, boundary.endIndexExclusive).trim()
        return IngredientSegmentExtraction(
            scanId = scanId,
            anchorFound = true,
            anchorIndex = anchorIndex,
            endIndex = boundary.endIndexExclusive,
            segmentText = segment,
            fallbackMode = boundary.fallbackMode
        )
    }
}
