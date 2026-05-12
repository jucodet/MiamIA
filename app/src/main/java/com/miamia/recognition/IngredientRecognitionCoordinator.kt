package com.miamia.recognition

import com.miamia.core.FeatureConfig
import com.miamia.data.repository.ScanSessionRepository
import java.io.File
import java.util.UUID

class IngredientRecognitionCoordinator(
    private val engineSelector: RecognitionEngineSelector,
    private val extractionPipeline: IngredientExtractionPipeline,
    private val repository: ScanSessionRepository
) {
    suspend fun runRecognition(file: File): IngredientRecognitionResult {
        val scanId = UUID.randomUUID().toString()
        val startedAt = System.currentTimeMillis()
        repository.startSession(scanId, startedAt)
        val command = StartIngredientRecognitionCommand(
            scanId = scanId,
            imageUri = file.absolutePath,
            requestedAtEpochMs = startedAt,
            source = "camera"
        )
        val (_, result) = engineSelector.recognize(command)
        val enrichedItems = result.items
        val finalResult = result.copy(
            items = enrichedItems,
            autoValidated = result.ocrConfidenceGlobal >= FeatureConfig.AUTO_VALIDATE_OCR_THRESHOLD
        )
        repository.completeSession(
            sessionId = scanId,
            startedAt = startedAt,
            finishedAt = System.currentTimeMillis(),
            status = finalResult.outcome,
            tempImageDeleted = file.delete() || !file.exists(),
            errorCode = if (finalResult.outcome == "error") "recognition_failed" else null
        )
        return finalResult
    }
}
