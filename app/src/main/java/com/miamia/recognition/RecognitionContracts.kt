package com.miamia.recognition

data class StartIngredientRecognitionCommand(
    val scanId: String,
    val imageUri: String,
    val requestedAtEpochMs: Long,
    val source: String
)

data class RecognitionCapabilityResponse(
    val scanId: String,
    val aiEdgeGalleryAvailable: Boolean,
    val selectedEngine: String,
    val reason: String
)

data class IngredientRecognitionItem(
    val position: Int,
    val rawText: String,
    val normalizedText: String,
    val confidence: Float,
    val languageTag: String? = null,
    val isAllergenMarked: Boolean
)

data class IngredientAnchorCandidate(
    val startIndex: Int,
    val rawMatch: String,
    val isCanonical: Boolean
)

data class IngredientAnchorDetectionResult(
    val scanId: String,
    val candidates: List<IngredientAnchorCandidate>,
    val selectionRule: String,
    val selectedStartIndex: Int?,
    val anchorFound: Boolean,
    val blockedReason: String
)

data class IngredientRecognitionResult(
    val scanId: String,
    val outcome: String,
    val ocrConfidenceGlobal: Float,
    val autoValidated: Boolean,
    val items: List<IngredientRecognitionItem>,
    val userMessage: String
)

data class PhotoTextRecognitionState(
    val state: String,
    val displayText: String? = null,
    val message: String? = null,
    val errorReason: String? = null
)

data class ValidateIngredientListCommand(
    val scanId: String,
    val finalItems: List<String>,
    val editedByUser: Boolean
)

data class ValidateIngredientListResult(
    val scanId: String,
    val accepted: Boolean,
    val validatedAtEpochMs: Long,
    val errors: List<String>
)
