package com.foodgpt.analysis.ingredientsegment

import com.foodgpt.analysis.ingredientsegment.fixtures.OcrFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientSegmentExtractionAcceptanceTest {

    private val service = IngredientSegmentPreparationService()

    @Test
    fun `extracts only ingredient line from noisy text`() {
        val result = service.prepare(scanId = "scan-1", ocrText = OcrFixtures.NOMINAL_MULTI_LINE.trimIndent())

        assertTrue(result.anchorFound)
        assertEquals(IngredientSegmentBoundaryEndReason.LINE_END, result.boundaryEndReason)
        assertEquals("ingredients: sucre, farine, sel", result.segmentText)
    }
}
