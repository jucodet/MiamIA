package com.foodgpt.analysis.ingredientsegment

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AnalysisSubmissionDecisionAcceptanceTest {

    private val gate = AnalysisSubmissionGate()

    @Test
    fun `submission is blocked when not confirmed`() {
        val extraction = IngredientSegmentExtraction(
            scanId = "scan-confirm",
            anchorFound = true,
            anchorIndex = 0,
            endIndex = 24,
            segmentText = "ingredients: sucre, sel",
            fallbackMode = IngredientSegmentFallbackMode.NONE
        )

        val blocked = gate.evaluate("scan-confirm", extraction, userConfirmed = false)
        val allowed = gate.evaluate("scan-confirm", extraction, userConfirmed = true)

        assertFalse(blocked.submissionAllowed)
        assertTrue(allowed.submissionAllowed)
    }
}
