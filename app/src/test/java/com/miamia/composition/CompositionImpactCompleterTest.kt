package com.miamia.composition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositionImpactCompleterTest {

    @Test
    fun addsMissingImpactForLexiconRenamedIngredient() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("eau", "omidon"),
            compositionAnalysis = "Analyse.",
            disclaimer = "d",
            healthImpacts = listOf(
                IngredientHealthImpact("VERT", "eau", "ok"),
            ),
        )
        val prepared = CompositionImpactCompleter.completeMissingHealthImpacts(
            IngredientOcrLexicon.applyToBilan(bilan),
        )
        assertEquals(2, prepared.healthImpacts.size)
        assertTrue(prepared.healthImpacts.any { it.ingredient.equals("amidon", ignoreCase = true) })
        val out = CompositionResultValidator.validateAgainstSource(prepared, "eau, omidon, sel")
        assertTrue(out is AnalyzeCompositionResult.BilanSuccess)
    }
}
