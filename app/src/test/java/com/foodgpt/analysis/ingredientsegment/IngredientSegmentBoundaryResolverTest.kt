package com.foodgpt.analysis.ingredientsegment

import com.foodgpt.analysis.ingredientsegment.fixtures.OcrFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientSegmentBoundaryResolverTest {

    private val resolver = IngredientSegmentBoundaryResolver()
    private val service = IngredientSegmentPreparationService()

    @Test
    fun `resolver ends at sentence terminator before newline`() {
        val text = "Ingrédients: a, b.\nSuite"
        val anchor = text.indexOf("Ingrédients")
        val out = resolver.resolveEnd(text, anchor)

        assertEquals(IngredientSegmentBoundaryEndReason.SENTENCE_TERMINATOR, out.boundaryEndReason)
        assertEquals("Ingrédients: a, b.", text.substring(anchor, out.endIndexExclusive))
    }

    @Test
    fun `resolver ends at line end when no terminal punctuation`() {
        val text = "Ingredients x, y\nNext line"
        val anchor = text.indexOf("Ingredients")
        val out = resolver.resolveEnd(text, anchor)

        assertEquals(IngredientSegmentBoundaryEndReason.LINE_END, out.boundaryEndReason)
        assertEquals("Ingredients x, y", text.substring(anchor, out.endIndexExclusive))
    }

    @Test
    fun `resolver ends at eof for single line without punctuation`() {
        val text = "Ingredient a, b, c"
        val anchor = text.indexOf("Ingredient")
        val out = resolver.resolveEnd(text, anchor)

        assertEquals(IngredientSegmentBoundaryEndReason.TEXT_END, out.boundaryEndReason)
        assertEquals(text.length, out.endIndexExclusive)
    }

    @Test
    fun `service uses text end when newline is absent after anchor`() {
        val out = service.prepare("scan-eof", OcrFixtures.NO_NEWLINE_AFTER_ANCHOR)

        assertEquals(IngredientSegmentBoundaryEndReason.TEXT_END, out.boundaryEndReason)
        assertEquals("Ingredients: sucre, farine, sel, huile", out.segmentText)
    }
}
