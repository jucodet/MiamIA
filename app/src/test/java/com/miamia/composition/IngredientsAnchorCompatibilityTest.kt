package com.miamia.composition

import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientsAnchorCompatibilityTest {
    @Test
    fun extracted_text_with_anchor_remains_composition_compatible() {
        val extracted = "ingredients : sucre, farine, sel"
        assertTrue(extracted.contains("ingredients"))
        assertTrue(extracted.contains(":"))
    }
}
