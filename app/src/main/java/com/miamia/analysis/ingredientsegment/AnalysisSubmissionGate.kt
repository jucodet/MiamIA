package com.miamia.analysis.ingredientsegment

import android.util.Log

class AnalysisSubmissionGate {
    private val ingredientsLabelOnlyRegex =
        Regex("""^\s*(ingr[ée]dients|ingr[ée]dient|ingredients|ingredient)\s*:?\s*$""", RegexOption.IGNORE_CASE)

    /**
     * @param fullOcrTranscript Transcript OCR complet de la session (FR-012). Les garde-fous de vacuité /
     *   label seul s'appliquent à cette chaîne ; [extraction.anchorFound] ne bloque plus à lui seul la soumission.
     */
    fun evaluate(
        scanId: String,
        extraction: IngredientSegmentExtraction,
        userConfirmed: Boolean,
        implicitValidationFromIngredientsFraming: Boolean = false,
        fullOcrTranscript: String,
    ): AnalysisSubmissionDecision {
        val ocrTrimmed = fullOcrTranscript.trim()
        if (ocrTrimmed.isEmpty()) {
            logBlocked(scanId, SubmissionBlockedReason.EMPTY_SEGMENT)
            return AnalysisSubmissionDecision(
                scanId = scanId,
                segmentPreview = "",
                userConfirmed = userConfirmed,
                submissionAllowed = false,
                blockedReason = SubmissionBlockedReason.EMPTY_SEGMENT,
                implicitValidationFromIngredientsFraming = false,
            )
        }
        if (ingredientsLabelOnlyRegex.matches(ocrTrimmed)) {
            logBlocked(scanId, SubmissionBlockedReason.EMPTY_SEGMENT)
            return AnalysisSubmissionDecision(
                scanId = scanId,
                segmentPreview = ocrTrimmed,
                userConfirmed = userConfirmed,
                submissionAllowed = false,
                blockedReason = SubmissionBlockedReason.EMPTY_SEGMENT,
                implicitValidationFromIngredientsFraming = false,
            )
        }

        val allowImplicit = implicitValidationFromIngredientsFraming && !userConfirmed
        if (!userConfirmed && !allowImplicit) {
            return AnalysisSubmissionDecision(
                scanId = scanId,
                segmentPreview = ocrTrimmed,
                userConfirmed = false,
                submissionAllowed = false,
                blockedReason = SubmissionBlockedReason.USER_REJECTED,
                implicitValidationFromIngredientsFraming = false,
            )
        }

        val implicitApplied = allowImplicit
        Log.d(
            TAG,
            "segment_submission_allowed scanId=$scanId mode=${extraction.fallbackMode} implicitFraming=$implicitApplied anchorFound=${extraction.anchorFound}",
        )
        return AnalysisSubmissionDecision(
            scanId = scanId,
            segmentPreview = ocrTrimmed,
            userConfirmed = true,
            submissionAllowed = true,
            blockedReason = SubmissionBlockedReason.NONE,
            implicitValidationFromIngredientsFraming = implicitApplied,
        )
    }

    private fun logBlocked(scanId: String, reason: SubmissionBlockedReason) {
        Log.w(TAG, "segment_submission_blocked scanId=$scanId reason=$reason")
    }

    companion object {
        private const val TAG = "AnalysisSubmissionGate"
    }
}
