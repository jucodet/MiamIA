package com.miamia.composition

import org.junit.Assert.assertTrue
import org.junit.Test

class CompositionAnchoringAllOrNothingTest {

    @Test
    fun oneOfTwoCheckableLinesMissing_isRejected_notHalfSuccess() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("farine complète", "ingredient_invente_xyz"),
            compositionAnalysis = "Analyse.",
            disclaimer = "d",
        )
        val segment = "farine complète, eau"
        val out = CompositionResultValidator.validateAgainstSource(bilan, segment)
        assertTrue(
            "Ancrage strict : une ligne hors segment ⇒ CompositionLimit (pas de succès partiel).",
            out is AnalyzeCompositionResult.CompositionLimit,
        )
    }
}
