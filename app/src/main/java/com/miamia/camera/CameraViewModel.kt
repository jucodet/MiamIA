package com.miamia.camera

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miamia.analysis.Gemma4LocalUiMessageResolver
import androidx.lifecycle.viewModelScope
import com.miamia.analysis.ingredientsegment.AnalysisSubmissionGate
import com.miamia.analysis.ingredientsegment.IngredientSegmentPreparationService
import com.miamia.analysis.ingredientsegment.SubmissionBlockedReason
import com.miamia.additives.AnalysisDisplayResult
import com.miamia.additives.BuildAdditiveKpiDisplay
import com.miamia.composition.AnalyzeCompositionResult
import com.miamia.composition.CompositionAnalysisEngine
import com.miamia.composition.CompositionMessages
import com.miamia.composition.CompositionResultValidator
import com.miamia.composition.GemmaErrorCode
import com.miamia.core.FeatureConfig
import com.miamia.ingredients.ExtractedIngredientMapper
import com.miamia.ingredients.RetryScanActionHandler
import com.miamia.ingredients.ScanFailureMessageBuilder
import com.miamia.permissions.CameraPermissionHandler
import com.miamia.recognition.IngredientRecognitionCoordinator
import com.miamia.recognition.ScanFailureClassifier
import com.miamia.gemma4local.Gemma4LocalAvailabilityChecker
import com.miamia.home.HomeLlmFailureCategory
import com.miamia.home.MediaPipeLlmAvailabilityProbe
import com.miamia.home.MediaPipeStatusViewState
import com.miamia.welcome.WelcomeDisplayLogger
import com.miamia.welcome.WelcomeMessagePolicy
import com.miamia.welcome.WelcomeMessageProvider
import com.miamia.welcome.WelcomeMessageSelector
import com.miamia.welcome.WelcomeMessageUiState
import com.miamia.welcome.toUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    /**
     * Segment ingrédients validé aligné sur [ScanState.BilanReady.rawTranscript] (spec 002 / SC-005).
     * [null] tant qu’aucun bilan prêt ; réinitialisé au redémarrage de scan ou si le flux quitte le bilan valide.
     */
    private val _lastValidatedSegmentForHealth = MutableStateFlow<String?>(null)
    val lastValidatedSegmentForHealth: StateFlow<String?> = _lastValidatedSegmentForHealth.asStateFlow()

    private val _previewSession = MutableStateFlow(0)
    val previewSession: StateFlow<Int> = _previewSession.asStateFlow()

    private val _welcomeUiState = MutableStateFlow<WelcomeMessageUiState>(WelcomeMessageUiState.Hidden)
    val welcomeUiState: StateFlow<WelcomeMessageUiState> = _welcomeUiState.asStateFlow()
    private val _mediaPipeStatus = MutableStateFlow(MediaPipeStatusViewState.checking())
    val mediaPipeStatus: StateFlow<MediaPipeStatusViewState> = _mediaPipeStatus.asStateFlow()

    private val _additiveKpiDisplay = MutableStateFlow<AnalysisDisplayResult?>(null)
    val additiveKpiDisplay: StateFlow<AnalysisDisplayResult?> = _additiveKpiDisplay.asStateFlow()

    private var bindJob: Job? = null
    private var inFlightScan = false

    private var lastRawTranscript: String? = null
    private var lastItemsPreview: List<String>? = null
    private var pendingAnalysisSegment: String? = null
    private var pendingScanId: String? = null
    private val mediaPipeApiProbe = MediaPipeLlmAvailabilityProbe()
    private val segmentPreparationService = IngredientSegmentPreparationService()
    private val submissionGate = AnalysisSubmissionGate()

    private val _captureRouteActive = MutableStateFlow(false)

    private val _streamingBilan = MutableStateFlow<StreamingBilanState>(StreamingBilanState.Idle)
    val streamingBilan: StateFlow<StreamingBilanState> = _streamingBilan.asStateFlow()

    private val _navigateToLlmResult = MutableSharedFlow<CameraLlmResultNavigation>(extraBufferCapacity = 1)
    val navigateToLlmResult: SharedFlow<CameraLlmResultNavigation> = _navigateToLlmResult.asSharedFlow()

    fun setCaptureRouteActive(active: Boolean) {
        _captureRouteActive.value = active
    }

    @VisibleForTesting
    fun debugOverrideScanStateForTests(state: ScanState) {
        _scanState.value = state
    }

    /**
     * Réservé aux AndroidTest (Feature D / UGE-D-FR-002) : force un état `Displayed` du
     * flow `welcomeUiState` afin d'exercer le scénario « policy yields a welcome message »
     * sans dépendre du catalogue de production. Aucune utilisation en code de production.
     */
    @VisibleForTesting
    fun debugForceWelcomeDisplayedForTests(
        text: String = "Bienvenue (test)",
        messageId: String = "test-welcome-id"
    ) {
        _welcomeUiState.value = WelcomeMessageUiState.Displayed(text = text, messageId = messageId)
    }

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
        _lastValidatedSegmentForHealth.value = null
        _previewSession.value += 1
        clearAdditiveKpiDisplay()
        _scanState.value = if (permissionHandler.hasCameraPermission(getApplication())) {
            ScanState.CameraReady
        } else {
            ScanState.PermissionDenied
        }
        refreshMediaPipeAvailability()
    }

    private fun clearAdditiveKpiDisplay() {
        _additiveKpiDisplay.value = null
    }

    fun onLoginSucceeded(userId: String = "connected-user") {
        _welcomeUiState.value = welcomePolicy.onLoginSucceeded(userId).toUiState()
        refreshMediaPipeAvailability()
    }

    fun onGemmaModelImported() {
        refreshMediaPipeAvailability()
    }

    fun canCapturePhoto(): Boolean {
        if (inFlightScan) return false
        if (_scanState.value is ScanState.CompositionAnalyzing) return false
        return _scanState.value == ScanState.PreviewActive
    }

    private fun navigateToResultScreen() {
        val signal = CameraLlmResultNavigation(body = "", isError = false)
        if (!_navigateToLlmResult.tryEmit(signal)) {
            viewModelScope.launch {
                _navigateToLlmResult.emit(signal)
            }
        }
    }

    fun resetStreamingBilan() {
        _streamingBilan.value = StreamingBilanState.Idle
        _previewSession.value += 1
        _scanState.value = if (permissionHandler.hasCameraPermission(getApplication())) {
            ScanState.CameraReady
        } else {
            ScanState.PermissionDenied
        }
    }

    fun buildLlmResultFallbackPayload(): CameraLlmResultNavigation {
        val transcript = lastRawTranscript?.trim().orEmpty()
        return if (transcript.isNotEmpty()) {
            CameraLlmResultNavigation(
                body = "Analyse indisponible pour l'instant. Texte reconnu :\n\n$transcript",
                isError = true,
                errorCategoryWire = HomeLlmFailureCategory.NON_ANALYSABLE_RESPONSE.wireValue
            )
        } else {
            CameraLlmResultNavigation(
                body = "Analyse indisponible pour le moment. Revenez a la capture et relancez l'analyse.",
                isError = true,
                errorCategoryWire = HomeLlmFailureCategory.NON_ANALYSABLE_RESPONSE.wireValue
            )
        }
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
        clearAdditiveKpiDisplay()
        _lastValidatedSegmentForHealth.value = null
        _scanState.value = ScanState.Success(transcriptText = raw, items = items)
    }

    fun tapToFocus(x: Float, y: Float) {
        captureController.focusOnPoint(x, y)
    }

    fun attachPreview(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        if (!permissionHandler.hasCameraPermission(getApplication())) {
            _scanState.value = ScanState.PermissionDenied
            return
        }
        if (_scanState.value is ScanState.CompositionAnalyzing) return
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
        _lastValidatedSegmentForHealth.value = null
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
                    val extraction = segmentPreparationService.prepare(result.scanId, transcriptText)
                    lastRawTranscript = transcriptText
                    lastItemsPreview = itemLabels
                    val previewDecision = submissionGate.evaluate(
                        scanId = result.scanId,
                        extraction = extraction,
                        userConfirmed = false,
<<<<<<< HEAD
                        implicitValidationFromIngredientsFraming = _ingredientsFramingTagActive.value,
                        fullOcrTranscript = transcriptText,
=======
                        /** Validation implicite : enchaînement direct analyse LLM si le segment est exploitable (plus d’écran intermédiaire). */
                        implicitValidationFromIngredientsFraming = true
>>>>>>> f2d806ea7921ea48dd8d92efc6c8fa3783e1ba2c
                    )
                    if (!previewDecision.submissionAllowed) {
                        if (previewDecision.blockedReason == SubmissionBlockedReason.USER_REJECTED) {
                            pendingAnalysisSegment = previewDecision.segmentPreview
                            pendingScanId = result.scanId
                            _scanState.value = ScanState.SegmentConfirmationRequired(
                                segmentPreview = previewDecision.segmentPreview,
                                itemsPreview = itemLabels,
                            )
                        } else {
                            _scanState.value = ScanState.Error(
                                "Texte non exploitable pour l'analyse. Reprenez la photo ou editez le texte.",
                            )
                        }
                        return@launch
                    }
<<<<<<< HEAD
                    pendingAnalysisSegment = previewDecision.segmentPreview
                    pendingScanId = result.scanId
                    if (previewDecision.implicitValidationFromIngredientsFraming) {
                        confirmSegmentAndAnalyze()
                    } else {
                        _scanState.value = ScanState.SegmentConfirmationRequired(
                            segmentPreview = previewDecision.segmentPreview,
                            itemsPreview = itemLabels,
                        )
                    }
=======
                    if (!previewDecision.submissionAllowed) {
                        _scanState.value = ScanState.Error(
                            "Analyse bloquee: segment non exploitable pour l'instant."
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
                    confirmSegmentAndAnalyze()
>>>>>>> f2d806ea7921ea48dd8d92efc6c8fa3783e1ba2c
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
        val scanId = pendingScanId ?: return
        val sourceOcr = lastRawTranscript ?: pendingAnalysisSegment ?: return
        val items = lastItemsPreview.orEmpty()
        val extraction = segmentPreparationService.prepare(scanId, sourceOcr)
        val decision = submissionGate.evaluate(
            scanId = scanId,
            extraction = extraction,
            userConfirmed = true,
            implicitValidationFromIngredientsFraming = false,
            fullOcrTranscript = sourceOcr,
        )
        if (!decision.submissionAllowed) {
            _scanState.value = ScanState.Error("Analyse bloquee: confirmation ou segment invalide.")
            return
        }
        val engine = compositionEngine
        if (engine == null) {
            _lastValidatedSegmentForHealth.value = null
            _scanState.value = ScanState.Success(
                transcriptText = decision.segmentPreview,
                items = items
            )
            return
        }
        _scanState.value = ScanState.CompositionAnalyzing()
        viewModelScope.launch {
            runCompositionStage(engine, decision.segmentPreview, items)
        }
    }

    fun rejectSegmentConfirmation() {
        _lastValidatedSegmentForHealth.value = null
        _scanState.value = ScanState.Error("Analyse annulee. Vous pouvez reprendre une photo.")
    }

    private suspend fun runCompositionStage(
        engine: CompositionAnalysisEngine,
        rawText: String,
        itemsPreview: List<String>
    ) {
        _streamingBilan.value = StreamingBilanState.Streaming()
        navigateToResultScreen()

        val inferenceStart = SystemClock.elapsedRealtime()
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
            _streamingBilan.value = StreamingBilanParser.parsePartial(partial)
        }
        val inferenceTimeMs = SystemClock.elapsedRealtime() - inferenceStart
        clearAdditiveKpiDisplay()
        when (outcome) {
            is AnalyzeCompositionResult.BilanSuccess -> {
                val emptyReject = CompositionResultValidator.rejectEmptyStructure(outcome.bilan)
                if (emptyReject != null) {
                    _lastValidatedSegmentForHealth.value = null
                    _streamingBilan.value = StreamingBilanState.Error(
                        message = emptyReject.message,
                        errorCategory = HomeLlmFailureCategory.NON_ANALYSABLE_RESPONSE.wireValue
                    )
                    if (!_captureRouteActive.value) {
                        _scanState.value = ScanState.CompositionLimit(
                            message = emptyReject.message,
                            rawTranscript = rawText
                        )
                    }
                } else {
                    when (
                        val v = CompositionResultValidator.validateAgainstSource(
                            outcome.bilan,
                            rawText,
                            outcome.rawModelOutput,
                        )
                    ) {
                        is AnalyzeCompositionResult.BilanSuccess -> {
                            _lastValidatedSegmentForHealth.value = rawText
                            _streamingBilan.value = StreamingBilanState.Complete(
                                bilan = v.bilan,
                                rawTranscript = rawText,
                                inferenceTimeMs = inferenceTimeMs
                            )
                            if (!_captureRouteActive.value) {
                                val kpi = withContext(Dispatchers.Default) {
                                    BuildAdditiveKpiDisplay(
                                        bilan = v.bilan,
                                        rawLlmTextForParsing = v.rawModelOutput,
                                        validatedIngredientSegment = rawText,
                                    )
                                }
                                _additiveKpiDisplay.value = kpi
                                _scanState.value = ScanState.BilanReady(
                                    bilan = v.bilan,
                                    rawTranscript = rawText,
                                    itemsPreview = itemsPreview,
                                    inferenceTimeMs = inferenceTimeMs
                                )
                            }
                        }
                        is AnalyzeCompositionResult.CompositionLimit -> {
                            _lastValidatedSegmentForHealth.value = null
                            _streamingBilan.value = StreamingBilanState.Error(
                                message = v.message,
                                errorCategory = HomeLlmFailureCategory.NON_ANALYSABLE_RESPONSE.wireValue
                            )
                            if (!_captureRouteActive.value) {
                                _scanState.value = ScanState.CompositionLimit(
                                    message = v.message,
                                    rawTranscript = rawText
                                )
                            }
                        }
                        else -> {
                            _lastValidatedSegmentForHealth.value = null
                            _streamingBilan.value = StreamingBilanState.Error(
                                message = CompositionMessages.COMPOSITION_LIMIT_GENERIC,
                                errorCategory = HomeLlmFailureCategory.NON_ANALYSABLE_RESPONSE.wireValue
                            )
                            if (!_captureRouteActive.value) {
                                _scanState.value = ScanState.CompositionLimit(
                                    CompositionMessages.COMPOSITION_LIMIT_GENERIC,
                                    rawText
                                )
                            }
                        }
                    }
                }
            }
            is AnalyzeCompositionResult.GemmaError -> {
                _lastValidatedSegmentForHealth.value = null
                val uiMessage = Gemma4LocalUiMessageResolver.resolve(outcome.code, outcome.message)
                val wire = if (outcome.code == GemmaErrorCode.GEMMA_TIMEOUT) {
                    HomeLlmFailureCategory.TIMEOUT.wireValue
                } else {
                    HomeLlmFailureCategory.RUNTIME_UNAVAILABLE.wireValue
                }
                _streamingBilan.value = StreamingBilanState.Error(
                    message = uiMessage,
                    errorCategory = wire
                )
                if (!_captureRouteActive.value) {
                    _scanState.value = ScanState.GemmaUnavailable(
                        code = outcome.code,
                        message = uiMessage,
                        rawTranscript = rawText
                    )
                }
            }
            is AnalyzeCompositionResult.CompositionLimit -> {
                _lastValidatedSegmentForHealth.value = null
                _streamingBilan.value = StreamingBilanState.Error(
                    message = outcome.message,
                    errorCategory = HomeLlmFailureCategory.NON_ANALYSABLE_RESPONSE.wireValue
                )
                if (!_captureRouteActive.value) {
                    _scanState.value = ScanState.CompositionLimit(
                        message = outcome.message,
                        rawTranscript = rawText
                    )
                }
            }
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
            compositionEngine: CompositionAnalysisEngine? = null,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(CameraViewModel::class.java))
                    return CameraViewModel(
                        application,
                        coordinator,
                        compositionEngine,
                    ) as T
                }
            }
        }
    }
}
