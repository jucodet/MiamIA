package com.miamia.additives

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.miamia.additives.ui.AdditiveKpiPanel
import com.miamia.composition.CompositionBilan
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdditiveKpiPanelComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun panel_showsKpiTags_andOrderedLines() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("eau"),
            compositionAnalysis = "a",
            disclaimer = "d",
        )
        val raw = """
            ###LISTE
            - eau
            ###ANALYSE
            Texte d'analyse.
            ###ADDITIFS_RISQUE
            VERT|E300|Antioxydant courant
            ORANGE|E250|Conservateur
            ROUGE|E102|Colorant suspect
            INCERTAIN|E999|Données partielles
        """.trimIndent()
        val result = BuildAdditiveKpiDisplay(bilan, raw)

        composeRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                AdditiveKpiPanel(result = result)
            }
        }

        composeRule.onNodeWithTag("additive_kpi_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("additive_kpi_high").assertIsDisplayed()
        composeRule.onNodeWithTag("additive_kpi_medium").assertIsDisplayed()
        composeRule.onNodeWithTag("additive_kpi_low").assertIsDisplayed()
        composeRule.onNodeWithTag("additive_kpi_unknown").assertIsDisplayed()
        composeRule.onNodeWithTag("additive_kpi_line_0").assertIsDisplayed()
        composeRule.onNodeWithTag("additive_kpi_line_1").assertIsDisplayed()
    }
}
