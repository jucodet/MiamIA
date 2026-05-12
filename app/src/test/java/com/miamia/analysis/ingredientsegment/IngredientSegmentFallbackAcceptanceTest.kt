package com.miamia.analysis.ingredientsegment

import com.miamia.analysis.ingredientsegment.fixtures.OcrFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class IngredientSegmentFallbackAcceptanceTest {

    private val service = IngredientSegmentPreparationService()

    @Test
    fun `blocks when ingredient anchor is absent`() {
        val out = service.prepare("scan-no-anchor", OcrFixtures.NO_ANCHOR.trimIndent())

        assertFalse(out.anchorFound)
        assertEquals(IngredientSegmentFallbackMode.ANCHOR_MISSING_BLOCKED, out.fallbackMode)
        assertEquals(IngredientSegmentBoundaryEndReason.NONE, out.boundaryEndReason)
    }
}
