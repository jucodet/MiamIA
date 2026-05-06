package com.foodgpt.camera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.foodgpt.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraUnavailableLlmButtonUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun cameraUnavailable_showsMessageAndLlmTestButton() {
        composeRule.waitForIdle()

        val activity = composeRule.activity
        val vm = ViewModelProvider(activity)[CameraViewModel::class.java]
        composeRule.runOnIdle {
            vm.debugOverrideScanStateForTests(ScanState.CameraUnavailable("simulation indisponibilité"))
        }

        composeRule.onNodeWithTag("camera_unavailable_message").assertIsDisplayed()
        composeRule.onNodeWithTag("photo_preview_placeholder").assertIsDisplayed()
        composeRule.onNodeWithTag("camera_tab_llm_test_button").assertIsDisplayed()
    }
}
