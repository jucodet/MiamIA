package com.miamia.analysis.ingredientsegment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AnalysisSubmissionGateContractTest {

    private val gate = AnalysisSubmissionGate()

    @Test
    fun `blocks when full ocr empty even if anchor missing`() {
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-1",
            anchorFound = false,
            anchorIndex = null,
            endIndex = null,
            segmentText = null,
            fallbackMode = IngredientSegmentFallbackMode.ANCHOR_MISSING_BLOCKED,
        )

        val out = gate.evaluate("scan-1", extraction, userConfirmed = true, fullOcrTranscript = "   ")

        assertFalse(out.submissionAllowed)
        assertEquals(SubmissionBlockedReason.EMPTY_SEGMENT, out.blockedReason)
    }

    @Test
    fun `allows when anchor missing but full ocr has content and user confirmed`() {
        val full = "Nutrition facts\nPer 100g\nEnergy 2000kJ"
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-no-anchor",
            anchorFound = false,
            anchorIndex = null,
            endIndex = null,
            segmentText = null,
            fallbackMode = IngredientSegmentFallbackMode.ANCHOR_MISSING_BLOCKED,
        )
        val out = gate.evaluate("scan-no-anchor", extraction, userConfirmed = true, fullOcrTranscript = full)
        assertTrue(out.submissionAllowed)
        assertEquals(full, out.segmentPreview)
    }

    @Test
    fun `allows implicit when anchor missing but full ocr substantive`() {
        val full = "Lot 123\nEau, sucre"
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-implicit-no-anchor",
            anchorFound = false,
            anchorIndex = null,
            endIndex = null,
            segmentText = null,
            fallbackMode = IngredientSegmentFallbackMode.ANCHOR_MISSING_BLOCKED,
        )
        val out = gate.evaluate(
            "scan-implicit-no-anchor",
            extraction,
            userConfirmed = false,
            implicitValidationFromIngredientsFraming = true,
            fullOcrTranscript = full,
        )
        assertTrue(out.submissionAllowed)
        assertTrue(out.implicitValidationFromIngredientsFraming)
    }

    @Test
    fun `blocks when segment is only English singular label`() {
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-en-s",
            anchorFound = true,
            anchorIndex = 0,
            endIndex = 10,
            segmentText = "Ingredient",
            fallbackMode = IngredientSegmentFallbackMode.NONE,
        )
        val out = gate.evaluate("scan-en-s", extraction, userConfirmed = true, fullOcrTranscript = "Ingredient")
        assertFalse(out.submissionAllowed)
        assertEquals(SubmissionBlockedReason.EMPTY_SEGMENT, out.blockedReason)
    }

    @Test
    fun `blocks when segment is only English plural label with colon`() {
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-en-p",
            anchorFound = true,
            anchorIndex = 0,
            endIndex = 14,
            segmentText = "Ingredients:",
            fallbackMode = IngredientSegmentFallbackMode.NONE,
        )
        val out = gate.evaluate("scan-en-p", extraction, userConfirmed = true, fullOcrTranscript = "Ingredients:")
        assertFalse(out.submissionAllowed)
        assertEquals(SubmissionBlockedReason.EMPTY_SEGMENT, out.blockedReason)
    }

    @Test
    fun `blocks when segment is only French label with accent`() {
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-fr",
            anchorFound = true,
            anchorIndex = 0,
            endIndex = 12,
            segmentText = "Ingrédients:",
            fallbackMode = IngredientSegmentFallbackMode.NONE,
        )
        val out = gate.evaluate("scan-fr", extraction, userConfirmed = true, fullOcrTranscript = "Ingrédients:")
        assertFalse(out.submissionAllowed)
        assertEquals(SubmissionBlockedReason.EMPTY_SEGMENT, out.blockedReason)
    }

    @Test
    fun `allows submission when implicit ingredients framing without user tap`() {
        val full = "Ingrédients: eau, sucre, sel."
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-implicit",
            anchorFound = true,
            anchorIndex = 0,
            endIndex = 40,
            segmentText = "Ingrédients: eau, sucre, sel.",
            fallbackMode = IngredientSegmentFallbackMode.NONE,
        )
        val out = gate.evaluate(
            "scan-implicit",
            extraction,
            userConfirmed = false,
            implicitValidationFromIngredientsFraming = true,
            fullOcrTranscript = full,
        )
        assertTrue(out.submissionAllowed)
        assertTrue(out.implicitValidationFromIngredientsFraming)
        assertEquals(SubmissionBlockedReason.NONE, out.blockedReason)
    }

    @Test
    fun `implicit framing still blocks label only full ocr`() {
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-implicit-empty",
            anchorFound = true,
            anchorIndex = 0,
            endIndex = 12,
            segmentText = "Ingrédients:",
            fallbackMode = IngredientSegmentFallbackMode.NONE,
        )
        val out = gate.evaluate(
            "scan-implicit-empty",
            extraction,
            userConfirmed = false,
            implicitValidationFromIngredientsFraming = true,
            fullOcrTranscript = "Ingrédients:",
        )
        assertFalse(out.submissionAllowed)
        assertEquals(SubmissionBlockedReason.EMPTY_SEGMENT, out.blockedReason)
    }
}
