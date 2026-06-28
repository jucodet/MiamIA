package com.miamia.camera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodesWithText
import com.miamia.composition.CompositionBilan
import com.miamia.gemma4local.model.BackendExecution
import com.miamia.ui.theme.MiamIATheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Acceptance UI — pastille backend d'inférence (`inference_backend_badge`) à côté de la durée.
 *
 * Couvre :
 * - US1 : présence + libellé correct par backend (NPU/GPU/CPU) à côté de `inference_time_label`.
 * - US2 : distinctité visuelle — libellés distincts par backend (identification sans lecture ambigüe).
 * - US3 : cas non trompeur — aucune pastille quand `inferenceTimeMs == 0` (échec pré-exécution) ;
 *         pastille neutre "—" pour `INDETERMINATE`.
 */
@RunWith(RobolectricTestRunner::class)
class InferenceBackendBadgeUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun sampleBilan(): CompositionBilan = CompositionBilan(
        ingredientLines = listOf("farine de blé", "sucre"),
        compositionAnalysis = "Bilan de composition.",
        disclaimer = "Disclaimer."
    )

    private fun renderCard(inferenceTimeMs: Long, backend: BackendExecution) {
        composeRule.setContent {
            MiamIATheme {
                BilanResultCard(
                    bilan = sampleBilan(),
                    rawTranscript = "farine de blé, sucre",
                    additiveKpi = null,
                    showRaw = false,
                    onToggleRaw = {},
                    inferenceTimeMs = inferenceTimeMs,
                    backend = backend
                )
            }
        }
    }

    // ---- US1 : présence + libellé par backend ----

    @Test
    fun npuBackend_showsBadgeWithNpuLabelNextToInferenceTime() {
        renderCard(inferenceTimeMs = 1500L, backend = BackendExecution.NPU)
        composeRule.onNodeWithTag("inference_time_label", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("inference_backend_badge", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("NPU", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun gpuBackend_showsBadgeWithGpuLabel() {
        renderCard(inferenceTimeMs = 1500L, backend = BackendExecution.GPU)
        composeRule.onNodeWithTag("inference_backend_badge", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("GPU", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun cpuBackend_showsBadgeWithCpuLabel() {
        renderCard(inferenceTimeMs = 1500L, backend = BackendExecution.CPU)
        composeRule.onNodeWithTag("inference_backend_badge", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("CPU", useUnmergedTree = true).assertIsDisplayed()
    }

    // ---- US2 : distinctité — un seul libellé backend présent à la fois ----

    @Test
    fun eachBackendShowsItsOwnDistinctLabel() {
        BackendExecution.values().filter { it != BackendExecution.INDETERMINATE }.forEach { backend ->
            renderCard(inferenceTimeMs = 1500L, backend = backend)
            composeRule.onNodesWithText(backend.label, useUnmergedTree = true).assertCountEquals(1)
        }
    }

    // ---- US3 : cas non trompeur ----

    @Test
    fun zeroInferenceTime_hidesBackendBadge() {
        renderCard(inferenceTimeMs = 0L, backend = BackendExecution.NPU)
        composeRule.onAllNodesWithTag("inference_backend_badge", useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun indeterminateBackend_showsNeutralBadge() {
        renderCard(inferenceTimeMs = 1500L, backend = BackendExecution.INDETERMINATE)
        composeRule.onNodeWithTag("inference_backend_badge", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("—", useUnmergedTree = true).assertIsDisplayed()
    }
}
