package com.miamia.composition

import org.junit.Assert.assertTrue
import org.junit.Test

class CompositionResultValidatorGroundingTest {

    @Test
    fun healthImpactIngredient_mustAppearInSegment() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("eau", "sucre"),
            compositionAnalysis = "Analyse.",
            disclaimer = "d",
            healthImpacts = listOf(
                IngredientHealthImpact("VERT", "vanille", "note"),
            ),
        )
        val segment = "eau, sucre"
        val out = CompositionResultValidator.validateAgainstSource(bilan, segment)
        assertTrue(out is AnalyzeCompositionResult.CompositionLimit)
    }

    @Test
    fun identifiedProduct_whenNotAnchored_isStripped_successKeepsIngredients() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("eau"),
            identifiedProduct = "Yaourt miracle inconnu",
            productConfidence = 90,
            compositionAnalysis = "Analyse.",
            disclaimer = "d",
        )
        val segment = "eau, sel"
        val out = CompositionResultValidator.validateAgainstSource(bilan, segment)
        assertTrue(out is AnalyzeCompositionResult.BilanSuccess)
        val b = (out as AnalyzeCompositionResult.BilanSuccess).bilan
        assertTrue(b.identifiedProduct == null && b.productConfidence == null)
    }

    @Test
    fun mechanicalCaseFolding_stillAnchors() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("SUCRE"),
            compositionAnalysis = "Analyse.",
            disclaimer = "d",
        )
        val segment = "sucre en poudre"
        val out = CompositionResultValidator.validateAgainstSource(bilan, segment)
        assertTrue(out is AnalyzeCompositionResult.BilanSuccess)
    }
}
