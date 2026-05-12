package com.miamia.scan

import java.io.File

data class StartScanRequest(
    val sessionId: String,
    val capturedAt: String,
    val tempImagePath: String,
    val languageHint: String = "fr",
    val maxProcessingMs: Long = 10_000
)

data class StartScanResponse(
    val sessionId: String,
    val status: String,
    val transcriptText: String? = null,
    val errorCode: String? = null,
    val analyzedAt: String,
    val processingMs: Long,
    val tempImageDeleted: Boolean
)

class LocalOcrAnalyzer {
    suspend fun analyze(request: StartScanRequest): StartScanResponse {
        val file = File(request.tempImagePath)
        if (!file.exists()) {
            return StartScanResponse(
                sessionId = request.sessionId,
                status = "error",
                errorCode = "temp_image_missing",
                analyzedAt = java.time.Instant.now().toString(),
                processingMs = 0,
                tempImageDeleted = false
            )
        }

        // Placeholder local inference path; integration with Google AI Edge runtime plugs in here.
        return StartScanResponse(
            sessionId = request.sessionId,
            status = "success",
            transcriptText = "Transcription locale simulée",
            analyzedAt = java.time.Instant.now().toString(),
            processingMs = 350,
            tempImageDeleted = false
        )
    }
}
