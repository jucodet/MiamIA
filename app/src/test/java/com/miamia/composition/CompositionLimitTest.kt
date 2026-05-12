package com.miamia.composition

import org.junit.Assert.assertTrue
import org.junit.Test

class CompositionLimitTest {

    @Test
    fun rejectEmptyStructure_whenMissingSections() {
        val bilan = CompositionBilan(
            ingredientLines = emptyList(),
            compositionAnalysis = "",
            disclaimer = "d"
        )
        val reject = CompositionResultValidator.rejectEmptyStructure(bilan)
        assertTrue(reject != null)
    }

    @Test
    fun inventedIngredient_becomesCompositionLimit() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("eau", "ingredient_invente_xyz"),
            compositionAnalysis = "Texte.",
            disclaimer = "d"
        )
        val raw = "eau, sucre"
        val out = CompositionResultValidator.validateAgainstSource(bilan, raw)
        assertTrue(out is AnalyzeCompositionResult.CompositionLimit)
    }

    @Test
    fun allIngredientsInSource_staysSuccess() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("eau", "sucre"),
            compositionAnalysis = "Analyse.",
            disclaimer = "d"
        )
        val raw = "liste : eau, sucre, farine"
        val out = CompositionResultValidator.validateAgainstSource(bilan, raw)
        assertTrue(out is AnalyzeCompositionResult.BilanSuccess)
    }
}
