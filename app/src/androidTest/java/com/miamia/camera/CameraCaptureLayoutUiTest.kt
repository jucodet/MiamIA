package com.miamia.camera

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
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
    fun preview_capture_verticalOrder() {
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("Accueil").assertCountEquals(0)
        composeRule.onAllNodesWithText("Caméra").assertCountEquals(0)

        val preview = composeRule.onNodeWithTag("photo_preview_box", useUnmergedTree = true)
        val capture = composeRule.onNodeWithTag("capture_photo_button")

        preview.assertIsDisplayed()
        capture.assertIsDisplayed()

        val topPreview = preview.getUnclippedBoundsInRoot().top
        val topCapture = capture.getUnclippedBoundsInRoot().top
        assertTrue("preview above capture", topPreview < topCapture)
    }

    /**
     * CR-FR-009 / CR-FR-010 (incrément 020) : aucun bouton d'action persistant ne MUST recouvrir,
     * même partiellement, la zone vidéo (`photo_preview_box`). On exige une séparation visuelle
     * non ambiguë (≥ 16 dp) entre la bottom du preview et le top du bouton principal.
     */
    @Test
    fun preview_andCaptureBar_doNotOverlap_inLivePreview() {
        composeRule.waitForIdle()

        val preview = composeRule.onNodeWithTag("photo_preview_box", useUnmergedTree = true)
        val capture = composeRule.onNodeWithTag("capture_photo_button")

        preview.assertIsDisplayed()
        capture.assertIsDisplayed()

        val previewBounds = preview.getUnclippedBoundsInRoot()
        val captureBounds = capture.getUnclippedBoundsInRoot()

        val minGap = 16.dp

        val captureGap = captureBounds.top - previewBounds.bottom

        assertTrue(
            "capture_photo_button must be strictly below photo_preview_box (no overlap). " +
                "previewBottom=${previewBounds.bottom}, captureTop=${captureBounds.top}",
            captureBounds.top >= previewBounds.bottom
        )
        assertTrue(
            "Gap between preview bottom and capture button top must be >= $minGap " +
                "(got $captureGap) to ensure a clear visual separation.",
            captureGap >= minGap
        )
    }
}
