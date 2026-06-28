package com.miamia.healthcritique

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthCritiqueGroundingTest {

    @Test
    fun eNumberNotInSegment_returnsInferenceError() = runBlocking {
        val fixture = """
            Évalué pour vous : Adulte

            ###ADULTE

            Niveau de prudence : Élevé — additif non listé.

            • Conservateur X | E999 | Conservateur — Additif
              Impact : effet suspecté.
              Fait établi : classement CIRC en discussion.
              Nuance : dépend de la dose.
              Cible particulièrement : Enfants.

            Liste complète des ingrédients analysés :
            - Eau : RAS
        """.trimIndent()
        val engine = HealthCritiqueEngine(
            llmRunner = FakeHealthCritiqueLlmRunner(
                HealthCritiqueLlmGenerateResult.Success(fixture),
            ),
        )
        val r = engine.analyze(requestId = "rid", ingredientText = "eau, sucre", profile = UserProfile.ADULTE)
        assertTrue(r is HealthCritiqueResult.InferenceError)
    }
}
