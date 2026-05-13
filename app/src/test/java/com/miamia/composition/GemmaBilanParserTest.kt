package com.miamia.composition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun parse_withProductSection_extractsProductAndConfidence() {
        val raw = """
            ###LISTE
            - lait
            - sucre
            - ferments lactiques
            ###PRODUIT
            Yaourt aux fruits|85
            ###ANALYSE
            Produit laitier fermenté. Sucre ajouté en quantité modérée.
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals("Yaourt aux fruits", bilan!!.identifiedProduct)
        assertEquals(85, bilan.productConfidence)
        assertEquals(listOf("lait", "sucre", "ferments lactiques"), bilan.ingredientLines)
        assertEquals("Produit laitier fermenté. Sucre ajouté en quantité modérée.", bilan.compositionAnalysis)
    }

    @Test
    fun parse_withProductSection_withoutConfidence() {
        val raw = """
            ###LISTE
            - lait
            - sucre
            ###PRODUIT
            Yaourt nature
            ###ANALYSE
            Produit laitier simple.
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals("Yaourt nature", bilan!!.identifiedProduct)
        assertNull(bilan.productConfidence)
    }

    @Test
    fun parse_withoutProductSection_identifiedProductIsNull() {
        val raw = """
            ###LISTE
            - eau
            - sucre
            ###ANALYSE
            Le sucre apporte des calories ; modération recommandée.
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertNull(bilan!!.identifiedProduct)
        assertNull(bilan.productConfidence)
    }

    @Test
    fun parseProductLine_withPipeFormat() {
        val (name, confidence) = GemmaBilanParser.parseProductLine("Biscuit fourré chocolat|60")
        assertEquals("Biscuit fourré chocolat", name)
        assertEquals(60, confidence)
    }

    @Test
    fun parseProductLine_withoutPipe() {
        val (name, confidence) = GemmaBilanParser.parseProductLine("Limonade")
        assertEquals("Limonade", name)
        assertNull(confidence)
    }

    @Test
    fun parseProductLine_null() {
        val (name, confidence) = GemmaBilanParser.parseProductLine(null)
        assertNull(name)
        assertNull(confidence)
    }

    @Test
    fun parseProductLine_clampsTo100() {
        val (name, confidence) = GemmaBilanParser.parseProductLine("Soda|150")
        assertEquals("Soda", name)
        assertEquals(100, confidence)
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

    @Test
    fun parse_withImpactSante_extractsHealthImpacts() {
        val raw = """
            ###LISTE
            - eau
            - sucre
            - sel
            ###ANALYSE
            Produit simple avec peu d'ingrédients.
            ###ADDITIFS_RISQUE
            ###IMPACT_SANTE
            VERT|eau|Essentiel à l'hydratation.
            ORANGE|sucre|Consommation excessive liée au diabète.
            ROUGE|sel|Hypertension en cas d'excès.
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals(3, bilan!!.healthImpacts.size)

        val eau = bilan.healthImpacts[0]
        assertEquals("VERT", eau.level)
        assertEquals("eau", eau.ingredient)
        assertEquals("Essentiel à l'hydratation.", eau.note)

        val sucre = bilan.healthImpacts[1]
        assertEquals("ORANGE", sucre.level)
        assertEquals("sucre", sucre.ingredient)

        val sel = bilan.healthImpacts[2]
        assertEquals("ROUGE", sel.level)
        assertEquals("sel", sel.ingredient)
    }

    @Test
    fun parse_withoutImpactSante_returnsEmptyList() {
        val raw = """
            ###LISTE
            - eau
            ###ANALYSE
            Analyse simple.
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals(emptyList<IngredientHealthImpact>(), bilan!!.healthImpacts)
    }

    @Test
    fun parse_impactSante_skipsInvalidLines() {
        val raw = """
            ###LISTE
            - eau
            - sucre
            ###ANALYSE
            Analyse courte.
            ###IMPACT_SANTE
            VERT|eau|Bonne hydratation.
            ligne sans pipe
            INVALIDE|sucre|Niveau inconnu
            ORANGE||Note sans ingrédient
            ROUGE|sel|Excès dangereux.
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals(2, bilan!!.healthImpacts.size)
        assertEquals("VERT", bilan.healthImpacts[0].level)
        assertEquals("eau", bilan.healthImpacts[0].ingredient)
        assertEquals("ROUGE", bilan.healthImpacts[1].level)
        assertEquals("sel", bilan.healthImpacts[1].ingredient)
    }

    @Test
    fun parse_impactSante_toleratesAccentVariations() {
        val raw = """
            ###LISTE
            - lait
            ###ANALYSE
            Produit laitier.
            ###IMPACT_SANTE
            INCERTAIN|lait|Intolérance possible au lactose.
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals(1, bilan!!.healthImpacts.size)
        assertEquals("INCERTAIN", bilan.healthImpacts[0].level)
        assertEquals("lait", bilan.healthImpacts[0].ingredient)
    }

    @Test
    fun parse_impactSante_toleratesHashVariations() {
        val raw = """
            ###LISTE
            - lait
            ###ANALYSE
            Produit laitier.
            ## IMPACT SANTE
            VERT|lait|Source de calcium.
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals(1, bilan!!.healthImpacts.size)
        assertEquals("lait", bilan.healthImpacts[0].ingredient)
    }

    @Test
    fun parse_stripsTruncatedMarkdownGarbageAfterAnalyse() {
        val raw = """
            ###LISTE
            - eau
            ###ANALYSE
            Texte d'analyse valide.

            ##]
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals("Texte d'analyse valide.", bilan!!.compositionAnalysis)
    }

    @Test
    fun parse_insertsPlaceholderWhenAdditifsFollowsAnalyseWithNoText() {
        val raw = """
            ###LISTE
            - eau
            ###ANALYSE
            ###ADDITIFS_RISQUE
            VERT|E300|ok
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertTrue(
            bilan!!.compositionAnalysis.contains("Synthèse indisponible", ignoreCase = true),
        )
    }

    @Test
    fun parse_acceptsAnalysisSectionMarkerInEnglish() {
        val raw = """
            ###LISTE
            - eau
            ###ANALYSIS
            Short analysis.
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals("Short analysis.", bilan!!.compositionAnalysis)
    }

    @Test
    fun parse_withEnergySection_extractsKcal() {
        val raw = """
            ###LISTE
            - eau
            ###ANALYSE
            Boisson très diluée.
            ###ENERGIE_ESTIMEE
            38
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertEquals(38, bilan!!.estimatedKcalPer100g)
        assertEquals("Boisson très diluée.", bilan.compositionAnalysis)
    }

    @Test
    fun parse_energySectionNa_yieldsNullKcal() {
        val raw = """
            ###LISTE
            - eau
            ###ANALYSE
            Eau seule.
            ###ENERGIE_ESTIMEE
            NA
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertNull(bilan!!.estimatedKcalPer100g)
    }

    @Test
    fun parse_energyOutOfRange_yieldsNullKcal() {
        val raw = """
            ###LISTE
            - huile
            ###ANALYSE
            Lipides concentrés.
            ###ENERGIE_ESTIMEE
            9999
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertNull(bilan!!.estimatedKcalPer100g)
    }

    @Test
    fun parse_energyAfterAdditifs_ignored() {
        val raw = """
            ###LISTE
            - eau
            ###ANALYSE
            Court.
            ###ADDITIFS_RISQUE

            ###ENERGIE_ESTIMEE
            400
        """.trimIndent()

        val bilan = GemmaBilanParser.parse(raw)
        assertNotNull(bilan)
        assertNull(bilan!!.estimatedKcalPer100g)
    }
}
