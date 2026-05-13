package com.miamia.composition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientLabelNormalizerTest {

    @Test
    fun normalizeForMatching_ignoresPercentParentheses() {
        val a = IngredientLabelNormalizer.normalizeForMatching("Farine de blé (50,2 %)")
        val b = IngredientLabelNormalizer.normalizeForMatching("farine de blé")
        assertEquals(a, b)
    }

    @Test
    fun stripPercent_removesIncompleteTrailingParen() {
        val s = IngredientLabelNormalizer.stripPercentAndBrokenParentheticals(
            "Graisses végétales (palme",
        )
        assertEquals("Graisses végétales", s)
    }

    @Test
    fun normalizeBilan_alignsListAndImpactKeys() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("Farine de seigle (49 %)"),
            compositionAnalysis = "x",
            disclaimer = "d",
            healthImpacts = listOf(
                IngredientHealthImpact("VERT", "Farine de seigle", "ok"),
            ),
        )
        val n = IngredientLabelNormalizer.normalizeBilanIngredientLabels(bilan)
        assertEquals("Farine de seigle", n.ingredientLines.single())
        assertEquals("Farine de seigle", n.healthImpacts.single().ingredient)
        val completed = CompositionImpactCompleter.completeMissingHealthImpacts(n)
        assertEquals(1, completed.healthImpacts.size)
        assertTrue(completed.healthImpacts.none { it.level == "INCERTAIN" })
    }
}
