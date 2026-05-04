package com.foodgpt.healthcritique

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HealthCritiqueReadOnlySegmentAndroidTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun segmentListField_isReadOnly() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val store = LastHealthAnalysisStore(ctx, "ro_seg_${System.nanoTime()}")
        val fake = FakeHealthCritiqueLlmRunner(
            HealthCritiqueLlmGenerateResult.Success(
                """
                ###ENFANTS
                x
                ###FEMMES_ENCEINTES
                x
                ###ADULTES
                x
                ###PERSONNES_AGEES
                x
                """.trimIndent(),
            ),
        )
        val engine = HealthCritiqueEngine(llmRunner = fake)
        val vm = HealthCritiqueViewModel(engine, store, HealthCritiquePromptBuilder())
        vm.setValidatedSegmentFromScan("eau, sucre, sel, farine")

        composeRule.setContent {
            MaterialTheme {
                HealthCritiqueScreen(vm)
            }
        }

        val node = composeRule.onNodeWithTag("health_segment_list", useUnmergedTree = true)
        node.assertIsDisplayed()
        node.assert(SemanticsMatcher.expectValue(SemanticsProperties.Editable, false))
    }
}
