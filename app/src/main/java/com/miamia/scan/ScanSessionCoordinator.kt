package com.miamia.scan

import com.miamia.data.repository.ScanSessionRepository
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

class ScanSessionCoordinator(
    private val analyzer: LocalOcrAnalyzer,
    private val repository: ScanSessionRepository
) {
    suspend fun runScan(tempImageFile: File): StartScanResponse {
        val sessionId = UUID.randomUUID().toString()
        val startedAt = System.currentTimeMillis()
        repository.startSession(sessionId, startedAt)

        var deleted = false
        var responseStatus = "error"
        var responseErrorCode: String? = null
        var responseTranscript: String? = null
        val perfStart = System.currentTimeMillis()
        return try {
            val response = analyzer.analyze(
                StartScanRequest(
                    sessionId = sessionId,
                    capturedAt = Instant.now().toString(),
                    tempImagePath = tempImageFile.absolutePath
                )
            )
            responseStatus = response.status
            responseErrorCode = response.errorCode
            responseTranscript = response.transcriptText
            response
        } catch (e: Exception) {
            StartScanResponse(
                sessionId = sessionId,
                status = "error",
                errorCode = "analysis_exception",
                analyzedAt = Instant.now().toString(),
                processingMs = 0,
                tempImageDeleted = false
            )
        } finally {
            val perfEnd = System.currentTimeMillis()
            deleted = tempImageFile.delete() || !tempImageFile.exists()
            val finishedAt = System.currentTimeMillis()
            repository.completeSession(
                sessionId = sessionId,
                startedAt = startedAt,
                finishedAt = finishedAt,
                status = responseStatus,
                tempImageDeleted = deleted,
                errorCode = responseErrorCode,
                fingerprint = if (tempImageFile.exists()) sha256(tempImageFile) else null
            )
            // Local-only performance metric for startup/scan observability.
            println("scan_session_duration_ms=${perfEnd - perfStart} status=$responseStatus hasText=${!responseTranscript.isNullOrBlank()}")
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = file.readBytes()
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
