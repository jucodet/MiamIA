package com.miamia.analysis.ingredientsegment

import com.miamia.analysis.ingredientsegment.fixtures.OcrFixtures
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AnalysisSubmissionDecisionAcceptanceTest {

    private val gate = AnalysisSubmissionGate()
    private val service = IngredientSegmentPreparationService()

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
        val implicitAllowed = gate.evaluate(
            "scan-confirm",
            extraction,
            userConfirmed = false,
            implicitValidationFromIngredientsFraming = true
        )

        assertFalse(blocked.submissionAllowed)
        assertTrue(allowed.submissionAllowed)
        assertTrue(implicitAllowed.submissionAllowed)
        assertTrue(implicitAllowed.implicitValidationFromIngredientsFraming)
    }

    @Test
    fun `gate accepts segment with internal dot after user confirmation`() {
        val extraction = service.prepare("scan-dot-gate", OcrFixtures.DOT_INTERNAL_ADDITIVE)

        assertTrue(extraction.anchorFound)
        assertTrue(extraction.segmentText!!.contains("E.621"))

        val decision = gate.evaluate("scan-dot-gate", extraction, userConfirmed = true)

        assertTrue(decision.submissionAllowed)
    }
}
