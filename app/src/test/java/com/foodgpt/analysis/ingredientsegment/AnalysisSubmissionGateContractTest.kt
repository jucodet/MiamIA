package com.foodgpt.analysis.ingredientsegment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AnalysisSubmissionGateContractTest {

    private val gate = AnalysisSubmissionGate()

    @Test
    fun `returns blocked reason when anchor missing`() {
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-1",
            anchorFound = false,
            anchorIndex = null,
            endIndex = null,
            segmentText = null,
            fallbackMode = IngredientSegmentFallbackMode.ANCHOR_MISSING_BLOCKED
        )

        val out = gate.evaluate("scan-1", extraction, userConfirmed = true)

        assertFalse(out.submissionAllowed)
        assertEquals(SubmissionBlockedReason.ANCHOR_MISSING, out.blockedReason)
    }

    @Test
    fun `blocks when segment is only English singular label`() {
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-en-s",
            anchorFound = true,
            anchorIndex = 0,
            endIndex = 10,
            segmentText = "Ingredient",
            fallbackMode = IngredientSegmentFallbackMode.NONE
        )
        val out = gate.evaluate("scan-en-s", extraction, userConfirmed = true)
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
            fallbackMode = IngredientSegmentFallbackMode.NONE
        )
        val out = gate.evaluate("scan-en-p", extraction, userConfirmed = true)
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
            fallbackMode = IngredientSegmentFallbackMode.NONE
        )
        val out = gate.evaluate("scan-fr", extraction, userConfirmed = true)
        assertFalse(out.submissionAllowed)
        assertEquals(SubmissionBlockedReason.EMPTY_SEGMENT, out.blockedReason)
    }
}
