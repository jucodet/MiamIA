package com.miamia.recognition

class RecognitionEngineSelector(
    private val capabilityDetector: DeviceAiCapabilityDetector,
    private val aiEdgeGalleryRecognizer: AiEdgeGalleryRecognizer,
    private val localOcrFallbackRecognizer: LocalOcrFallbackRecognizer
) {
    suspend fun recognize(command: StartIngredientRecognitionCommand): Pair<RecognitionCapabilityResponse, IngredientRecognitionResult> {
        val aiAvailable = capabilityDetector.isAiEdgeGalleryAvailable()
        val capability = RecognitionCapabilityResponse(
            scanId = command.scanId,
            aiEdgeGalleryAvailable = aiAvailable,
            selectedEngine = "local_ocr_fallback",
            reason = "${capabilityDetector.explainAvailability()} - OCR local forcé pour éviter tout résultat factice."
        )
        // Tant que le moteur AI Edge n'est pas branché sur une reconnaissance réelle,
        // on force le fallback local pour garantir des résultats issus de la photo.
        val result = localOcrFallbackRecognizer.recognize(command)
        return capability to result
    }
}
