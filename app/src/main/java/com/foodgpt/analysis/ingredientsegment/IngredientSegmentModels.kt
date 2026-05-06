package com.foodgpt.analysis.ingredientsegment

data class OcrRawText(
    val scanId: String,
    val content: String,
    val capturedAtEpochMs: Long
)

enum class IngredientSegmentFallbackMode {
    NONE,
    ANCHOR_MISSING_BLOCKED,
    NO_NEWLINE_TO_EOF
}

enum class IngredientSegmentBoundaryEndReason {
    NONE,
    SENTENCE_TERMINATOR,
    LINE_END,
    TEXT_END
}

data class IngredientSegmentExtraction(
    val scanId: String,
    val anchorFound: Boolean,
    val anchorIndex: Int?,
    val endIndex: Int?,
    val segmentText: String?,
    val fallbackMode: IngredientSegmentFallbackMode,
    val boundaryEndReason: IngredientSegmentBoundaryEndReason = IngredientSegmentBoundaryEndReason.NONE
)

enum class SubmissionBlockedReason {
    NONE,
    ANCHOR_MISSING,
    EMPTY_SEGMENT,
    USER_REJECTED
}

data class AnalysisSubmissionDecision(
    val scanId: String,
    val segmentPreview: String,
    val userConfirmed: Boolean,
    val submissionAllowed: Boolean,
    val blockedReason: SubmissionBlockedReason
)
