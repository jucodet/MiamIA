package com.foodgpt.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ModelDownloadResumeAcceptanceTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenInterruptedDownload_whenRelaunch_thenResumeWordingDisplayed() {
        composeTestRule.setContent {
            ModelDownloadOnboardingScreen(
                networkType = NetworkType.WIFI,
                isResumable = true,
                onConfirm = {},
                onDecline = {}
            )
        }

        composeTestRule.onNodeWithText("Reprendre le téléchargement", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Reprendre", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun givenFirstDownload_whenLaunch_thenStandardWordingDisplayed() {
        composeTestRule.setContent {
            ModelDownloadOnboardingScreen(
                networkType = NetworkType.WIFI,
                isResumable = false,
                onConfirm = {},
                onDecline = {}
            )
        }

        composeTestRule.onNodeWithText("Téléchargement requis", substring = true)
            .assertIsDisplayed()
    }
}
