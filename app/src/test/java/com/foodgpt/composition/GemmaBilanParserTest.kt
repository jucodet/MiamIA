package com.foodgpt.composition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GemmaBilanParserTest {

    @Test
    fun parse_validSections() {
        val raw = """
            ###LISTE
            - eau
            - sucre
            ###ANALYSE
            Le sucre apporte des calories ; modération recommandée.
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals(listOf("eau", "sucre"), bilan!!.ingredientLines)
        assertEquals("Le sucre apporte des calories ; modération recommandée.", bilan.compositionAnalysis)
    }

    @Test
    fun parse_missingMarker_returnsNull() {
        assertNull(GemmaBilanParser.parse("pas de structure"))
    }

    @Test
    fun parse_withAdditivesSection_analysisExcludesAdditivesBlock() {
        val raw = """
            ###LISTE
            - eau
            ###ANALYSE
            Phrase courte sur le produit.
            ###ADDITIFS_RISQUE
            ROUGE|E120|Colorant de synthese
            VERT|E300|Antioxydant courant
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals("Phrase courte sur le produit.", bilan!!.compositionAnalysis)
        assertEquals(listOf("eau"), bilan.ingredientLines)
    }
}
