package com.miamia.camera

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miamia.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraCaptureLayoutUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun preview_capture_thenLlmButton_verticalOrder() {
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("Accueil").assertCountEquals(0)
        composeRule.onAllNodesWithText("Caméra").assertCountEquals(0)

        val preview = composeRule.onNodeWithTag("photo_preview_box", useUnmergedTree = true)
        val capture = composeRule.onNodeWithTag("capture_photo_button")
        val llm = composeRule.onNodeWithTag("camera_tab_llm_test_button")

        preview.assertIsDisplayed()
        capture.assertIsDisplayed()
        llm.assertIsDisplayed()

        val topPreview = preview.getUnclippedBoundsInRoot().top
        val topCapture = capture.getUnclippedBoundsInRoot().top
        val topLlm = llm.getUnclippedBoundsInRoot().top
        assertTrue("preview above capture", topPreview < topCapture)
        assertTrue("capture above llm", topCapture < topLlm)
    }
}
