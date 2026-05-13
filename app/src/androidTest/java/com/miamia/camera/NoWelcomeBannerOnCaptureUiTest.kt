package com.miamia.camera

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miamia.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Feature D — UGE-D-FR-001 / UGE-D-FR-002.
 *
 * L'écran capture (`CameraScreen`) ne MUST jamais rendre la bannière de message d'accueil,
 * quel que soit le `ScanState` actif, **même** lorsque la `WelcomeMessagePolicy` a
 * sélectionné un message à afficher (`WelcomeMessageUiState.Displayed`). Cette suite force
 * explicitement les deux conditions (state visible + policy displayed) puis assert qu'aucun
 * nœud porteur du test tag `welcome_message_banner` n'existe dans l'arbre Compose.
 *
 * Couverture multi-états (alignée avec `quickstart.md` Feature D, scénarios D1) :
 *  - `ScanState.PreviewActive` (parcours nominal)
 *  - `ScanState.CameraUnavailable` (mode dégradé)
 *  - `ScanState.Error` (mode erreur)
 */
@RunWith(AndroidJUnit4::class)
class NoWelcomeBannerOnCaptureUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun no_welcome_banner_in_live_preview() {
        composeRule.waitForIdle()
        val vm = ViewModelProvider(composeRule.activity)[CameraViewModel::class.java]
        composeRule.runOnIdle {
            vm.debugForceWelcomeDisplayedForTests("Bienvenue (test live preview)")
            vm.debugOverrideScanStateForTests(ScanState.PreviewActive)
        }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)
    }

    @Test
    fun no_welcome_banner_in_camera_unavailable() {
        composeRule.waitForIdle()
        val vm = ViewModelProvider(composeRule.activity)[CameraViewModel::class.java]
        composeRule.runOnIdle {
            vm.debugForceWelcomeDisplayedForTests("Bienvenue (test unavailable)")
            vm.debugOverrideScanStateForTests(ScanState.CameraUnavailable("test-injected"))
        }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)
    }

    @Test
    fun no_welcome_banner_in_error_state() {
        composeRule.waitForIdle()
        val vm = ViewModelProvider(composeRule.activity)[CameraViewModel::class.java]
        composeRule.runOnIdle {
            vm.debugForceWelcomeDisplayedForTests("Bienvenue (test error)")
            vm.debugOverrideScanStateForTests(ScanState.Error("test-injected"))
        }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("welcome_message_banner").assertCountEquals(0)
    }
}
