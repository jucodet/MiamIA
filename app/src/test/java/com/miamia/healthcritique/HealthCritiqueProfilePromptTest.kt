package com.miamia.healthcritique

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Feature N — US-N2 : le prompt construit pour un profil donné exige uniquement le marqueur
 * de ce profil + le rappel « Évalué pour vous : <label> », les blocs prudence/cartes/liste,
 * et préserve l'héritage Feature L (persona, dimensions, garde-fous, disclaimer).
 */
class HealthCritiqueProfilePromptTest {

    private val builder = HealthCritiquePromptBuilder()

    @Test
    fun prompt_femmeEnceinte_containsRappelAndOnlyThisMarker() {
        val s = builder.buildSystemInstruction(UserProfile.FEMME_ENCEINTE)
        assertTrue("rappel Femme enceinte", s.contains("Évalué pour vous : Femme enceinte"))
        assertTrue("marqueur FEMME_ENCEINTE", s.contains("###FEMME_ENCEINTE"))
        assertFalse("pas de marqueur ENFANT", s.contains("###ENFANT"))
        assertFalse("pas de marqueur ADULTE", s.contains("###ADULTE"))
        assertFalse("pas de marqueur PERSONNE_AGEE", s.contains("###PERSONNE_AGEE"))
        assertFalse("pas de marqueur SPORTIF", s.contains("###SPORTIF"))
    }

    @Test
    fun prompt_sportif_containsRappelAndOnlyThisMarker() {
        val s = builder.buildSystemInstruction(UserProfile.SPORTIF)
        assertTrue("rappel Sportif", s.contains("Évalué pour vous : Sportif"))
        assertTrue("marqueur SPORTIF", s.contains("###SPORTIF"))
        assertFalse("pas de marqueur FEMME_ENCEINTE", s.contains("###FEMME_ENCEINTE"))
        assertFalse("pas de marqueur ADULTE", s.contains("###ADULTE"))
    }

    @Test
    fun prompt_containsRequiredBlocksForProfileUniqueFormat() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue("Niveau de prudence", s.contains("Niveau de prudence", ignoreCase = true))
        assertTrue("Cartes d'ingrédients à vigilance", s.contains("Cartes d'ingrédients à vigilance", ignoreCase = true))
        assertTrue("Impact", s.contains("Impact :"))
        assertTrue("Fait établi", s.contains("Fait établi :"))
        assertTrue("Nuance", s.contains("Nuance :"))
        assertTrue("Cible particulièrement", s.contains("Cible particulièrement :"))
        assertTrue("Liste complète des ingrédients analysés", s.contains("Liste complète des ingrédients analysés", ignoreCase = true))
    }

    @Test
    fun prompt_preservesFeatureLHeritage() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue("persona nutrition clinique", s.contains("nutrition clinique", ignoreCase = true))
        assertTrue("persona cancérologie préventive", s.contains("cancérologie préventive", ignoreCase = true))
        assertTrue("cancérogène", s.contains("cancérogène", ignoreCase = true))
        assertTrue("mutagène", s.contains("mutagène", ignoreCase = true))
        assertTrue("neurotoxique", s.contains("neurotoxique", ignoreCase = true))
        assertTrue("métabolique", s.contains("métabolique", ignoreCase = true))
        assertTrue("inflammatoire", s.contains("inflammatoire", ignoreCase = true))
        assertTrue("diagnostic", s.contains("diagnostic", ignoreCase = true))
        assertTrue("prescription", s.contains("prescription", ignoreCase = true))
        assertTrue(HealthCritiquePromptBuilder.DISCLAIMER in s)
    }

    @Test
    fun prompt_isRepeatableForSameProfile() {
        val a = builder.buildSystemInstruction(UserProfile.SPORTIF)
        val b = builder.buildSystemInstruction(UserProfile.SPORTIF)
        assertEquals(a, b)
    }

    @Test
    fun prompt_differsAcrossProfiles() {
        val a = builder.buildSystemInstruction(UserProfile.FEMME_ENCEINTE)
        val b = builder.buildSystemInstruction(UserProfile.SPORTIF)
        assertFalse(a == b)
    }
}
