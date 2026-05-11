package com.foodgpt.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.foodgpt.additives.ui.AdditiveKpiPanel
import com.foodgpt.home.HomeSpacingRules
import com.foodgpt.home.MediaPipeStatusIndicator
import com.foodgpt.welcome.WelcomeMessageUiState
import java.io.File

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
    val welcomeState by viewModel.welcomeUiState.collectAsState()
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
        if (welcomeState is WelcomeMessageUiState.Displayed) {
            Text(
                text = (welcomeState as WelcomeMessageUiState.Displayed).text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("welcome_message_banner")
            )
        }

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
                Text(
                    text = "Caméra indisponible: ${unavailable.reason ?: "erreur inconnue"}",
                    modifier = Modifier
                        .semantics { contentDescription = "Message caméra indisponible avec détail" }
                        .testTag("camera_unavailable_message")
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .testTag("photo_preview_placeholder"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Prévisualisation désactivée — la caméra n'est pas disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Button(
                    onClick = { viewModel.capturePhoto(onCreateTempImage()) },
                    enabled = viewModel.canCapturePhoto(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capture_photo_button")
                ) {
                    Text("Prendre la photo")
                }
                OutlinedButton(
                    onClick = viewModel::runCameraTabLlmMockTest,
                    enabled = viewModel.canRunCameraTabLlmTest(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Lancer le test LLM (données de démonstration)" }
                        .testTag("camera_tab_llm_test_button")
                ) {
                    Text("Test LLM")
                }
                Button(onClick = viewModel::onRetry, modifier = Modifier.testTag("retry_camera")) {
                    Text("Réessayer")
                }
            }

            is ScanState.CompositionAnalyzing -> {
                val analyzing = state as ScanState.CompositionAnalyzing
                key(previewSession) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .testTag("photo_preview_box")
                    ) {
                        CameraPreviewBox(
                            onPreviewViewCreated = { previewView ->
                                viewModel.attachPreview(previewView, lifecycleOwner)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    "Analyse LLM en cours…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.testTag("llm_loading_overlay_label")
                                )
                                if (analyzing.partialResponse.isNotBlank()) {
                                    Text(
                                        text = analyzing.partialResponse,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 120.dp)
                                            .verticalScroll(rememberScrollState())
                                            .testTag("composition_streaming_draft")
                                    )
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = { viewModel.capturePhoto(onCreateTempImage()) },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capture_photo_button")
                ) {
                    Text("Prendre la photo")
                }
                OutlinedButton(
                    onClick = viewModel::runCameraTabLlmMockTest,
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Lancer le test LLM (données de démonstration)" }
                        .testTag("camera_tab_llm_test_button")
                ) {
                    Text("Test LLM")
                }
            }

            is ScanState.BilanReady -> {
                val bilanState = state as ScanState.BilanReady
                var showRaw by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Bilan composition", style = MaterialTheme.typography.titleMedium)
                    Text("Ingrédients", style = MaterialTheme.typography.titleSmall)
                    bilanState.bilan.ingredientLines.forEach { line ->
                        Text("• $line", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("Analyse", style = MaterialTheme.typography.titleSmall)
                    Text(bilanState.bilan.compositionAnalysis, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        bilanState.bilan.disclaimer,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    additiveKpi?.let { kpi ->
                        Text("Additifs", style = MaterialTheme.typography.titleSmall)
                        AdditiveKpiPanel(
                            result = kpi,
                            onRequestShowRaw = { showRaw = true },
                        )
                    }
                    OutlinedButton(
                        onClick = { showRaw = !showRaw },
                        modifier = Modifier.testTag("toggle_raw_transcript")
                    ) {
                        Text(if (showRaw) "Masquer le texte original" else "Voir le texte original")
                    }
                    if (showRaw) {
                        Text(
                            bilanState.rawTranscript,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("raw_transcript_secondary")
                        )
                    }
                    Button(onClick = viewModel::onRetry, modifier = Modifier.testTag("new_scan_button")) {
                        Text("Nouveau scan")
                    }
                }
            }

            is ScanState.GemmaUnavailable -> {
                val g = state as ScanState.GemmaUnavailable
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Analyse composition", style = MaterialTheme.typography.titleMedium)
                    Text(g.message, modifier = Modifier.testTag("gemma_error_message"))
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

            is ScanState.CompositionLimit -> {
                val c = state as ScanState.CompositionLimit
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(c.message, style = MaterialTheme.typography.bodyLarge)
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

            is ScanState.Success -> {
                Text("Analyse terminée")
                Text((state as ScanState.Success).transcriptText)
                (state as ScanState.Success).items.forEach { Text("- $it") }
                Button(onClick = viewModel::onRetry, modifier = Modifier.testTag("new_scan_button")) {
                    Text("Nouveau scan")
                }
            }
            is ScanState.SegmentConfirmationRequired -> {
                val confirmation = state as ScanState.SegmentConfirmationRequired
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Verifier la ligne ingredients", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = confirmation.segmentPreview,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("segment_preview_text")
                    )
                    Button(
                        onClick = viewModel::confirmSegmentAndAnalyze,
                        modifier = Modifier.testTag("confirm_segment_button")
                    ) {
                        Text("Confirmer et analyser")
                    }
                    OutlinedButton(
                        onClick = viewModel::rejectSegmentConfirmation,
                        modifier = Modifier.testTag("reject_segment_button")
                    ) {
                        Text("Reprendre la photo")
                    }
                }
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
                Column(verticalArrangement = Arrangement.spacedBy(HomeSpacingRules.standardFixedSpacing)) {
                    key(previewSession) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                                .testTag("photo_preview_box")
                        ) {
                            CameraPreviewBox(
                                onPreviewViewCreated = { previewView ->
                                    viewModel.attachPreview(previewView, lifecycleOwner)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
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

                    Button(
                        onClick = { viewModel.capturePhoto(onCreateTempImage()) },
                        enabled = viewModel.canCapturePhoto(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("capture_photo_button")
                    ) {
                        Text("Prendre la photo")
                    }
                    OutlinedButton(
                        onClick = viewModel::runCameraTabLlmMockTest,
                        enabled = viewModel.canRunCameraTabLlmTest(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Lancer le test LLM (données de démonstration)" }
                            .testTag("camera_tab_llm_test_button")
                    ) {
                        Text("Test LLM")
                    }

                    Text(
                        text = when (state) {
                            ScanState.PreviewActive -> "Aperçu caméra actif"
                            ScanState.PreviewInitializing -> "Démarrage de l'aperçu caméra…"
                            ScanState.Capturing -> "Capture en cours…"
                            ScanState.Analyzing -> "Traitement de l'image…"
                            else -> "Préparez le cadrage"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
