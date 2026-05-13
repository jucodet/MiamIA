package com.miamia.camera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miamia.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraUnavailableLlmButtonUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun cameraUnavailable_showsMessageAndCaptureButton() {
        composeRule.waitForIdle()

        val activity = composeRule.activity
        val vm = ViewModelProvider(activity)[CameraViewModel::class.java]
        composeRule.runOnIdle {
            vm.debugOverrideScanStateForTests(ScanState.CameraUnavailable("simulation indisponibilité"))
        }

        composeRule.onNodeWithTag("camera_unavailable_message").assertIsDisplayed()
        composeRule.onNodeWithTag("photo_preview_placeholder").assertIsDisplayed()
        composeRule.onNodeWithTag("capture_photo_button").assertIsDisplayed()
    }

    /**
     * CR-FR-009 / CR-FR-010 (incrément 020) : dans l'état `CameraUnavailable`, aucun bouton
     * d'action ne MUST recouvrir le placeholder de prévisualisation. Séparation visuelle
     * non ambiguë attendue (≥ 16 dp).
     */
    @Test
    fun cameraUnavailable_capturePlaceholder_andCaptureBar_doNotOverlap() {
        composeRule.waitForIdle()

        val activity = composeRule.activity
        val vm = ViewModelProvider(activity)[CameraViewModel::class.java]
        composeRule.runOnIdle {
            vm.debugOverrideScanStateForTests(ScanState.CameraUnavailable("simulation indisponibilité"))
        }

        val placeholder = composeRule.onNodeWithTag("photo_preview_placeholder", useUnmergedTree = true)
        val capture = composeRule.onNodeWithTag("capture_photo_button")

        placeholder.assertIsDisplayed()
        capture.assertIsDisplayed()

        val placeholderBounds = placeholder.getUnclippedBoundsInRoot()
        val captureBounds = capture.getUnclippedBoundsInRoot()

        val minGap = 16.dp

        assertTrue(
            "capture_photo_button must be strictly below photo_preview_placeholder. " +
                "placeholderBottom=${placeholderBounds.bottom}, captureTop=${captureBounds.top}",
            captureBounds.top >= placeholderBounds.bottom
        )
        val captureGap = captureBounds.top - placeholderBounds.bottom
        assertTrue(
            "Gap between placeholder bottom and capture button top must be >= $minGap " +
                "(got $captureGap).",
            captureGap >= minGap
        )
    }

    /**
     * CR-FR-011 (incrément 020) : libellé exact de l'action principale dans l'état
     * `CameraUnavailable`.
     */
    @Test
    fun cameraUnavailable_capturePhotoButton_displaysYAQuoiLaDedansLabel() {
        composeRule.waitForIdle()

        val activity = composeRule.activity
        val vm = ViewModelProvider(activity)[CameraViewModel::class.java]
        composeRule.runOnIdle {
            vm.debugOverrideScanStateForTests(ScanState.CameraUnavailable("simulation indisponibilité"))
        }

        composeRule.onNodeWithTag("capture_photo_button")
            .assertIsDisplayed()
            .assert(hasText("Y a quoi là-dedans ?"))
    }
}
