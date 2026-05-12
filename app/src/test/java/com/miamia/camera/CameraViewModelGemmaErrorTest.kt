package com.miamia.camera

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.miamia.composition.AnalyzeCompositionResult
import com.miamia.composition.CompositionAnalysisEngine
import com.miamia.composition.GemmaErrorCode
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CameraViewModelGemmaErrorTest {

    @Test
    fun engineReturnsNotFound_emitsGemmaUnavailable_notBilanReady() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val engine = object : CompositionAnalysisEngine {
            override suspend fun analyze(
                rawText: String,
                maxInferenceMs: Long,
                onStreamPartial: ((String) -> Unit)?
            ) = AnalyzeCompositionResult.GemmaError(GemmaErrorCode.GEMMA_NOT_FOUND, "Gemma introuvable")
        }
        val vm = CameraViewModel(app, coordinator = null, compositionEngine = engine)
        vm.debugSeedTranscript("eau, sucre")
        vm.retryCompositionAnalysis()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        val s = vm.scanState.value
        assertTrue(s is ScanState.GemmaUnavailable)
        assertTrue(s !is ScanState.BilanReady)
    }
}
