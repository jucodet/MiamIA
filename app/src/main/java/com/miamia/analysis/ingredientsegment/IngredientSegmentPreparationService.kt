package com.miamia.analysis.ingredientsegment

class IngredientSegmentPreparationService(
    private val normalizer: IngredientAnchorNormalizer = IngredientAnchorNormalizer(),
    private val boundaryResolver: IngredientSegmentBoundaryResolver = IngredientSegmentBoundaryResolver()
) {

    fun prepare(scanId: String, ocrText: String): IngredientSegmentExtraction {
        val anchorIndex = normalizer.findFirstPhraseAnchorIndex(ocrText)
            ?: return IngredientSegmentExtraction(
                scanId = scanId,
                anchorFound = false,
                anchorIndex = null,
                endIndex = null,
                segmentText = null,
                fallbackMode = IngredientSegmentFallbackMode.ANCHOR_MISSING_BLOCKED,
                boundaryEndReason = IngredientSegmentBoundaryEndReason.NONE
            )

        val boundary = boundaryResolver.resolveEnd(ocrText, anchorIndex)
        val segment = ocrText.substring(anchorIndex, boundary.endIndexExclusive).trim()
        return IngredientSegmentExtraction(
            scanId = scanId,
            anchorFound = true,
            anchorIndex = anchorIndex,
            endIndex = boundary.endIndexExclusive,
            segmentText = segment,
            fallbackMode = IngredientSegmentFallbackMode.NONE,
            boundaryEndReason = boundary.boundaryEndReason
        )
    }
}
