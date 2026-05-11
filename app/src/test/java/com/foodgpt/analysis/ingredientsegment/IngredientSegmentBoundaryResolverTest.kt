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

    // --- BC-01: dot + space = SENTENCE_TERMINATOR ---
    @Test
    fun `BC-01 dot followed by space is sentence terminator`() {
        val text = OcrFixtures.DOT_SPACE_END
        val anchor = text.indexOf("Ingrédients")
        val out = resolver.resolveEnd(text, anchor)

        assertEquals(IngredientSegmentBoundaryEndReason.SENTENCE_TERMINATOR, out.boundaryEndReason)
        assertEquals("Ingrédients: eau, sucre.", text.substring(anchor, out.endIndexExclusive))
    }

    // --- BC-02: dot internal additive (E.621) → not a terminator ---
    @Test
    fun `BC-02 dot inside additive code is not a terminator`() {
        val text = OcrFixtures.DOT_INTERNAL_ADDITIVE
        val anchor = text.indexOf("Ingrédients")
        val out = resolver.resolveEnd(text, anchor)

        assertEquals(IngredientSegmentBoundaryEndReason.TEXT_END, out.boundaryEndReason)
        assertEquals(text.length, out.endIndexExclusive)
    }

    // --- BC-03: dot internal abbreviation (vit.B12) → not a terminator, LINE_END ---
    @Test
    fun `BC-03 dot inside abbreviation is not a terminator`() {
        val text = OcrFixtures.DOT_INTERNAL_ABBREVIATION
        val anchor = text.indexOf("Ingredients")
        val out = resolver.resolveEnd(text, anchor)

        assertEquals(IngredientSegmentBoundaryEndReason.LINE_END, out.boundaryEndReason)
        assertEquals("Ingredients: vit.B12, iron, zinc", text.substring(anchor, out.endIndexExclusive))
    }

    // --- BC-04: dot at EOF = SENTENCE_TERMINATOR ---
    @Test
    fun `BC-04 dot at end of text is sentence terminator`() {
        val text = OcrFixtures.DOT_EOF_END
        val anchor = text.indexOf("Ingrédients")
        val out = resolver.resolveEnd(text, anchor)

        assertEquals(IngredientSegmentBoundaryEndReason.SENTENCE_TERMINATOR, out.boundaryEndReason)
        assertEquals("Ingrédients: eau, sucre.", text.substring(anchor, out.endIndexExclusive))
    }

    // --- BC-07: dot + newline = SENTENCE_TERMINATOR ---
    @Test
    fun `BC-07 dot followed by newline is sentence terminator`() {
        val text = OcrFixtures.DOT_NEWLINE_END
        val anchor = text.indexOf("Ingrédients")
        val out = resolver.resolveEnd(text, anchor)

        assertEquals(IngredientSegmentBoundaryEndReason.SENTENCE_TERMINATOR, out.boundaryEndReason)
        assertEquals("Ingrédients: eau, sucre.", text.substring(anchor, out.endIndexExclusive))
    }
}
