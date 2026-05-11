package com.foodgpt.analysis.ingredientsegment

import com.foodgpt.analysis.ingredientsegment.fixtures.OcrFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientSegmentPhraseBoundaryAcceptanceTest {

    private val service = IngredientSegmentPreparationService()

    @Test
    fun `US1 sentence terminator truncates at first period on line`() {
        val out = service.prepare("scan-sent", OcrFixtures.FR_WITH_SENTENCE_END.trimIndent())

        assertTrue(out.anchorFound)
        assertEquals(IngredientSegmentBoundaryEndReason.SENTENCE_TERMINATOR, out.boundaryEndReason)
        assertEquals("Ingrédients: sucre, sel.", out.segmentText)
    }

    @Test
    fun `US1 capture crosses newline when no terminator found`() {
        val out = service.prepare("scan-line", OcrFixtures.EN_LINE_END.trimIndent())

        assertTrue(out.anchorFound)
        assertEquals(IngredientSegmentBoundaryEndReason.TEXT_END, out.boundaryEndReason)
        assertEquals("Ingredients sugar, salt\nMay contain nuts", out.segmentText)
    }

    @Test
    fun `US1 text end for monoline without punctuation or newline`() {
        val out = service.prepare("scan-eof", OcrFixtures.EN_MONOLINE_NO_PUNCT)

        assertTrue(out.anchorFound)
        assertEquals(IngredientSegmentBoundaryEndReason.TEXT_END, out.boundaryEndReason)
        assertEquals("Ingredient sugar, salt and flour", out.segmentText)
    }

    @Test
    fun `US1 first line anchor wins when two languages present`() {
        val out = service.prepare("scan-bi", OcrFixtures.FR_THEN_EN_ANCHOR.trimIndent())

        assertTrue(out.anchorFound)
        assertEquals("Ingrédients: eau.", out.segmentText)
    }

    @Test
    fun `US1-2 internal dot in additive code does not end capture`() {
        val out = service.prepare("scan-dot-int", OcrFixtures.DOT_INTERNAL_ADDITIVE)

        assertTrue(out.anchorFound)
        assertEquals(IngredientSegmentBoundaryEndReason.TEXT_END, out.boundaryEndReason)
        assertEquals("Ingrédients: eau, colorant E.621, sucre, sel", out.segmentText)
    }

    @Test
    fun `US1-1 dot followed by space ends capture at first contextual dot`() {
        val out = service.prepare("scan-dot-sp", OcrFixtures.DOT_SPACE_END)

        assertTrue(out.anchorFound)
        assertEquals(IngredientSegmentBoundaryEndReason.SENTENCE_TERMINATOR, out.boundaryEndReason)
        assertEquals("Ingrédients: eau, sucre.", out.segmentText)
    }
}
