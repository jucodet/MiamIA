package com.miamia.camera

import com.miamia.composition.CompositionBilan
import com.miamia.composition.GemmaErrorCode

sealed class ScanState {
    /** Permission OK, en attente d’attache surface / redémarrage preview. */
    data object CameraReady : ScanState()

    /** Liaison CameraX + surface en cours. */
    data object PreviewInitializing : ScanState()

    /** Flux objectif actif (aperçu réel). */
    data object PreviewActive : ScanState()

    data object Capturing : ScanState()
    data object Analyzing : ScanState()

    data class Success(
        val transcriptText: String,
        val items: List<String> = emptyList()
    ) : ScanState()

    data class SegmentConfirmationRequired(
        val segmentPreview: String,
        val itemsPreview: List<String> = emptyList()
    ) : ScanState()

    /** OCR terminé ; inférence Gemma / bilan en cours (spec 009). */
    /** [partialResponse] : sortie modèle en cours (streaming) pour feedback immédiat. */
    data class CompositionAnalyzing(val partialResponse: String = "") : ScanState()

    /** Bilan liste + analyse affiché (spec 009 US1). */
    data class BilanReady(
        val bilan: CompositionBilan,
        val rawTranscript: String,
        val itemsPreview: List<String>
    ) : ScanState()

    /** Gemma absent ou exécution impossible (spec 009 US3). */
    data class GemmaUnavailable(
        val code: GemmaErrorCode,
        val message: String,
        val rawTranscript: String
    ) : ScanState()

    /** Texte non exploitable pour un bilan fiable (spec 009 US2). */
    data class CompositionLimit(
        val message: String,
        val rawTranscript: String
    ) : ScanState()

    data class Empty(val message: String) : ScanState()

    data class Error(val message: String) : ScanState()
    data object PermissionDenied : ScanState()

    /** Caméra indisponible ou échec de liaison explicite (pas de faux preview). */
    data class CameraUnavailable(val reason: String?) : ScanState()
}
