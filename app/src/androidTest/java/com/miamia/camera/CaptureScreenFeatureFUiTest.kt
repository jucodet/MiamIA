package com.miamia.camera

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miamia.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CaptureScreenFeatureFUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun previewActive_noLlmButton_noLegacyStatus_featureGChrome() {
        composeRule.waitForIdle()
        val vm = ViewModelProvider(composeRule.activity)[CameraViewModel::class.java]
        composeRule.runOnIdle {
            vm.debugOverrideScanStateForTests(ScanState.PreviewActive)
        }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("camera_tab_llm_test_button", useUnmergedTree = true)
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Aperçu caméra actif").assertCountEquals(0)
        composeRule.onAllNodesWithText("Test LLM").assertCountEquals(0)

        composeRule.onAllNodesWithTag("ingredients_framing_tag_chip", useUnmergedTree = true)
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Caméra prête — vous pouvez scanner").assertCountEquals(0)
        composeRule.onNodeWithTag("capture_scan_status_text").assertDoesNotExist()
    }
}
