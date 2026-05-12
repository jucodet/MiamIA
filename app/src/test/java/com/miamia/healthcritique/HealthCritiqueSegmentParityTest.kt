package com.miamia.healthcritique

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthCritiqueSegmentParityTest {

    @Test
    fun analyze_passesUserMessageBuiltFromSegment_only_SC005() = runBlocking {
        val segment = "eau, sucre, farine de blé, sel"
        val promptBuilder = HealthCritiquePromptBuilder()
        val expectedUser = promptBuilder.buildUserMessage(segment)
        val fake = FakeHealthCritiqueLlmRunner(
            HealthCritiqueLlmGenerateResult.Success(
                """
                ###ENFANTS
                x
                ###FEMMES_ENCEINTES
                x
                ###ADULTES
                x
                ###PERSONNES_AGEES
                x
                """.trimIndent(),
            ),
        )
        val engine = HealthCritiqueEngine(
            promptBuilder = promptBuilder,
            llmRunner = fake,
        )
        engine.analyze(requestId = "rid", ingredientText = segment)
        assertEquals(expectedUser, fake.lastUserMessage())
    }
}
