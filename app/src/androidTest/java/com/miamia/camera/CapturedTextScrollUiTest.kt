package com.miamia.camera

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.miamia.ui.theme.MiamIATheme
import org.junit.Rule
import org.junit.Test
import java.io.File

class CapturedTextScrollUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun longSuccess_showsScrollRegionAndPinnedNewScanButton() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CameraViewModel(app, coordinator = null)
        val longText = List(80) { "Ligne $it: " + "x".repeat(40) }.joinToString("\n")
        vm.debugOverrideScanStateForTests(
            ScanState.Success(transcriptText = longText, items = listOf("item unique"))
        )

        composeTestRule.setContent {
            MiamIATheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraScreen(
                        viewModel = vm,
                        onCreateTempImage = {
                            File.createTempFile("cap", ".jpg", app.cacheDir)
                        },
                        onRequestCameraPermission = {},
                        onOpenAppSettings = {},
                        onChooseGemmaModel = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("captured_review_scroll").assertIsDisplayed()
        composeTestRule.onNodeWithTag("new_scan_button").assertIsDisplayed()
    }

    @Test
    fun shortSuccess_showsTranscriptAndNewScanButton() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CameraViewModel(app, coordinator = null)
        val short = "Court: eau, sucre"
        vm.debugOverrideScanStateForTests(
            ScanState.Success(transcriptText = short, items = emptyList())
        )

        composeTestRule.setContent {
            MiamIATheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraScreen(
                        viewModel = vm,
                        onCreateTempImage = {
                            File.createTempFile("cap", ".jpg", app.cacheDir)
                        },
                        onRequestCameraPermission = {},
                        onOpenAppSettings = {},
                        onChooseGemmaModel = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Court:", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("new_scan_button").assertIsDisplayed()
    }
}
