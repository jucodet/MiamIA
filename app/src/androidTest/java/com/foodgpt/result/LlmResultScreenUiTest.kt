package com.foodgpt.result

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import org.junit.Rule
import org.junit.Test

class LlmResultScreenUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun longResultKeepsBackControlReachable() {
        val longBody = buildString {
            repeat(200) { index ->
                append("Ligne de transcription ")
                append(index)
                append('\n')
            }
            append("FIN_LONG_RESULT")
        }

        composeTestRule.setContent {
            LlmResultScreen(
                body = longBody,
                isError = false,
                errorCategoryWire = null,
                onBack = {}
            )
        }

        composeTestRule.onNodeWithTag("llm_result_back").assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("llm_result_body")
            .performScrollToNode(hasText("FIN_LONG_RESULT"))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("llm_result_back").assertIsDisplayed()
    }
}
