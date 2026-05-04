package com.foodgpt.camera

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.foodgpt.analysis.Gemma4LocalUiMessageResolver
import com.foodgpt.analysis.AnalysisInputBuilder
import androidx.lifecycle.viewModelScope
import com.foodgpt.analysis.ingredientsegment.AnalysisSubmissionGate
import com.foodgpt.analysis.ingredientsegment.IngredientSegmentPreparationService
import com.foodgpt.analysis.ingredientsegment.SubmissionBlockedReason
import com.foodgpt.composition.AnalyzeCompositionResult
import com.foodgpt.composition.CompositionAnalysisEngine
import com.foodgpt.composition.CompositionMessages
import com.foodgpt.composition.CompositionResultValidator
import com.foodgpt.composition.GemmaErrorCode
import com.foodgpt.core.FeatureConfig
import com.foodgpt.ingredients.ExtractedIngredientMapper
import com.foodgpt.ingredients.RetryScanActionHandler
import com.foodgpt.ingredients.ScanFailureMessageBuilder
import com.foodgpt.permissions.CameraPermissionHandler
import com.foodgpt.recognition.IngredientRecognitionCoordinator
import com.foodgpt.recognition.ScanFailureClassifier
import com.foodgpt.gemma4local.AndroidGemma4LocalGateway
import com.foodgpt.gemma4local.Gemma4LocalAvailabilityChecker
import com.foodgpt.home.MediaPipeLlmAvailabilityProbe
import com.foodgpt.home.MediaPipeStatusViewState
import com.foodgpt.welcome.WelcomeDisplayLogger
import com.foodgpt.welcome.WelcomeMessagePolicy
import com.foodgpt.welcome.WelcomeMessageProvider
import com.foodgpt.welcome.WelcomeMessageSelector
import com.foodgpt.welcome.WelcomeMessageUiState
import com.foodgpt.welcome.toUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CameraViewModel(
    application: Application,
    private val coordinator: IngredientRecognitionCoordinator?,
    private val compositionEngine: CompositionAnalysisEngine? = null,
    private val permissionHandler: CameraPermissionHandler = CameraPermissionHandler(),
    private val mapper: ExtractedIngredientMapper = ExtractedIngredientMapper(),
    private val failureClassifier: ScanFailureClassifier = ScanFailureClassifier(),
    private val failureMessageBuilder: ScanFailureMessageBuilder = ScanFailureMessageBuilder(),
    private val retryHandler: RetryScanActionHandler = RetryScanActionHandler(),
    private val captureController: CameraCaptureController = CameraCaptureController(application.applicationContext),
    private val welcomePolicy: WelcomeMessagePolicy = WelcomeMessagePolicy(
        provider = WelcomeMessageProvider(application.applicationContext),
        selector = WelcomeMessageSelector(),
        logger = WelcomeDisplayLogger()
    )
) : AndroidViewModel(application) {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.CameraReady)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _previewSession = MutableStateFlow(0)
    val previewSession: StateFlow<Int> = _previewSession.asStateFlow()

    private val _welcomeUiState = MutableStateFlow<WelcomeMessageUiState>(WelcomeMessageUiState.Hidden)
    val welcomeUiState: StateFlow<WelcomeMessageUiState> = _welcomeUiState.asStateFlow()
    private val _mediaPipeStatus = MutableStateFlow(MediaPipeStatusViewState.checking())
    val mediaPipeStatus: StateFlow<MediaPipeStatusViewState> = _mediaPipeStatus.asStateFlow()

    private var bindJob: Job? = null
    private var inFlightScan = false

    private var lastRawTranscript: String? = null
    private var lastItemsPreview: List<String>? = null
    private var pendingAnalysisSegment: String? = null
    private var pendingScanId: String? = null
    private val mediaPipeApiProbe = MediaPipeLlmAvailabilityProbe()
    private val segmentPreparationService = IngredientSegmentPreparationService()
    private val submissionGate = AnalysisSubmissionGate()

    /** Exclus tests unitaires — simule un OCR terminé avant l’étape composition. */
    @VisibleForTesting
    fun debugSeedTranscript(transcript: String, items: List<String> = emptyList()) {
        lastRawTranscript = transcript
        lastItemsPreview = items
    }

    fun onPermissionDenied() {
        bindJob?.cancel()
        captureController.unbind()
        _scanState.value = ScanState.PermissionDenied
        refreshMediaPipeAvailability()
    }

    fun onPermissionGranted() {
        if (!permissionHandler.hasCameraPermission(getApplication())) {
            onPermissionDenied()
            return
        }
        bindJob?.cancel()
        captureController.unbind()
        _previewSession.value += 1
        _scanState.value = ScanState.CameraReady
        refreshMediaPipeAvailability()
    }

    fun onRetry() {
        bindJob?.cancel()
        captureController.unbind()
        inFlightScan = false
        lastRawTranscript = null
        lastItemsPreview = null
        pendingAnalysisSegment = null
        pendingScanId = null
        _previewSession.value += 1
        _scanState.value = if (permissionHandler.hasCameraPermission(getApplication())) {
            ScanState.CameraReady
        } else {
            ScanState.PermissionDenied
        }
        refreshMediaPipeAvailability()
    }

    fun onLoginSucceeded(userId: String = "connected-user") {
        _welcomeUiState.value = welcomePolicy.onLoginSucceeded(userId).toUiState()
        refreshMediaPipeAvailability()
    }

    fun onGemmaModelImported() {
        refreshMediaPipeAvailability()
    }

    /**
     * Relance uniquement l’étape composition (Gemma) après une erreur, sans nouvelle capture.
     */
    fun retryCompositionAnalysis() {
        val engine = compositionEngine ?: return
        val raw = lastRawTranscript ?: return
        val items = lastItemsPreview.orEmpty()
        viewModelScope.launch {
            _scanState.value = ScanState.CompositionAnalyzing()
            runCompositionStage(engine, raw, items)
        }
    }

    /**
     * Repli : afficher le texte OCR comme avant le bilan (spec 009 repli texte brut).
     */
    fun showRawTranscriptOnly() {
        val raw = lastRawTranscript ?: return
        val items = lastItemsPreview.orEmpty()
        _scanState.value = ScanState.Success(transcriptText = raw, items = items)
    }

    fun attachPreview(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        if (!permissionHandler.hasCameraPermission(getApplication())) {
            _scanState.value = ScanState.PermissionDenied
            return
        }
        bindJob?.cancel()
        bindJob = viewModelScope.launch {
            _scanState.value = ScanState.PreviewInitializing
            val bindStart = SystemClock.elapsedRealtime()
            val result = withContext(Dispatchers.Main) {
                captureController.bind(lifecycleOwner, previewView)
            }
            val bindMs = SystemClock.elapsedRealtime() - bindStart
            Log.d(
                TAG,
                "preview_bind_total_ms=$bindMs target_ms=${FeatureConfig.PREVIEW_START_TARGET_MS}"
            )
            if (!isActive) return@launch
            _scanState.value = if (result.isSuccess) {
                ScanState.PreviewActive
            } else {
                ScanState.CameraUnavailable(result.exceptionOrNull()?.message)
            }
        }
    }

    fun capturePhoto(outputFile: File) {
        if (inFlightScan) return
        if (_scanState.value != ScanState.PreviewActive) return
        val scanCoordinator = coordinator ?: return

        inFlightScan = true
        _scanState.value = ScanState.Capturing
        viewModelScope.launch {
            try {
                val captureResult = withContext(Dispatchers.Main) {
                    captureController.captureToFile(outputFile)
                }
                if (captureResult.isFailure) {
                    _scanState.value = ScanState.Error(
                        captureResult.exceptionOrNull()?.message ?: "Échec de la capture"
                    )
                    return@launch
                }

                _scanState.value = ScanState.Analyzing
                val result = scanCoordinator.runRecognition(outputFile)
                if (result.outcome == "success" || result.outcome == "partial") {
                    val uiItems = mapper.toUi(result.items)
                    val itemLabels = uiItems.map { it.text }
                    val transcriptText = result.items.joinToString("\n") { it.normalizedText }
                    val canonicalAnchor = Regex("ingr[ée]dients?\\s*:", RegexOption.IGNORE_CASE).find(transcriptText)
                    if (canonicalAnchor == null) {
                        lastRawTranscript = transcriptText
                        lastItemsPreview = itemLabels
                        _scanState.value = ScanState.Success(
                            transcriptText = transcriptText,
                            items = itemLabels
                        )
                        return@launch
                    }
                    val extraction = segmentPreparationService.prepare(result.scanId, transcriptText)
                    val previewDecision = submissionGate.evaluate(
                        scanId = result.scanId,
                        extraction = extraction,
                        userConfirmed = false
                    )
                    if (!extraction.anchorFound || previewDecision.blockedReason == SubmissionBlockedReason.EMPTY_SEGMENT) {
                        _scanState.value = ScanState.Error(
                            "Section ingredients introuvable ou vide. Reprenez la photo ou editez le texte."
                        )
                        return@launch
                    }
                    val segmentForAnalysis = AnalysisInputBuilder.buildSegmentPayload(
                        extraction.segmentText.orEmpty()
                    )
                    pendingAnalysisSegment = segmentForAnalysis
                    pendingScanId = result.scanId
                    lastRawTranscript = transcriptText
                    lastItemsPreview = itemLabels
                    _scanState.value = ScanState.SegmentConfirmationRequired(
                        segmentPreview = segmentForAnalysis,
                        itemsPreview = itemLabels
                    )
                } else if (result.outcome == "empty") {
                    _scanState.value = ScanState.Empty(result.userMessage.ifBlank { "Aucun texte détecté" })
                } else {
                    val code = failureClassifier.classify(result.ocrConfidenceGlobal, result.items.size)
                    val message = failureMessageBuilder.build(code)
                    if (!retryHandler.canRetryManually()) {
                        _scanState.value = ScanState.Error("Relance manuelle indisponible")
                    } else {
                        _scanState.value = ScanState.Error(message)
                    }
                }
            } finally {
                captureController.unbind()
                inFlightScan = false
            }
        }
    }

    fun confirmSegmentAndAnalyze() {
        val segment = pendingAnalysisSegment ?: return
        val scanId = pendingScanId ?: return
        val items = lastItemsPreview.orEmpty()
        val extraction = segmentPreparationService.prepare(scanId, segment)
        val decision = submissionGate.evaluate(
            scanId = scanId,
            extraction = extraction,
            userConfirmed = true
        )
        if (!decision.submissionAllowed) {
            _scanState.value = ScanState.Error("Analyse bloquee: confirmation ou segment invalide.")
            return
        }
        val engine = compositionEngine
        if (engine == null) {
            _scanState.value = ScanState.Success(
                transcriptText = decision.segmentPreview,
                items = items
            )
            return
        }
        viewModelScope.launch {
            _scanState.value = ScanState.CompositionAnalyzing()
            runCompositionStage(engine, decision.segmentPreview, items)
        }
    }

    fun rejectSegmentConfirmation() {
        _scanState.value = ScanState.Error("Analyse annulee. Vous pouvez reprendre une photo.")
    }

    private suspend fun runCompositionStage(
        engine: CompositionAnalysisEngine,
        rawText: String,
        itemsPreview: List<String>
    ) {
        // Le délai est appliqué dans le moteur (code JNI bloquant : withTimeout coroutine ne suffit pas).
        val outcome = engine.analyze(
            rawText,
            FeatureConfig.COMPOSITION_ANALYSIS_TIMEOUT_MS
        ) { partial ->
            _scanState.update { cur ->
                if (cur is ScanState.CompositionAnalyzing) {
                    cur.copy(partialResponse = partial)
                } else {
                    cur
                }
            }
        }
        _scanState.value = when (outcome) {
            is AnalyzeCompositionResult.BilanSuccess -> {
                val emptyReject = CompositionResultValidator.rejectEmptyStructure(outcome.bilan)
                if (emptyReject != null) {
                    ScanState.CompositionLimit(
                        message = emptyReject.message,
                        rawTranscript = rawText
                    )
                } else {
                    when (val v = CompositionResultValidator.validateAgainstSource(outcome.bilan, rawText)) {
                        is AnalyzeCompositionResult.BilanSuccess -> ScanState.BilanReady(
                            bilan = v.bilan,
                            rawTranscript = rawText,
                            itemsPreview = itemsPreview
                        )
                        is AnalyzeCompositionResult.CompositionLimit -> ScanState.CompositionLimit(
                            message = v.message,
                            rawTranscript = rawText
                        )
                        else -> ScanState.CompositionLimit(
                            CompositionMessages.COMPOSITION_LIMIT_GENERIC,
                            rawText
                        )
                    }
                }
            }
            is AnalyzeCompositionResult.GemmaError -> {
                val uiMessage = Gemma4LocalUiMessageResolver.resolve(outcome.code, outcome.message)
                ScanState.GemmaUnavailable(
                    code = outcome.code,
                    message = uiMessage,
                    rawTranscript = rawText
                )
            }
            is AnalyzeCompositionResult.CompositionLimit -> ScanState.CompositionLimit(
                message = outcome.message,
                rawTranscript = rawText
            )
        }
    }

    override fun onCleared() {
        bindJob?.cancel()
        captureController.shutdown()
        super.onCleared()
    }

    private fun refreshMediaPipeAvailability() {
        viewModelScope.launch {
            _mediaPipeStatus.value = MediaPipeStatusViewState.checking()
            val available = withContext(Dispatchers.IO) {
                runCatching { mediaPipeApiProbe.isAvailable() }.getOrDefault(false)
            }
            _mediaPipeStatus.value = if (available) {
                MediaPipeStatusViewState.available()
            } else {
                MediaPipeStatusViewState.unavailable()
            }
        }
    }

    companion object {
        private const val TAG = "CameraViewModel"

        fun factory(
            application: Application,
            coordinator: IngredientRecognitionCoordinator?,
            compositionEngine: CompositionAnalysisEngine? = null
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(CameraViewModel::class.java))
                    return CameraViewModel(application, coordinator, compositionEngine) as T
                }
            }
        }
    }
}
