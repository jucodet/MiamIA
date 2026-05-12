package com.miamia.camera

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.miamia.home.HomeLlmMockOutcome
import com.miamia.home.HomeLlmMockRunner
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CameraLlmTestButtonTest {

    @Test
    fun cannotStartTabLlmTestWhileCompositionAnalyzing() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val runner = object : HomeLlmMockRunner {
            override suspend fun run(): HomeLlmMockOutcome =
                HomeLlmMockOutcome.Success("ok")
        }
        val vm = CameraViewModel(app, coordinator = null, compositionEngine = null, homeLlmRunner = runner)
        vm.setCaptureRouteActive(true)
        vm.debugOverrideScanStateForTests(ScanState.CompositionAnalyzing())
        assertFalse(vm.canRunCameraTabLlmTest())
    }

    @Test
    fun canStartTabLlmTestWhenPreviewReadyAndRunnerPresent() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val runner = object : HomeLlmMockRunner {
            override suspend fun run(): HomeLlmMockOutcome =
                HomeLlmMockOutcome.Success("ok")
        }
        val vm = CameraViewModel(app, coordinator = null, compositionEngine = null, homeLlmRunner = runner)
        vm.setCaptureRouteActive(true)
        vm.debugOverrideScanStateForTests(ScanState.PreviewActive)
        assertTrue(vm.canRunCameraTabLlmTest())
    }
}
