package com.miamia.additives

import com.miamia.composition.CompositionBilan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdditiveKpiGroundingTest {

    private val bilan = CompositionBilan(
        ingredientLines = listOf("e300"),
        compositionAnalysis = "Analyse.",
        disclaimer = "d",
    )

    @Test
    fun filtersAdditiveRowNotLiteralInValidatedSegment() {
        val modelRaw = """
            ###LISTE
            - e300
            ###ANALYSE
            x
            ###ADDITIFS_RISQUE
            VERT|E300|ok
            ROUGE|E999|hors segment
        """.trimIndent()
        val segment = "vitamine c (e300)"
        val display = BuildAdditiveKpiDisplay(
            bilan = bilan,
            rawLlmTextForParsing = modelRaw,
            validatedIngredientSegment = segment,
        )
        assertEquals(1, display.itemsOrdered.size)
        assertTrue(display.itemsOrdered.single().displayName.contains("E300", ignoreCase = true))
        assertTrue(display.parseErrors.any { it.contains("masqués") })
    }
}
