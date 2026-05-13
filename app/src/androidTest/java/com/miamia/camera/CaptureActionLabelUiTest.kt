package com.miamia.camera

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miamia.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CR-FR-011 (incrément 020) — Le bouton principal de capture MUST afficher exactement le
 * libellé « Y a quoi là-dedans ? » dans tous les états où il est rendu. Ce test couvre
 * spécifiquement les états de prévisualisation live (`PreviewActive`). L'état
 * `CameraUnavailable` est couvert dans `CameraUnavailableLlmButtonUiTest`.
 */
@RunWith(AndroidJUnit4::class)
class CaptureActionLabelUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun capture_photo_button_displays_y_a_quoi_la_dedans_in_live_preview() {
        composeRule.waitForIdle()

        val activity = composeRule.activity
        val vm = ViewModelProvider(activity)[CameraViewModel::class.java]
        composeRule.runOnIdle {
            vm.debugOverrideScanStateForTests(ScanState.PreviewActive)
        }

        composeRule.onNodeWithTag("capture_photo_button")
            .assertIsDisplayed()
            .assert(hasText("Y a quoi là-dedans ?"))
    }
}
