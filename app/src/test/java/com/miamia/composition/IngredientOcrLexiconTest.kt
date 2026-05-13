package com.miamia.composition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientOcrLexiconTest {

    @Test
    fun replacesPolmisteWithPalmiste() {
        assertEquals("palmiste", IngredientOcrLexicon.apply("polmiste"))
        assertEquals("huile de palmiste", IngredientOcrLexicon.apply("huile de polmiste"))
    }

    @Test
    fun replacesOmidonWithAmidon() {
        assertEquals("amidon", IngredientOcrLexicon.apply("omidon"))
        assertEquals("Eau, amidon, sucre", IngredientOcrLexicon.apply("Eau, omidon, sucre"))
    }

    @Test
    fun expandForAnchoring_matchesBilanAfterLexicon() {
        val segment = "sucre, omidon modifié"
        val expanded = IngredientOcrLexicon.expandForAnchoring(segment)
        assertTrue(expanded.contains("amidon", ignoreCase = true))
        val bilan = CompositionBilan(
            ingredientLines = listOf("amidon modifié"),
            compositionAnalysis = "Analyse.",
            disclaimer = "d",
            healthImpacts = listOf(
                IngredientHealthImpact("VERT", "amidon modifié", "ok"),
            ),
        )
        val out = CompositionResultValidator.validateAgainstSource(
            IngredientOcrLexicon.applyToBilan(bilan),
            segment,
        )
        assertTrue(out is AnalyzeCompositionResult.BilanSuccess)
    }
}
