package com.miamia.healthcritique

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Feature N — US-N3 : parseur profil unique (rappel, Niveau de prudence, cartes, liste
 * compacte) + rejet d'une sortie 4-marqueurs legacy.
 */
class HealthCritiqueSectionParserTest {

    private val parser = HealthCritiqueSectionParser()

    @Test
    fun parsesProfileUnique_prudenceCardsAndFullList() {
        val raw = """
            Évalué pour vous : Adulte

            ###ADULTE

            Niveau de prudence : Modéré — présence de phosphate qui peut perturber l'absorption du fer.

            • Nitrite de sodium | E250 | Conservateur — Additif
              Impact : Classé cancérogène probable (CIRC) lors de cuisson à haute température.
              Fait établi : Augmente le risque de cancer colorectal en cas de consommation régulière.
              Nuance : Le risque dépend de la fréquence et de la cuisson.
              Cible particulièrement : Enfants, Femmes enceintes.

            Liste complète des ingrédients analysés :
            - Farine de blé : RAS
            - Nitrite de sodium : Élevé
        """.trimIndent()

        val parsed = parser.parse(raw, UserProfile.ADULTE)

        assertEquals(UserProfile.ADULTE, parsed.profile)
        assertEquals("Évalué pour vous : Adulte", parsed.evaluatedForHeader)
        assertFalse(parsed.isRejectedLegacy4Markers)
        assertEquals(PrudenceLevel.MODERE, parsed.prudenceLevel)
        assertTrue(parsed.prudenceJustification?.contains("phosphate") == true)
        assertEquals(1, parsed.riskCards.size)
        val card = parsed.riskCards.first()
        assertEquals("Nitrite de sodium", card.nom)
        assertEquals("E250", card.code)
        assertEquals("Conservateur — Additif", card.type)
        assertTrue(card.impact.contains("cancérogène", ignoreCase = true))
        assertTrue(card.faitEtabli.contains("cancer colorectal", ignoreCase = true))
        assertTrue(card.nuance.contains("cuisson", ignoreCase = true))
        assertTrue(card.cibleParticulierement.contains("Femmes enceintes", ignoreCase = true))
        assertEquals(2, parsed.fullIngredientList.size)
        assertEquals("Farine de blé", parsed.fullIngredientList[0].nom)
        assertEquals(IngredientVigilanceStatut.RAS, parsed.fullIngredientList[0].statut)
        assertEquals("Nitrite de sodium", parsed.fullIngredientList[1].nom)
        assertEquals(IngredientVigilanceStatut.ELEVE, parsed.fullIngredientList[1].statut)
    }

    @Test
    fun rejectsLegacyFourMarkerOutput() {
        val raw = """
            ###ENFANTS
            e1
            ###FEMMES_ENCEINTES
            e2
            ###ADULTES
            e3
            ###PERSONNES_AGEES
            e4
        """.trimIndent()
        val parsed = parser.parse(raw, UserProfile.ADULTE)
        assertTrue("rejet legacy 4-marqueurs", parsed.isRejectedLegacy4Markers)
        assertTrue(parsed.warnings.any { it.contains("4-profils legacy") })
    }

    @Test
    fun missingPrudence_producesWarning() {
        val raw = """
            Évalué pour vous : Adulte

            ###ADULTE

            Liste complète des ingrédients analysés :
            - Eau : RAS
        """.trimIndent()
        val parsed = parser.parse(raw, UserProfile.ADULTE)
        assertTrue(parsed.prudenceLevel == null)
        assertTrue(parsed.warnings.any { it.contains("Niveau de prudence absent") })
    }
}
