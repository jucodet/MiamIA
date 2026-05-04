package com.foodgpt.healthcritique

import org.junit.Assert.assertTrue
import org.junit.Test

class HealthCritiquePromptPrudenceTest {

    private val builder = HealthCritiquePromptBuilder()

    @Test
    fun systemPrompt_containsPrudenceAndNoDiagnosis() {
        val s = builder.buildSystemInstruction()
        assertTrue(s.contains("sans diagnostic", ignoreCase = true))
        assertTrue(s.contains("incertitudes", ignoreCase = true))
        assertTrue(s.contains("hypothèses", ignoreCase = true))
        assertTrue(s.contains("professionnel", ignoreCase = true))
        assertTrue(s.contains("grossesse", ignoreCase = true))
        assertTrue(s.contains("ambigu", ignoreCase = true))
        assertTrue(s.contains("diagnostic", ignoreCase = true))
    }
}
