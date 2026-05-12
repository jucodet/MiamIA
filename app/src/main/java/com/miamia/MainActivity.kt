package com.miamia

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModelProvider
import com.miamia.camera.CameraScreen
import com.miamia.camera.CameraViewModel
import com.miamia.camera.ScanState
import com.miamia.composition.Gemma4LocalCompositionEngine
import com.miamia.data.repository.ScanSessionRepository
import com.miamia.gemma4local.DeviceClassResolver
import com.miamia.gemma4local.Gemma4LocalAvailabilityChecker
import com.miamia.gemma4local.Gemma4LocalClient
import com.miamia.gemma4local.Gemma4LocalErrorMapper
import com.miamia.gemma4local.Gemma4LocalMetricsLogger
import com.miamia.gemma4local.GemmaModelImportManager
import com.miamia.gemma4local.Gemma4LocalRequestMapper
import com.miamia.gemma4local.HybridGemma4LocalGateway
import com.miamia.healthcritique.HealthCritiqueResultScreen
import com.miamia.healthcritique.HealthCritiqueViewModel
import com.miamia.home.CompositionEngineHomeLlmMockRunner
import com.miamia.home.HomeSpecPriorityResolver
import com.miamia.permissions.CameraPermissionHandler
import com.miamia.recognition.AiEdgeGalleryRecognizer
import com.miamia.recognition.DeviceAiCapabilityDetector
import com.miamia.recognition.IngredientExtractionPipeline
import com.miamia.recognition.IngredientRecognitionCoordinator
import com.miamia.recognition.LocalOcrFallbackRecognizer
import com.miamia.recognition.RecognitionEngineSelector
import com.miamia.navigation.CameraFlowRoutes
import com.miamia.navigation.OnboardingRoutes
import com.miamia.onboarding.LlmModelReadinessState
import com.miamia.onboarding.ModelDownloadOnboardingScreen
import com.miamia.onboarding.ModelDownloadViewModel
import com.miamia.onboarding.ModelDownloadWaitingScreen
import com.miamia.onboarding.NetworkOfflineScreen
import com.miamia.result.LlmResultScreen
import com.miamia.scan.TemporaryImageManager
import com.miamia.ui.theme.MiamIATheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val permissionHandler = CameraPermissionHandler()
    private val modelImportManager by lazy { GemmaModelImportManager(applicationContext) }

    private lateinit var cameraViewModel: CameraViewModel

    private lateinit var healthCritiqueViewModel: HealthCritiqueViewModel

    private lateinit var modelDownloadViewModel: ModelDownloadViewModel

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!::cameraViewModel.isInitialized) return@registerForActivityResult
        if (granted) {
            cameraViewModel.onPermissionGranted()
        } else {
            cameraViewModel.onPermissionDenied()
        }
    }

    private val chooseGemmaModelLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        runCatching { contentResolver.takePersistableUriPermission(uri, takeFlags) }
        val hadLocalModel = modelImportManager.hasLocalModel()
        val imported = modelImportManager.importFromUri(uri, overwriteExisting = false)
        if (imported) {
            if (::cameraViewModel.isInitialized) {
                cameraViewModel.onGemmaModelImported()
            }
            val message = if (hadLocalModel) {
                "Modele deja present, reutilisation sans re-telechargement."
            } else {
                "Modele Gemma importe."
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else {
            val reason = modelImportManager.getLastImportErrorMessage() ?: "Import du modele impossible."
            Toast.makeText(this, reason, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MiamIAApplication

        setContent {
            // La spec 012 definit la reference d'ordre UI pour l'accueil.
            HomeSpecPriorityResolver.resolveHomeUiOrderSpec()
            val imageManager = remember { TemporaryImageManager(applicationContext) }
            var uiReady by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                modelDownloadViewModel = ViewModelProvider(
                    this@MainActivity
                )[ModelDownloadViewModel::class.java]

                val coordinator = withContext(Dispatchers.IO) {
                    val db = app.database
                    val repository = ScanSessionRepository(db.scanSessionDao())
                    val capabilityDetector = DeviceAiCapabilityDetector(applicationContext)
                    IngredientRecognitionCoordinator(
                        engineSelector = RecognitionEngineSelector(
                            capabilityDetector = capabilityDetector,
                            aiEdgeGalleryRecognizer = AiEdgeGalleryRecognizer(),
                            localOcrFallbackRecognizer = LocalOcrFallbackRecognizer(applicationContext)
                        ),
                        extractionPipeline = IngredientExtractionPipeline(),
                        repository = repository
                    )
                }
                val localGateway = HybridGemma4LocalGateway(applicationContext)
                val localClient = Gemma4LocalClient(
                    availabilityChecker = Gemma4LocalAvailabilityChecker(localGateway),
                    requestMapper = Gemma4LocalRequestMapper(),
                    errorMapper = Gemma4LocalErrorMapper(),
                    metricsLogger = Gemma4LocalMetricsLogger(),
                    deviceClassResolver = DeviceClassResolver(applicationContext),
                    gateway = localGateway
                )
                val compositionEngine = Gemma4LocalCompositionEngine(localClient)
                val homeLlmRunner = CompositionEngineHomeLlmMockRunner(compositionEngine)
                cameraViewModel = ViewModelProvider(
                    this@MainActivity,
                    CameraViewModel.factory(
                        application,
                        coordinator,
                        compositionEngine,
                        homeLlmRunner
                    )
                )[CameraViewModel::class.java]
                healthCritiqueViewModel = ViewModelProvider(
                    this@MainActivity,
                    HealthCritiqueViewModel.factory(applicationContext)
                )[HealthCritiqueViewModel::class.java]
                cameraViewModel.onLoginSucceeded()
                if (permissionHandler.hasCameraPermission(this@MainActivity)) {
                    cameraViewModel.onPermissionGranted()
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
                uiReady = true
            }
            if (!uiReady) {
                MiamIATheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                MiamIATheme {
                    LaunchedEffect(Unit) {
                        cameraViewModel.lastValidatedSegmentForHealth.collectLatest { segment ->
                            healthCritiqueViewModel.setValidatedSegmentFromScan(segment)
                        }
                    }
                    val onboardingState by modelDownloadViewModel.state.collectAsState()
                    val needsOnboarding = onboardingState !is LlmModelReadinessState.Ready
                    val startDest = if (needsOnboarding) OnboardingRoutes.Confirm else CameraFlowRoutes.Capture

                    val cameraNavController = rememberNavController()
                    LaunchedEffect(cameraViewModel) {
                        cameraViewModel.navigateToLlmResult.collect {
                            cameraNavController.navigate(CameraFlowRoutes.LlmResult)
                        }
                    }
                    LaunchedEffect(healthCritiqueViewModel) {
                        healthCritiqueViewModel.navigateToResult.collect {
                            cameraNavController.navigate(CameraFlowRoutes.HealthCritiqueResult)
                        }
                    }
                    LaunchedEffect(onboardingState) {
                        when (onboardingState) {
                            is LlmModelReadinessState.Ready -> {
                                if (cameraNavController.currentDestination?.route != CameraFlowRoutes.Capture) {
                                    cameraNavController.navigate(CameraFlowRoutes.Capture) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                            is LlmModelReadinessState.Offline -> {
                                if (cameraNavController.currentDestination?.route != OnboardingRoutes.Offline) {
                                    cameraNavController.navigate(OnboardingRoutes.Offline) {
                                        popUpTo(OnboardingRoutes.Confirm) { inclusive = true }
                                    }
                                }
                            }
                            is LlmModelReadinessState.Downloading -> {
                                if (cameraNavController.currentDestination?.route != OnboardingRoutes.Downloading) {
                                    cameraNavController.navigate(OnboardingRoutes.Downloading)
                                }
                            }
                            else -> {}
                        }
                    }
                    NavHost(
                        navController = cameraNavController,
                        startDestination = startDest,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(OnboardingRoutes.Confirm) {
                            val state = onboardingState
                            val networkType = if (state is LlmModelReadinessState.ConfirmationRequired)
                                state.networkType
                            else
                                com.miamia.onboarding.NetworkType.WIFI
                            ModelDownloadOnboardingScreen(
                                networkType = networkType,
                                isResumable = modelDownloadViewModel.isResumable,
                                onConfirm = { modelDownloadViewModel.confirmDownload() },
                                onDecline = { this@MainActivity.finishAffinity() }
                            )
                        }
                        composable(OnboardingRoutes.Offline) {
                            NetworkOfflineScreen(
                                onRetry = { modelDownloadViewModel.retryNetworkCheck() }
                            )
                        }
                        composable(OnboardingRoutes.Downloading) {
                            val state = onboardingState
                            val progress = if (state is LlmModelReadinessState.Downloading)
                                state.progress
                            else
                                null
                            val error = if (state is LlmModelReadinessState.Error) state else null
                            ModelDownloadWaitingScreen(
                                progress = progress,
                                error = error,
                                onRetry = { modelDownloadViewModel.retryDownload() }
                            )
                        }
                        composable(CameraFlowRoutes.Capture) {
                            CameraScreen(
                                viewModel = cameraViewModel,
                                onCreateTempImage = { imageManager.createTempImageFile() },
                                onRequestCameraPermission = {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                onOpenAppSettings = {
                                    startActivity(
                                        permissionHandler.buildAppSettingsIntent(this@MainActivity)
                                    )
                                },
                                onChooseGemmaModel = {
                                    chooseGemmaModelLauncher.launch(arrayOf("*/*"))
                                },
                            )
                        }
                        composable(CameraFlowRoutes.LlmResult) {
                            LlmResultScreen(
                                viewModel = cameraViewModel,
                                onBack = { cameraNavController.popBackStack() }
                            )
                        }
                        composable(CameraFlowRoutes.HealthCritiqueResult) {
                            HealthCritiqueResultScreen(
                                viewModel = healthCritiqueViewModel,
                                onBack = { cameraNavController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::cameraViewModel.isInitialized) return
        if (permissionHandler.hasCameraPermission(this) &&
            cameraViewModel.scanState.value is ScanState.PermissionDenied
        ) {
            cameraViewModel.onPermissionGranted()
        }
    }
}
