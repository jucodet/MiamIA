package com.miamia.recognition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientAnchorDetectionTest {
    private val pipeline = IngredientExtractionPipeline()

    @Test
    fun detects_anchor_on_real_list_not_intro_sentence() {
        val out = pipeline.detectAnchors("scan-1", IngredientAnchorFixtures.INTRO_THEN_LIST.trimIndent())
        assertTrue(out.anchorFound)
        val idx = out.selectedStartIndex!!
        assertEquals("ingredients:", IngredientAnchorFixtures.INTRO_THEN_LIST.trimIndent().substring(idx, idx + "ingredients:".length))
    }

    @Test
    fun supports_space_before_colon_variant() {
        val out = pipeline.detectAnchors("scan-2", IngredientAnchorFixtures.INTRO_THEN_LIST_WITH_SPACE.trimIndent())
        assertTrue(out.anchorFound)
        val source = IngredientAnchorFixtures.INTRO_THEN_LIST_WITH_SPACE.trimIndent()
        assertTrue(source.substring(out.selectedStartIndex!!).startsWith("ingredients :"))
    }

    @Test
    fun blocks_when_no_canonical_anchor() {
        val out = pipeline.detectAnchors("scan-3", IngredientAnchorFixtures.NO_CANONICAL_ANCHOR.trimIndent())
        assertFalse(out.anchorFound)
        assertEquals("NO_CANONICAL_ANCHOR", out.blockedReason)
    }
}
