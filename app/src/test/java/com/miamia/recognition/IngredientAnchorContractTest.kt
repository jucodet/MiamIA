package com.miamia.recognition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientAnchorContractTest {
    private val pipeline = IngredientExtractionPipeline()

    @Test
    fun contract_fields_are_populated_for_anchor_output() {
        val out = pipeline.detectAnchors("scan-contract", IngredientAnchorFixtures.INTRO_THEN_LIST_WITH_SPACE.trimIndent())
        assertEquals("scan-contract", out.scanId)
        assertEquals("FIRST_LINE_START_ANCHOR", out.selectionRule)
        assertTrue(out.candidates.isNotEmpty())
    }

    @Test
    fun first_canonical_anchor_is_selected() {
        val out = pipeline.detectAnchors("scan-multi", IngredientAnchorFixtures.MULTIPLE_ANCHORS.trimIndent())
        val first = out.candidates.minByOrNull { it.startIndex }!!.startIndex
        assertEquals(first, out.selectedStartIndex)
    }
}
