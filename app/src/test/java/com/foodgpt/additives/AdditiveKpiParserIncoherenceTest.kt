package com.foodgpt.additives

import org.junit.Assert.assertEquals
import org.junit.Test

class AdditiveKpiParserIncoherenceTest {

    @Test
    fun lowLevel_withStrongRiskToken_incoherent() {
        val raw = """
            ###LISTE
            - a
            ###ANALYSE
            x
            ###ADDITIFS_RISQUE
            VERT|Colorant X|Colorant cancérogène probable selon études
        """.trimIndent()
        val outcome = AdditiveKpiParser.parse(raw)
        assertEquals(1, outcome.items.size)
        assertEquals(AdditiveLineConfidence.INCOHERENT, outcome.items.single().confidence)
    }
}
