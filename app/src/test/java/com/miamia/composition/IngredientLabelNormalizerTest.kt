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
    fun preprocess_removesSurplusClosingParen() {
        assertEquals("huile de colza", IngredientLabelNormalizer.stripPercentAndBrokenParentheticals("huile de colza)"))
    }

    @Test
    fun normalizeBilan_splitsFarineDeBleEtSeigle() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("Farines de blé et de seigle"),
            compositionAnalysis = "x",
            disclaimer = "d",
            healthImpacts = listOf(
                IngredientHealthImpact("ORANGE", "Farines de blé et de seigle", "gluten"),
            ),
        )
        val n = IngredientLabelNormalizer.normalizeBilanIngredientLabels(bilan)
        assertEquals(listOf("farine de blé", "farine de seigle"), n.ingredientLines)
        assertEquals(2, n.healthImpacts.size)
        assertEquals("ORANGE", n.healthImpacts[0].level)
        assertEquals("ORANGE", n.healthImpacts[1].level)
        assertEquals("gluten", n.healthImpacts[0].note)
        assertEquals("gluten", n.healthImpacts[1].note)
    }

    @Test
    fun validator_acceptsSplitFarineLinesAgainstCombinedOcrSegment() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("farine de blé", "farine de seigle"),
            compositionAnalysis = "Analyse.",
            disclaimer = "d",
            healthImpacts = listOf(
                IngredientHealthImpact("VERT", "farine de blé", "a"),
                IngredientHealthImpact("VERT", "farine de seigle", "b"),
            ),
        )
        val segment = "farines de blé et de seigle, sucre"
        val out = CompositionResultValidator.validateAgainstSource(bilan, segment)
        assertTrue(out is AnalyzeCompositionResult.BilanSuccess)
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
