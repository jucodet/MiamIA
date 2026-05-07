package com.foodgpt.analysis.ingredientsegment

import com.foodgpt.analysis.ingredientsegment.fixtures.OcrFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientSegmentPreparationServiceTest {

    private val service = IngredientSegmentPreparationService()

    @Test
    fun `uses first anchor when multiple anchors exist`() {
        val out = service.prepare("scan-multi", OcrFixtures.MULTIPLE_ANCHORS.trimIndent())

        assertEquals("ingredients: eau", out.segmentText)
    }

    @Test
    fun `returns anchor missing when no line start anchor`() {
        val out = service.prepare("scan-none", OcrFixtures.NO_ANCHOR.trimIndent())

        assertEquals(IngredientSegmentFallbackMode.ANCHOR_MISSING_BLOCKED, out.fallbackMode)
        assertEquals(false, out.anchorFound)
    }
}
