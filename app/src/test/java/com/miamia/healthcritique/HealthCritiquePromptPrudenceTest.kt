package com.miamia.healthcritique

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests d'héritage Feature L (persona, dimensions, hiérarchie, garde-fous, disclaimer,
 * cas particuliers) sur le prompt personnalisé Feature N (profil unique).
 * Le format 4-marqueurs strict Feature L est supersédé (tests format strict déplacés
 * vers HealthCritiqueProfilePromptTest / HealthCritiqueSectionParserTest Feature N).
 */
class HealthCritiquePromptPrudenceTest {

    private val builder = HealthCritiquePromptBuilder()

    @Test
    fun systemPrompt_containsPrudenceAndNoDiagnosis() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue(s.contains("sans diagnostic", ignoreCase = true) || s.contains("diagnostic", ignoreCase = true))
        assertTrue(s.contains("incertitudes", ignoreCase = true))
        assertTrue(s.contains("hypothèses", ignoreCase = true))
        assertTrue(s.contains("professionnel", ignoreCase = true))
        assertTrue(s.contains("diagnostic", ignoreCase = true))
        assertTrue(s.contains("illisible", ignoreCase = true))
        assertTrue(s.contains("OCR", ignoreCase = true))
    }

    // --- Feature L — US-L1 : persona expert + dimensions de risque + hiérarchie des preuves ---

    @Test
    fun systemPrompt_containsExpertPersona() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue("persona nutrition clinique", s.contains("nutrition clinique", ignoreCase = true))
        assertTrue("persona cancérologie préventive", s.contains("cancérologie préventive", ignoreCase = true))
        assertTrue("évaluation des risques alimentaires", s.contains("risques alimentaires", ignoreCase = true))
    }

    @Test
    fun systemPrompt_containsFiveRiskDimensions() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue("cancérogène", s.contains("cancérogène", ignoreCase = true))
        assertTrue("mutagène", s.contains("mutagène", ignoreCase = true))
        assertTrue("neurotoxique", s.contains("neurotoxique", ignoreCase = true))
        assertTrue("métabolique", s.contains("métabolique", ignoreCase = true))
        assertTrue("inflammatoire", s.contains("inflammatoire", ignoreCase = true))
    }

    @Test
    fun systemPrompt_containsEvidenceHierarchy() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue("faits établis", s.contains("faits établis", ignoreCase = true))
        assertTrue("incertitudes", s.contains("incertitudes", ignoreCase = true))
        assertTrue("hypothèses", s.contains("hypothèses", ignoreCase = true))
    }

    @Test
    fun systemPrompt_forbidsCategoricalConclusions() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue("conclusions catégoriques interdites", s.contains("conclusions catégoriques", ignoreCase = true))
    }

    @Test
    fun systemPrompt_containsEthicalGuardrails() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue("diagnostic", s.contains("diagnostic", ignoreCase = true))
        assertTrue("prescription", s.contains("prescription", ignoreCase = true))
        assertTrue("professionnel de santé", s.contains("professionnel", ignoreCase = true))
    }

    @Test
    fun systemPrompt_containsDisclaimer() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue(
            "disclaimer présent",
            s.contains(HealthCritiquePromptBuilder.DISCLAIMER, ignoreCase = true)
        )
    }

    @Test
    fun systemPrompt_signalsSpecialCases() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue("longue liste", s.contains("longue", ignoreCase = true))
        assertTrue("langue/illisible", s.contains("illisible", ignoreCase = true))
    }

    @Test
    fun userMessage_isRepeatableAndBounded() {
        val list = "sucre, farine, huile"
        val a = builder.buildUserMessage(list)
        val b = builder.buildUserMessage(list)
        assertEquals(a, b)
        assertTrue(a.contains("sucre"))
    }
}
