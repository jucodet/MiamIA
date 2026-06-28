package com.miamia.healthcritique

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileTest {

    @Test
    fun fiveProfiles_haveFrenchLabelsAndCanonicalMarkers() {
        assertEquals("Femme enceinte", UserProfile.FEMME_ENCEINTE.label)
        assertEquals("###FEMME_ENCEINTE", UserProfile.FEMME_ENCEINTE.marker)

        assertEquals("Enfant", UserProfile.ENFANT.label)
        assertEquals("###ENFANT", UserProfile.ENFANT.marker)

        assertEquals("Agé", UserProfile.PERSONNE_AGEE.label)
        assertEquals("###PERSONNE_AGEE", UserProfile.PERSONNE_AGEE.marker)

        assertEquals("Adulte", UserProfile.ADULTE.label)
        assertEquals("###ADULTE", UserProfile.ADULTE.marker)

        assertEquals("Sportif", UserProfile.SPORTIF.label)
        assertEquals("###SPORTIF", UserProfile.SPORTIF.marker)
    }

    @Test
    fun default_isAdulte() {
        assertEquals(UserProfile.ADULTE, UserProfile.DEFAULT)
    }

    @Test
    fun evaluatedForHeader_usesProfileLabel() {
        assertEquals("Évalué pour vous : Femme enceinte", UserProfile.evaluatedForHeader(UserProfile.FEMME_ENCEINTE))
        assertEquals("Évalué pour vous : Sportif", UserProfile.evaluatedForHeader(UserProfile.SPORTIF))
    }

    @Test
    fun legacyFourProfileMarkers_areTheFeatureLRetiredMarkers() {
        assertEquals(
            listOf("###ENFANTS", "###FEMMES_ENCEINTES", "###ADULTES", "###PERSONNES_AGEES"),
            UserProfile.legacyFourProfileMarkers,
        )
    }

    @Test
    fun defaultProvider_returnsAdulte_byDefault() {
        val provider = DefaultUserProfileProvider()
        assertEquals(UserProfile.ADULTE, provider.current())
    }

    @Test
    fun defaultProvider_override_returnsSelectedProfile() {
        val provider = DefaultUserProfileProvider()
        provider.override = UserProfile.FEMME_ENCEINTE
        assertEquals(UserProfile.FEMME_ENCEINTE, provider.current())
    }

    @Test
    fun prudenceLevel_parseOrNull_handlesAccentsAndCase() {
        assertEquals(PrudenceLevel.FAIBLE, PrudenceLevel.parseOrNull("Faible"))
        assertEquals(PrudenceLevel.MODERE, PrudenceLevel.parseOrNull("Modéré"))
        assertEquals(PrudenceLevel.ELEVE, PrudenceLevel.parseOrNull("Élevé"))
        assertEquals(PrudenceLevel.MODERE, PrudenceLevel.parseOrNull("modéré"))
        assertTrue(PrudenceLevel.parseOrNull("Inconnu") == null)
    }

    @Test
    fun vigilanceStatut_parseOrNull_handlesLabels() {
        assertEquals(IngredientVigilanceStatut.RAS, IngredientVigilanceStatut.parseOrNull("RAS"))
        assertEquals(IngredientVigilanceStatut.MODERE, IngredientVigilanceStatut.parseOrNull("Modéré"))
        assertEquals(IngredientVigilanceStatut.ELEVE, IngredientVigilanceStatut.parseOrNull("Élevé"))
    }
}
