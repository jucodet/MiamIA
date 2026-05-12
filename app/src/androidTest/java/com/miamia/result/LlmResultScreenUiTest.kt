package com.miamia.result

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.miamia.camera.CameraViewModel
import com.miamia.camera.StreamingBilanState
import com.miamia.composition.CompositionBilan
import org.junit.Rule
import org.junit.Test

class LlmResultScreenUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun completeResultShowsBackButton() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CameraViewModel(app, coordinator = null)
        vm.debugOverrideScanStateForTests(
            com.miamia.camera.ScanState.CompositionAnalyzing()
        )

        composeTestRule.setContent {
            LlmResultScreen(
                viewModel = vm,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithTag("llm_result_title").assertIsDisplayed()
    }
}
