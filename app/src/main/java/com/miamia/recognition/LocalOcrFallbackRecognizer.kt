package com.miamia.recognition

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocalOcrFallbackRecognizer(private val context: Context) {
    suspend fun recognize(command: StartIngredientRecognitionCommand): IngredientRecognitionResult {
        return try {
            val image = InputImage.fromFilePath(context, Uri.parse(command.imageUri).takeIf { it.scheme != null }
                ?: Uri.fromFile(java.io.File(command.imageUri)))
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = recognizer.process(image).await()
            val lines = result.textBlocks.flatMap { it.lines }.map { it.text.trim() }.filter { it.isNotBlank() }
            if (lines.isEmpty()) {
                IngredientRecognitionResult(
                    scanId = command.scanId,
                    outcome = "empty",
                    ocrConfidenceGlobal = 0f,
                    autoValidated = false,
                    items = emptyList(),
                    userMessage = "Aucun texte détecté"
                )
            } else {
                val items = lines.mapIndexed { index, text ->
                    IngredientRecognitionItem(
                        position = index,
                        rawText = text,
                        normalizedText = text.lowercase(),
                        confidence = 0.75f,
                        languageTag = "auto",
                        isAllergenMarked = text.any { it.isUpperCase() }
                    )
                }
                IngredientRecognitionResult(
                    scanId = command.scanId,
                    outcome = "success",
                    ocrConfidenceGlobal = 0.75f,
                    autoValidated = true,
                    items = items,
                    userMessage = "Reconnaissance OCR locale terminée"
                )
            }
        } catch (_: Throwable) {
            IngredientRecognitionResult(
                scanId = command.scanId,
                outcome = "error",
                ocrConfidenceGlobal = 0f,
                autoValidated = false,
                items = emptyList(),
                userMessage = "Impossible de lire le texte, réessayez"
            )
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
    }
}
