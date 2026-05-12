package com.miamia.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class ModelDownloadOnboardingAcceptanceTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenModelAbsent_whenFirstLaunch_thenConfirmationScreenShownWithNetworkInfo() {
        composeTestRule.setContent {
            ModelDownloadOnboardingScreen(
                networkType = NetworkType.WIFI,
                isResumable = false,
                onConfirm = {},
                onDecline = {}
            )
        }

        composeTestRule.onNodeWithTag("onboarding_confirm_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wi-Fi", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_confirm_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_decline_button").assertIsDisplayed()
    }

    @Test
    fun givenConfirmationShown_whenDecline_thenDeclinedStateShown() {
        var declined = false
        composeTestRule.setContent {
            ModelDownloadOnboardingScreen(
                networkType = NetworkType.WIFI,
                isResumable = false,
                onConfirm = {},
                onDecline = { declined = true }
            )
        }

        composeTestRule.onNodeWithTag("onboarding_decline_button").performClick()
        assert(declined)
    }

    @Test
    fun givenOffline_whenFirstLaunch_thenOfflineScreenShownWithRetry() {
        composeTestRule.setContent {
            NetworkOfflineScreen(onRetry = {})
        }

        composeTestRule.onNodeWithTag("offline_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connexion requise", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("offline_retry_button").assertIsDisplayed()
    }
}
