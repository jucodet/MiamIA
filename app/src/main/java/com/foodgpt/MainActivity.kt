package com.foodgpt

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
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
import com.foodgpt.camera.CameraLlmResultNavigation
import com.foodgpt.camera.CameraLlmResultPayloadStore
import com.foodgpt.camera.CameraScreen
import com.foodgpt.camera.CameraViewModel
import com.foodgpt.camera.ScanState
import com.foodgpt.composition.Gemma4LocalCompositionEngine
import com.foodgpt.data.repository.ScanSessionRepository
import com.foodgpt.gemma4local.DeviceClassResolver
import com.foodgpt.gemma4local.Gemma4LocalAvailabilityChecker
import com.foodgpt.gemma4local.Gemma4LocalClient
import com.foodgpt.gemma4local.Gemma4LocalErrorMapper
import com.foodgpt.gemma4local.Gemma4LocalMetricsLogger
import com.foodgpt.gemma4local.GemmaModelImportManager
import com.foodgpt.gemma4local.Gemma4LocalRequestMapper
import com.foodgpt.gemma4local.HybridGemma4LocalGateway
import com.foodgpt.healthcritique.HealthCritiqueViewModel
import com.foodgpt.home.CompositionEngineHomeLlmMockRunner
import com.foodgpt.home.HomeSpecPriorityResolver
import com.foodgpt.permissions.CameraPermissionHandler
import com.foodgpt.recognition.AiEdgeGalleryRecognizer
import com.foodgpt.recognition.DeviceAiCapabilityDetector
import com.foodgpt.recognition.IngredientExtractionPipeline
import com.foodgpt.recognition.IngredientRecognitionCoordinator
import com.foodgpt.recognition.LocalOcrFallbackRecognizer
import com.foodgpt.recognition.RecognitionEngineSelector
import com.foodgpt.navigation.CameraFlowRoutes
import com.foodgpt.result.LlmResultScreen
import com.foodgpt.scan.TemporaryImageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val permissionHandler = CameraPermissionHandler()
    private val modelImportManager by lazy { GemmaModelImportManager(applicationContext) }

    private lateinit var cameraViewModel: CameraViewModel

    private lateinit var healthCritiqueViewModel: HealthCritiqueViewModel

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
        val app = application as FoodGptApplication

        setContent {
            // La spec 012 definit la reference d'ordre UI pour l'accueil.
            HomeSpecPriorityResolver.resolveHomeUiOrderSpec()
            val imageManager = remember { TemporaryImageManager(applicationContext) }
            var uiReady by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
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
                localGateway.ensureModelDownloaded()
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
                MaterialTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                MaterialTheme {
                    LaunchedEffect(Unit) {
                        cameraViewModel.lastValidatedSegmentForHealth.collectLatest { segment ->
                            healthCritiqueViewModel.setValidatedSegmentFromScan(segment)
                        }
                    }
                    val cameraNavController = rememberNavController()
                    LaunchedEffect(cameraViewModel) {
                        cameraViewModel.navigateToLlmResult.collect {
                            cameraNavController.navigate(CameraFlowRoutes.LlmResult)
                        }
                    }
                    NavHost(
                        navController = cameraNavController,
                        startDestination = CameraFlowRoutes.Capture,
                        modifier = Modifier.fillMaxSize()
                    ) {
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
                            val payload = remember {
                                CameraLlmResultPayloadStore.getAndClear()
                                    ?: cameraViewModel.buildLlmResultFallbackPayload()
                            }
                            LlmResultScreen(
                                body = payload.body,
                                isError = payload.isError,
                                errorCategoryWire = payload.errorCategoryWire,
                                onBack = { cameraNavController.popBackStack() }
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
