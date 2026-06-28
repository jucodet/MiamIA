package com.miamia.healthcritique

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Feature Q — US-Q1 : le prompt de critique santé contient une contrainte explicite de
 * concision maximale (`ConcisionDirective`), bornée par le format strict Feature N
 * (rappel + marqueur unique + 3 blocs), et préservant l'ancrage Feature C + les garde-fous.
 *
 * Réf. spec Feature Q — `IHI-Q-FR-001`..`006` / `IHI-Q-SC-001`/`002`/`006`.
 */
class HealthCritiquePromptConcisionTest {

    private val builder = HealthCritiquePromptBuilder()

    @Test
    fun prompt_containsConcisionDirective() {
        val s = builder.buildSystemInstruction(UserProfile.FEMME_ENCEINTE)
        assertTrue("bloc CONCISION MAXIMALE", s.contains("CONCISION MAXIMALE", ignoreCase = true))
        assertTrue("formulations courtes", s.contains("formulations courtes", ignoreCase = true))
        assertTrue("pas de préambule", s.contains("préambule", ignoreCase = true))
        assertTrue("pas de prose narrative", s.contains("prose narrative", ignoreCase = true))
        assertTrue("pas de répétitions", s.contains("répétition", ignoreCase = true))
    }

    @Test
    fun prompt_concisionBoundedToStrictFormat() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue("borne format strict (ne supprime ni ne fusionne)",
            s.contains("ne supprime ni ne fusionne", ignoreCase = true) ||
                s.contains("sans supprimer", ignoreCase = true))
        // Préservation des 3 blocs Feature N.
        assertTrue("Niveau de prudence préservé", s.contains("Niveau de prudence", ignoreCase = true))
        assertTrue("Cartes à vigilance préservées", s.contains("Cartes d'ingrédients à vigilance", ignoreCase = true))
        assertTrue("Liste complète préservée", s.contains("Liste complète des ingrédients analysés", ignoreCase = true))
        // Rappel + marqueur uniques.
        assertTrue("rappel préservé", s.contains("Évalué pour vous : Adulte"))
        assertTrue("marqueur unique préservé", s.contains("###ADULTE"))
    }

    @Test
    fun prompt_concisionIndicativeThresholdsPresent() {
        val s = builder.buildSystemInstruction(UserProfile.FEMME_ENCEINTE)
        assertTrue("seuil indicatif justificatif prudence (25 mots)", s.contains("25 mots"))
        assertTrue("seuil indicatif sous-ligne carte (15 mots)", s.contains("15 mots"))
        assertTrue("références CIRC compactes", s.contains("CIRC", ignoreCase = true))
    }

    @Test
    fun prompt_concisionPreservesAnchoringAndGuardrails() {
        val s = builder.buildSystemInstruction(UserProfile.ADULTE)
        assertTrue("ancrage préservé (ne pas inventer/résumer non ancré)",
            s.contains("non ancré", ignoreCase = true))
        assertTrue("garde-fou diagnostic préservé", s.contains("diagnostic", ignoreCase = true))
        assertTrue("garde-fou prescription préservé", s.contains("prescription", ignoreCase = true))
        assertTrue(HealthCritiquePromptBuilder.DISCLAIMER in s)
    }

    @Test
    fun prompt_concisionIsRepeatable() {
        val a = builder.buildSystemInstruction(UserProfile.SPORTIF)
        val b = builder.buildSystemInstruction(UserProfile.SPORTIF)
        assertEquals(a, b)
    }
}
