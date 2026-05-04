package com.foodgpt.analysis.ingredientsegment

import android.util.Log

class AnalysisSubmissionGate {
    private val ingredientsLabelOnlyRegex = Regex("^ingr[ée]dients?\\s*:?$", RegexOption.IGNORE_CASE)

    fun evaluate(
        scanId: String,
        extraction: IngredientSegmentExtraction,
        userConfirmed: Boolean
    ): AnalysisSubmissionDecision {
        if (!extraction.anchorFound) {
            logBlocked(scanId, SubmissionBlockedReason.ANCHOR_MISSING)
            return AnalysisSubmissionDecision(
                scanId = scanId,
                segmentPreview = "",
                userConfirmed = userConfirmed,
                submissionAllowed = false,
                blockedReason = SubmissionBlockedReason.ANCHOR_MISSING
            )
        }

        val segment = extraction.segmentText.orEmpty().trim()
        val normalizedSegment = segment.lowercase()
        if (normalizedSegment.isBlank() || ingredientsLabelOnlyRegex.matches(normalizedSegment)) {
            logBlocked(scanId, SubmissionBlockedReason.EMPTY_SEGMENT)
            return AnalysisSubmissionDecision(
                scanId = scanId,
                segmentPreview = segment,
                userConfirmed = userConfirmed,
                submissionAllowed = false,
                blockedReason = SubmissionBlockedReason.EMPTY_SEGMENT
            )
        }

        if (!userConfirmed) {
            return AnalysisSubmissionDecision(
                scanId = scanId,
                segmentPreview = segment,
                userConfirmed = false,
                submissionAllowed = false,
                blockedReason = SubmissionBlockedReason.USER_REJECTED
            )
        }

        Log.d(TAG, "segment_submission_allowed scanId=$scanId mode=${extraction.fallbackMode}")
        return AnalysisSubmissionDecision(
            scanId = scanId,
            segmentPreview = segment,
            userConfirmed = true,
            submissionAllowed = true,
            blockedReason = SubmissionBlockedReason.NONE
        )
    }

    private fun logBlocked(scanId: String, reason: SubmissionBlockedReason) {
        Log.w(TAG, "segment_submission_blocked scanId=$scanId reason=$reason")
    }

    companion object {
        private const val TAG = "AnalysisSubmissionGate"
    }
}
