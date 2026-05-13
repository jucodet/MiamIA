package com.miamia.camera

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.miamia.home.HomeSpacingRules
import com.miamia.home.MediaPipeStatusIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val CapturePrimaryActionLabel = "Y a quoi là-dedans ?"

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onCreateTempImage: () -> File,
    onRequestCameraPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onChooseGemmaModel: () -> Unit
) {
    val state by viewModel.scanState.collectAsState()
    val additiveKpi by viewModel.additiveKpiDisplay.collectAsState()
    val previewSession by viewModel.previewSession.collectAsState()
    val mediaPipeStatus by viewModel.mediaPipeStatus.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Le flag suit la présence effective de la route capture dans la navigation Compose.
    DisposableEffect(Unit) {
        viewModel.setCaptureRouteActive(true)
        onDispose { viewModel.setCaptureRouteActive(false) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .widthIn(max = 720.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MediaPipeStatusIndicator(
            viewState = mediaPipeStatus,
            modifier = Modifier.fillMaxWidth()
        )

        when (state) {
            ScanState.PermissionDenied -> {
                Text("L'accès à la caméra est nécessaire pour afficher l'aperçu réel.")
                Button(
                    onClick = onRequestCameraPermission,
                    modifier = Modifier.testTag("request_camera_permission")
                ) {
                    Text("Autoriser la caméra")
                }
                Button(
                    onClick = onOpenAppSettings,
                    modifier = Modifier.testTag("open_app_settings")
                ) {
                    Text("Ouvrir les paramètres")
                }
            }

            is ScanState.CameraUnavailable -> {
                val unavailable = state as ScanState.CameraUnavailable
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    verticalArrangement = Arrangement.spacedBy(HomeSpacingRules.standardFixedSpacing)
                ) {
                    Text(
                        text = "Caméra indisponible: ${unavailable.reason ?: "erreur inconnue"}",
                        modifier = Modifier
                            .semantics { contentDescription = "Message caméra indisponible avec détail" }
                            .testTag("camera_unavailable_message")
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true)
                            .heightIn(min = 200.dp, max = 480.dp)
                            .testTag("photo_preview_placeholder"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Prévisualisation désactivée — la caméra n'est pas disponible.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    CaptureActionBar(
                        onCapture = { viewModel.capturePhoto(onCreateTempImage()) },
                        canCapture = viewModel.canCapturePhoto()
                    )
                    Button(onClick = viewModel::onRetry, modifier = Modifier.testTag("retry_camera")) {
                        Text("Réessayer")
                    }
                }
            }

            is ScanState.CompositionAnalyzing -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.testTag("composition_analyzing_spinner")
                    )
                    Text(
                        "Analyse en cours — résultat sur l'écran suivant…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("llm_loading_overlay_label")
                    )
                }
            }

            is ScanState.BilanReady -> {
                val bilanState = state as ScanState.BilanReady
                var showRaw by remember { mutableStateOf(false) }
                ScrollableReviewWithPrimaryActions(
                    scrollTestTag = "bilan_review_scroll",
                    scrollableContent = {
                        BilanResultCard(
                            bilan = bilanState.bilan,
                            rawTranscript = bilanState.rawTranscript,
                            additiveKpi = additiveKpi,
                            showRaw = showRaw,
                            onToggleRaw = { showRaw = !showRaw },
                            inferenceTimeMs = bilanState.inferenceTimeMs
                        )
                    },
                    footerActions = {
                        Button(onClick = viewModel::onRetry, modifier = Modifier.testTag("new_scan_button")) {
                            Text("Nouveau scan")
                        }
                    }
                )
            }

            is ScanState.GemmaUnavailable -> {
                val g = state as ScanState.GemmaUnavailable
                ScrollableReviewWithPrimaryActions(
                    scrollTestTag = "gemma_unavailable_scroll",
                    scrollableContent = {
                        Text("Analyse composition", style = MaterialTheme.typography.titleMedium)
                        Text(g.message, modifier = Modifier.testTag("gemma_error_message"))
                    },
                    footerActions = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = viewModel::retryCompositionAnalysis,
                                modifier = Modifier.testTag("retry_composition_button")
                            ) {
                                Text("Réessayer l'analyse")
                            }
                            OutlinedButton(
                                onClick = viewModel::showRawTranscriptOnly,
                                modifier = Modifier.testTag("show_raw_transcript_button")
                            ) {
                                Text("Voir le texte brut")
                            }
                            OutlinedButton(
                                onClick = onChooseGemmaModel,
                                modifier = Modifier.testTag("choose_gemma_model_button")
                            ) {
                                Text("Choisir un modele Gemma")
                            }
                            Button(onClick = viewModel::onRetry, modifier = Modifier.testTag("new_scan_button")) {
                                Text("Nouveau scan")
                            }
                        }
                    }
                )
            }

            is ScanState.CompositionLimit -> {
                val c = state as ScanState.CompositionLimit
                ScrollableReviewWithPrimaryActions(
                    scrollTestTag = "composition_limit_scroll",
                    scrollableContent = {
                        Text(c.message, style = MaterialTheme.typography.bodyLarge)
                    },
                    footerActions = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = viewModel::showRawTranscriptOnly,
                                modifier = Modifier.testTag("composition_limit_show_raw")
                            ) {
                                Text("Voir le texte capturé")
                            }
                            Button(onClick = viewModel::onRetry, modifier = Modifier.testTag("new_scan_button")) {
                                Text("Nouveau scan")
                            }
                        }
                    }
                )
            }

            is ScanState.Success -> {
                val success = state as ScanState.Success
                ScrollableReviewWithPrimaryActions(
                    scrollTestTag = "captured_review_scroll",
                    scrollableContent = {
                        Text("Analyse terminée", style = MaterialTheme.typography.titleMedium)
                        Text(success.transcriptText, style = MaterialTheme.typography.bodyMedium)
                        success.items.forEach { line ->
                            Text("- $line", style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    footerActions = {
                        Button(onClick = viewModel::onRetry, modifier = Modifier.testTag("new_scan_button")) {
                            Text("Nouveau scan")
                        }
                    }
                )
            }
            is ScanState.Empty -> {
                Text((state as ScanState.Empty).message)
                Button(onClick = viewModel::onRetry, modifier = Modifier.testTag("retry_after_empty")) {
                    Text("Réessayer")
                }
            }

            is ScanState.Error -> {
                Text("Erreur: ${(state as ScanState.Error).message}")
                Button(onClick = viewModel::onRetry, modifier = Modifier.testTag("retry_after_error")) {
                    Text("Réessayer")
                }
            }

            ScanState.CameraReady,
            ScanState.PreviewInitializing,
            ScanState.PreviewActive,
            ScanState.Capturing,
            ScanState.Analyzing -> {
                var focusTapOffset by remember { mutableStateOf<Offset?>(null) }
                var focusTapKey by remember { mutableIntStateOf(0) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    verticalArrangement = Arrangement.spacedBy(HomeSpacingRules.standardFixedSpacing)
                ) {
                    key(previewSession) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = true)
                                .heightIn(min = 220.dp, max = 480.dp)
                                .testTag("photo_preview_box")
                        ) {
                            CameraPreviewBox(
                                onPreviewViewCreated = { previewView ->
                                    viewModel.attachPreview(previewView, lifecycleOwner)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            if (state == ScanState.PreviewActive) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectTapGestures { offset ->
                                                focusTapOffset = offset
                                                focusTapKey++
                                                viewModel.tapToFocus(offset.x, offset.y)
                                            }
                                        }
                                )
                            }
                            focusTapOffset?.let { offset ->
                                key(focusTapKey) {
                                    FocusRingIndicator(
                                        position = offset,
                                        onAnimationEnd = { focusTapOffset = null }
                                    )
                                }
                            }
                            if (state is ScanState.PreviewInitializing) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            if (state is ScanState.Capturing || state is ScanState.Analyzing) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator()
                                        Text("Analyse en cours…")
                                    }
                                }
                            }
                        }
                    }

                    CaptureActionBar(
                        onCapture = { viewModel.capturePhoto(onCreateTempImage()) },
                        canCapture = viewModel.canCapturePhoto()
                    )

                    if (state != ScanState.PreviewActive) {
                        Text(
                            text = when (state) {
                                ScanState.PreviewInitializing -> "Démarrage de l'aperçu caméra…"
                                ScanState.Capturing -> "Capture en cours…"
                                ScanState.Analyzing -> "Traitement de l'image…"
                                else -> "Préparez le cadrage"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.testTag("capture_scan_status_text")
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bande d'action de capture (incrément 020) — placée **sous** l'aperçu vidéo, en dehors de
 * la `Box` portant le test tag `photo_preview_box` / `photo_preview_placeholder`, afin de
 * garantir CR-FR-009 (aucun bouton ne recouvre l'aperçu) et CR-FR-010 (bande dédiée).
 *
 * Un `Spacer` interne assure une séparation visuelle non ambiguë (≥ 16 dp cumulés avec
 * l'espacement de la `Column` parente) entre l'aperçu et le premier bouton.
 */
@Composable
private fun CaptureActionBar(
    onCapture: () -> Unit,
    canCapture: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onCapture,
            enabled = canCapture,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("capture_photo_button")
        ) {
            Text(CapturePrimaryActionLabel)
        }
    }
}

@Composable
private fun ColumnScope.ScrollableReviewWithPrimaryActions(
    scrollTestTag: String,
    scrollableContent: @Composable () -> Unit,
    footerActions: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .testTag(scrollTestTag),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            scrollableContent()
        }
        footerActions()
    }
}

@Composable
private fun FocusRingIndicator(
    position: Offset,
    onAnimationEnd: () -> Unit
) {
    val alpha = remember { Animatable(1f) }
    val scale = remember { Animatable(1.4f) }
    val ringRadiusDp = 28.dp
    val strokeWidthDp = 2.dp
    val density = LocalDensity.current
    val ringRadiusPx = with(density) { ringRadiusDp.toPx() }
    val strokeWidthPx = with(density) { strokeWidthDp.toPx() }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, tween(durationMillis = 250)) }
        delay(700)
        alpha.animateTo(0f, tween(durationMillis = 300))
        onAnimationEnd()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.White,
            radius = ringRadiusPx * scale.value,
            center = position,
            alpha = alpha.value,
            style = Stroke(width = strokeWidthPx)
        )
    }
}
