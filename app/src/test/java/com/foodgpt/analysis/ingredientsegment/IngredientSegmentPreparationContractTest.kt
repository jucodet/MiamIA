package com.foodgpt.analysis.ingredientsegment

import com.foodgpt.analysis.ingredientsegment.fixtures.OcrFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientSegmentPreparationContractTest {

    private val service = IngredientSegmentPreparationService()

    @Test
    fun `returns expected contract fields when anchor exists`() {
        val out = service.prepare("scan-contract", OcrFixtures.NOMINAL_MULTI_LINE.trimIndent())

        assertEquals("scan-contract", out.scanId)
        assertTrue(out.anchorFound)
        assertNotNull(out.anchorIndex)
        assertNotNull(out.endIndex)
        assertEquals(IngredientSegmentBoundaryEndReason.LINE_END, out.boundaryEndReason)
        assertEquals("ingredients: sucre, farine, sel", out.segmentText)
    }
}
