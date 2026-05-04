package com.foodgpt.healthcritique

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthCritiqueEngineTest {

    @Test
    fun inputInvalid_skipsLlm() = runBlocking {
        val engine = HealthCritiqueEngine(
            llmRunner = FakeHealthCritiqueLlmRunner(
                HealthCritiqueLlmGenerateResult.Success("should not be used"),
            ),
        )
        val r = engine.analyze(requestId = "rid", ingredientText = "  ")
        assertTrue(r is HealthCritiqueResult.InputInvalid)
    }

    @Test
    fun fakeSuccess_returnsCritiqueReady() = runBlocking {
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
        assertTrue(r is HealthCritiqueResult.CritiqueReady)
        val ok = r as HealthCritiqueResult.CritiqueReady
        assertEquals("e1", ok.sections[PopulationKey.ENFANTS]?.trim())
        assertEquals("e4", ok.sections[PopulationKey.PERSONNES_AGEES]?.trim())
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
