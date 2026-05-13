package com.miamia.camera.ingredientsegment

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miamia.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * SC-005 / FR-010 (UI) : le signal « balise ingrédients » est exposé sur l’écran capture
 * (`ingredients_framing_tag_chip`). L’absence d’écran intermédiaire après OCR est couverte
 * par les tests JVM sur [com.miamia.analysis.ingredientsegment.AnalysisSubmissionGate] et
 * l’orchestration [com.miamia.camera.CameraViewModel].
 */
@RunWith(AndroidJUnit4::class)
class IngredientFramingTagChipAndroidTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun ingredients_framing_chip_displayed_above_capture_button() {
        composeRule.waitForIdle()

        val chip = composeRule.onNodeWithTag("ingredients_framing_tag_chip", useUnmergedTree = true)
        val capture = composeRule.onNodeWithTag("capture_photo_button")

        chip.assertIsDisplayed()
        capture.assertIsDisplayed()

        val topChip = chip.getUnclippedBoundsInRoot().top
        val topCapture = capture.getUnclippedBoundsInRoot().top
        assertTrue("chip above capture button (FR-010 signal visible avant capture)", topChip < topCapture)
    }
}
