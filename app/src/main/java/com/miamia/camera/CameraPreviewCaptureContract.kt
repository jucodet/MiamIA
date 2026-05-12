package com.miamia.camera

data class StartPreviewCommand(
    val sessionId: String,
    val requestedAtEpochMs: Long,
    val cameraFacing: String,
    val permissionGranted: Boolean
)

data class PreviewStateEvent(
    val sessionId: String,
    val state: String,
    val message: String,
    val timestampEpochMs: Long
)

data class CaptureFrameCommand(
    val sessionId: String,
    val actionId: String,
    val triggeredBy: String,
    val triggeredAtEpochMs: Long
)

data class CaptureFrameResult(
    val sessionId: String,
    val actionId: String,
    val status: String,
    val imageUri: String?,
    val feedbackMessage: String,
    val errorCode: String?
)

data class CameraErrorState(
    val sessionId: String,
    val errorType: String,
    val userMessage: String,
    val canRetry: Boolean
)
