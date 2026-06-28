package com.miamia.healthcritique

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthCritiqueEngineTest {

    @Test
    fun inputInvalid_nullSegment_skipsLlm_noValidatedSegment() = runBlocking {
        val fake = FakeHealthCritiqueLlmRunner(
            HealthCritiqueLlmGenerateResult.Success("should not be used"),
        )
        val engine = HealthCritiqueEngine(llmRunner = fake)
        val r = engine.analyze(requestId = "rid", ingredientText = null)
        assertTrue(r is HealthCritiqueResult.InputInvalid)
        val inv = r as HealthCritiqueResult.InputInvalid
        assertEquals(InputInvalidReason.NO_VALIDATED_SEGMENT, inv.reasonCode)
        assertNull(fake.lastUserMessage())
    }

    @Test
    fun inputInvalid_blankSegment_skipsLlm_empty() = runBlocking {
        val fake = FakeHealthCritiqueLlmRunner(
            HealthCritiqueLlmGenerateResult.Success("should not be used"),
        )
        val engine = HealthCritiqueEngine(llmRunner = fake)
        val r = engine.analyze(requestId = "rid", ingredientText = "  ")
        assertTrue(r is HealthCritiqueResult.InputInvalid)
        val inv = r as HealthCritiqueResult.InputInvalid
        assertEquals(InputInvalidReason.EMPTY, inv.reasonCode)
        assertNull(fake.lastUserMessage())
    }

    // --- Feature N — US-N4 : profil unique parsé + fallback Adulte + rejet 4-marqueurs ---

    @Test
    fun fakeSuccess_profileUnique_returnsCritiqueReady_withProfileCritique() = runBlocking {
        val fixture = """
            Évalué pour vous : Adulte

            ###ADULTE

            Niveau de prudence : Modéré — phosphate.

            • Nitrite de sodium | E250 | Conservateur — Additif
              Impact : cancérogène probable.
              Fait établi : risque cancer colorectal.
              Nuance : dépend de la fréquence.
              Cible particulièrement : Enfants, Femmes enceintes.

            Liste complète des ingrédients analysés :
            - Eau : RAS
            - Nitrite de sodium : Élevé
        """.trimIndent()
        val engine = HealthCritiqueEngine(
            llmRunner = FakeHealthCritiqueLlmRunner(
                HealthCritiqueLlmGenerateResult.Success(fixture),
            ),
        )
        val r = engine.analyze(
            requestId = "rid",
            ingredientText = "eau, nitrite de sodium (E250)",
            profile = UserProfile.ADULTE,
        )
        assertTrue(r is HealthCritiqueResult.CritiqueReady)
        val ok = r as HealthCritiqueResult.CritiqueReady
        assertEquals(UserProfile.ADULTE, ok.profile)
        assertEquals(PrudenceLevel.MODERE, ok.profileCritique.prudenceLevel)
        assertEquals(1, ok.profileCritique.riskCards.size)
        assertEquals(2, ok.profileCritique.fullIngredientList.size)
        assertTrue(ok.isDefaultProfile)
    }

    @Test
    fun fakeSuccess_legacyFourMarkers_returnsInferenceError() = runBlocking {
        val fixture = """
            ###ENFANTS
            e1
            ###FEMMES_ENCEINTES
            e2
            ###ADULTES
            e3
            ###PERSONNES_AGEES
            e4
        """.trimIndent()
        val engine = HealthCritiqueEngine(
            llmRunner = FakeHealthCritiqueLlmRunner(
                HealthCritiqueLlmGenerateResult.Success(fixture),
            ),
        )
        val r = engine.analyze(requestId = "rid", ingredientText = "eau, sucre, farine")
        assertTrue(r is HealthCritiqueResult.InferenceError)
    }

    @Test
    fun fakeFailure_returnsInferenceError() = runBlocking {
        val engine = HealthCritiqueEngine(
            llmRunner = FakeHealthCritiqueLlmRunner(
                HealthCritiqueLlmGenerateResult.Failure(
                    HealthInferenceErrorCode.GEMMA_NOT_FOUND,
                    "msg",
                ),
            ),
        )
        val r = engine.analyze(requestId = "rid", ingredientText = "eau, sucre, farine")
        assertTrue(r is HealthCritiqueResult.InferenceError)
    }
}
