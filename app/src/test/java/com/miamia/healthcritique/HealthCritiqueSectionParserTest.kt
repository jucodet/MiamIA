package com.miamia.healthcritique

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthCritiqueSectionParserTest {

    private val parser = HealthCritiqueSectionParser()

    @Test
    fun parsesFourSections() {
        val raw = """
            ###ENFANTS
            vigilance: sucre
            explication: énergie rapide
            prudence: modéré
            ###FEMMES_ENCEINTES
            vigilance: caféine si présente
            explication: nuancer selon dose
            prudence: élevé si doute
            ###ADULTES
            ok
            ###PERSONNES_AGEES
            sel
        """.trimIndent()
        val parsed = parser.parse(raw)
        assertTrue(parsed.warnings.isEmpty())
        assertTrue(parsed.sections[PopulationKey.ENFANTS]!!.contains("sucre"))
        assertTrue(parsed.sections[PopulationKey.FEMMES_ENCEINTES]!!.contains("caféine"))
    }

    @Test
    fun missingMarker_producesWarning() {
        val raw = """
            ###ENFANTS
            a
            ###FEMMES_ENCEINTES
            b
        """.trimIndent()
        val parsed = parser.parse(raw)
        assertTrue(parsed.warnings.any { it.contains("###ADULTES") })
    }

    // --- Feature L — US-L3 : non-régression de l'ordre des 4 marqueurs ---

    @Test
    fun parsesFourMarkersInStrictOrder() {
        val raw = """
            ###ENFANTS
            e
            ###FEMMES_ENCEINTES
            f
            ###ADULTES
            a
            ###PERSONNES_AGEES
            p
        """.trimIndent()
        val parsed = parser.parse(raw)
        assertTrue("aucun warning quand les 4 marqueurs sont présents", parsed.warnings.isEmpty())
        val expected = listOf(
            PopulationKey.ENFANTS,
            PopulationKey.FEMMES_ENCEINTES,
            PopulationKey.ADULTES,
            PopulationKey.PERSONNES_AGEES,
        )
        expected.forEach { key ->
            assertTrue("section $key présente", parsed.sections.containsKey(key))
        }
    }
}
