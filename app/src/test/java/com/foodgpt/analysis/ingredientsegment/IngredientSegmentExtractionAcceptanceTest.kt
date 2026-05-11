package com.foodgpt.analysis.ingredientsegment

import com.foodgpt.analysis.ingredientsegment.fixtures.OcrFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientSegmentExtractionAcceptanceTest {

    private val service = IngredientSegmentPreparationService()

    @Test
    fun `captures from anchor to end of text when no terminator`() {
        val result = service.prepare(scanId = "scan-1", ocrText = OcrFixtures.NOMINAL_MULTI_LINE.trimIndent())

        assertTrue(result.anchorFound)
        assertEquals(IngredientSegmentBoundaryEndReason.TEXT_END, result.boundaryEndReason)
        assertEquals("ingredients: sucre, farine, sel\ntraces possibles d arachides", result.segmentText)
    }
}
