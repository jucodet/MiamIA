package com.miamia.healthcritique

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthCritiqueGroundingTest {

    @Test
    fun eNumberNotInSegment_returnsInferenceError() = runBlocking {
        val fixture = """
            ###ENFANTS
            Contient E999 non listé.
            ###FEMMES_ENCEINTES
            ok
            ###ADULTES
            ok
            ###PERSONNES_AGEES
            ok
        """.trimIndent()
        val engine = HealthCritiqueEngine(
            llmRunner = FakeHealthCritiqueLlmRunner(
                HealthCritiqueLlmGenerateResult.Success(fixture),
            ),
        )
        val r = engine.analyze(requestId = "rid", ingredientText = "eau, sucre")
        assertTrue(r is HealthCritiqueResult.InferenceError)
    }
}
