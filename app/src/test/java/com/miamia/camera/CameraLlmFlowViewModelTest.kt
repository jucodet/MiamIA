package com.miamia.camera

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.miamia.composition.AnalyzeCompositionResult
import com.miamia.composition.CompositionAnalysisEngine
import com.miamia.composition.CompositionBilan
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CameraLlmFlowViewModelTest {

    private val bilan = CompositionBilan(
        ingredientLines = listOf("eau", "sucre"),
        compositionAnalysis = "Analyse courte.",
        disclaimer = "Non médical",
    )

    private fun engineReturningSuccess() = object : CompositionAnalysisEngine {
        override suspend fun analyze(
            rawText: String,
            maxInferenceMs: Long,
            onStreamPartial: ((String) -> Unit)?
        ) = AnalyzeCompositionResult.BilanSuccess(bilan)
    }

    @Test
    fun compositionDone_captureActive_emitsNavAndStreamingComplete() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CameraViewModel(app, coordinator = null, compositionEngine = engineReturningSuccess())
        vm.setCaptureRouteActive(true)
        vm.debugSeedTranscript("Ingredients: eau, sucre", listOf("eau", "sucre"))

        val received = CountDownLatch(1)
        lateinit var nav: CameraLlmResultNavigation
        thread(start = true) {
            runBlocking {
                nav = vm.navigateToLlmResult.first()
                received.countDown()
            }
        }
        Thread.sleep(150)

        vm.retryCompositionAnalysis()
        val mainLooper = Shadows.shadowOf(Looper.getMainLooper())
        repeat(25) {
            mainLooper.idle()
            if (received.count > 0L) Thread.sleep(5)
        }
        assertTrue(received.await(5, TimeUnit.SECONDS))
        assertFalse(nav.isError)

        var sawComplete = false
        for (i in 0 until 80) {
            if (vm.streamingBilan.value is StreamingBilanState.Complete) {
                sawComplete = true
                break
            }
            Thread.sleep(25)
            mainLooper.idle()
        }
        assertTrue(sawComplete)
        val complete = vm.streamingBilan.value as StreamingBilanState.Complete
        assertTrue(complete.bilan.ingredientLines.contains("eau"))
    }

    @Test
    fun compositionDone_captureInactive_keepsBilanReadyAndNoNav() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CameraViewModel(app, coordinator = null, compositionEngine = engineReturningSuccess())
        vm.setCaptureRouteActive(false)
        vm.debugSeedTranscript("Ingredients: eau, sucre", listOf("eau", "sucre"))

        vm.retryCompositionAnalysis()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        var sawBilan = false
        for (i in 0 until 80) {
            if (vm.scanState.value is ScanState.BilanReady) {
                sawBilan = true
                break
            }
            Thread.sleep(25)
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
        assertTrue(sawBilan)
    }

    @Test
    fun fallbackPayload_usesTranscriptWhenAvailable() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CameraViewModel(app, coordinator = null, compositionEngine = engineReturningSuccess())
        vm.debugSeedTranscript("Ingredients: eau, sucre", listOf("eau", "sucre"))

        val payload = vm.buildLlmResultFallbackPayload()

        assertTrue(payload.isError)
        assertEquals("non-analysable-response", payload.errorCategoryWire)
        assertTrue(payload.body.contains("Ingredients: eau, sucre"))
    }
}
