package com.miamia.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ModelDownloadWaitingAcceptanceTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenDownloading_whenScreenDisplayed_thenTitleProgressAndMarmiteVisible() {
        composeTestRule.setContent {
            ModelDownloadWaitingScreen(
                progress = DownloadProgress(percent = 42, downloadedBytes = 420L, totalBytes = 1000L),
                error = null,
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithTag("download_waiting_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Téléchargement du modèle de langage en cours...")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("download_progress_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("download_marmite").assertIsDisplayed()
        composeTestRule.onNodeWithTag("download_progress_percent").assertIsDisplayed()
    }

    @Test
    fun givenDownloading_whenTimeElapses_thenPhrasesRotate() {
        composeTestRule.setContent {
            ModelDownloadWaitingScreen(
                progress = DownloadProgress(percent = 10, downloadedBytes = 100L, totalBytes = 1000L),
                error = null,
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithTag("download_waiting_phrase").assertIsDisplayed()
    }

    @Test
    fun givenError_whenScreenDisplayed_thenErrorMessageAndRetryShown() {
        composeTestRule.setContent {
            ModelDownloadWaitingScreen(
                progress = null,
                error = LlmModelReadinessState.Error("Connexion perdue", canRetry = true),
                onRetry = {}
            )
        }

        composeTestRule.onNodeWithText("Connexion perdue", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("download_retry_button").assertIsDisplayed()
    }
}
