package com.miamia.healthcritique

import org.junit.Assert.assertEquals
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
        assertTrue(s.contains("capture", ignoreCase = true))
        assertTrue(s.contains("OCR", ignoreCase = true))
    }

    // --- Feature L — US-L1 : persona expert + dimensions de risque + hiérarchie des preuves ---

    @Test
    fun systemPrompt_containsExpertPersona() {
        val s = builder.buildSystemInstruction()
        assertTrue("persona nutrition clinique", s.contains("nutrition clinique", ignoreCase = true))
        assertTrue("persona cancérologie préventive", s.contains("cancérologie préventive", ignoreCase = true))
        assertTrue("évaluation des risques alimentaires", s.contains("risques alimentaires", ignoreCase = true))
    }

    @Test
    fun systemPrompt_containsFiveRiskDimensions() {
        val s = builder.buildSystemInstruction()
        assertTrue("cancérogène", s.contains("cancérogène", ignoreCase = true))
        assertTrue("mutagène", s.contains("mutagène", ignoreCase = true))
        assertTrue("neurotoxique", s.contains("neurotoxique", ignoreCase = true))
        assertTrue("métabolique", s.contains("métabolique", ignoreCase = true))
        assertTrue("inflammatoire", s.contains("inflammatoire", ignoreCase = true))
    }

    @Test
    fun systemPrompt_containsEvidenceHierarchyAndIarcWho() {
        val s = builder.buildSystemInstruction()
        assertTrue("faits établis", s.contains("faits établis", ignoreCase = true))
        assertTrue("incertitudes scientifiques", s.contains("incertitudes scientifiques", ignoreCase = true))
        assertTrue("hypothèses", s.contains("hypothèses", ignoreCase = true))
        assertTrue("CIRC", s.contains("CIRC", ignoreCase = true))
        assertTrue("OMS", s.contains("OMS", ignoreCase = true))
    }

    @Test
    fun systemPrompt_forbidsCategoricalConclusions() {
        val s = builder.buildSystemInstruction()
        assertTrue("toujours toxique interdit", s.contains("toujours toxique", ignoreCase = true))
        assertTrue("poison interdit", s.contains("poison", ignoreCase = true))
    }

    // --- Feature L — US-L2 : populations vulnérables + garde-fous éthiques + cas particuliers ---

    @Test
    fun systemPrompt_containsVulnerablePopulations() {
        val s = builder.buildSystemInstruction()
        assertTrue("femmes enceintes/allaitantes", s.contains("enceintes", ignoreCase = true))
        assertTrue("enfants", s.contains("enfants", ignoreCase = true))
        assertTrue("immunodéprimées", s.contains("immunodéprim", ignoreCase = true))
        assertTrue("antécédents familiaux cancer", s.contains("antécédents familiaux", ignoreCase = true))
    }

    @Test
    fun systemPrompt_containsEthicalGuardrails() {
        val s = builder.buildSystemInstruction()
        assertTrue("diagnostic", s.contains("diagnostic", ignoreCase = true))
        assertTrue("prescription", s.contains("prescription", ignoreCase = true))
        assertTrue("professionnel de santé", s.contains("professionnel", ignoreCase = true))
    }

    @Test
    fun systemPrompt_containsDisclaimer() {
        val s = builder.buildSystemInstruction()
        assertTrue(
            "disclaimer présent",
            s.contains(HealthCritiquePromptBuilder.DISCLAIMER, ignoreCase = true)
        )
    }

    @Test
    fun systemPrompt_signalsOpacityAndSpecialCases() {
        val s = builder.buildSystemInstruction()
        assertTrue("opacité arômes", s.contains("arômes", ignoreCase = true))
        assertTrue("longue liste → synthèse", s.contains("synthèse", ignoreCase = true))
        assertTrue("langue/illisible", s.contains("illisible", ignoreCase = true))
    }

    // --- Feature L — US-L3 : format de sortie strict ---

    @Test
    fun systemPrompt_containsStrictSectionMarkersInOrder() {
        val s = builder.buildSystemInstruction()
        val markers = listOf("###ENFANTS", "###FEMMES_ENCEINTES", "###ADULTES", "###PERSONNES_AGEES")
        var previous = -1
        for (m in markers) {
            val idx = s.indexOf(m)
            assertTrue("marqueur présent : $m", idx >= 0)
            assertTrue("marqueur $m après le précédent (ordre)", idx > previous)
            previous = idx
        }
    }

    @Test
    fun systemPrompt_instructsNoTextBeforeFirstMarker() {
        val s = builder.buildSystemInstruction()
        assertTrue(
            "instruction 'aucun texte avant ###ENFANTS' présente",
            s.contains("avant la ligne ###ENFANTS", ignoreCase = true) ||
                s.contains("avant ###ENFANTS", ignoreCase = true)
        )
    }

    @Test
    fun systemPrompt_containsThreeRequiredBlocks() {
        val s = builder.buildSystemInstruction()
        assertTrue("Points de vigilance", s.contains("Points de vigilance", ignoreCase = true))
        assertTrue("Analyse par ingrédient", s.contains("Analyse par ingrédient", ignoreCase = true))
        assertTrue("Niveau de prudence", s.contains("Niveau de prudence", ignoreCase = true))
    }

    @Test
    fun systemPrompt_isRepeatable() {
        val a = builder.buildSystemInstruction()
        val b = builder.buildSystemInstruction()
        assertEquals("buildSystemInstruction déterministe", a, b)
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
