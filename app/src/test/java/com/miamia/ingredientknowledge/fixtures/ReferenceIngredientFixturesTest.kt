package com.miamia.ingredientknowledge.fixtures

import com.miamia.ingredientknowledge.ReferenceContextBuilder
import com.miamia.ingredientknowledge.ReferenceContextQualification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ATDD — US-IKB-A3 : vérification sur le jeu fixe d'ingrédients de référence.
 *
 * Valide : lookup → fiches attendues exactes (référencées) + aucune pour les non référencées ;
 * contexte + qualification « général » conformes ; répétabilité ≥ 3 exécutions (IKB-A-SC-004).
 */
class ReferenceIngredientFixturesTest {

    @Test
    fun lookup_sur_jeu_fixe_renvoie_les_fiches_attendues() {
        val outcome = ReferenceIngredientFixtures.kb.lookup(ReferenceIngredientFixtures.designations)

        assertEquals(
            ReferenceIngredientFixtures.expectedAdditiveNumbers,
            outcome.matchedAdditives.map { it.eNumber }.toSet(),
        )
        assertEquals(
            ReferenceIngredientFixtures.expectedAllergenIds,
            outcome.matchedAllergens.map { it.id }.toSet(),
        )
        assertEquals(
            ReferenceIngredientFixtures.expectedUnmatchedRaw,
            outcome.unmatchedDesignations.map { it.rawText }.toSet(),
        )
        assertEquals(ReferenceIngredientFixtures.BASE_VERSION, outcome.baseVersion)
    }

    @Test
    fun contexte_sur_jeu_fixe_qualifie_general_et_borne() {
        val outcome = ReferenceIngredientFixtures.kb.lookup(ReferenceIngredientFixtures.designations)
        val ctx = ReferenceContextBuilder().build(outcome)

        assertEquals(ReferenceContextQualification.GENERAL, ctx.qualification)
        assertTrue(ctx.cards.size <= ReferenceContextBuilder.DEFAULT_MAX_CARDS)
        // 3 allergènes prioritaires + 3 additifs = 6 ≤ 12
        assertEquals(6, ctx.cards.size)
    }

    @Test
    fun repetabilite_3_executions_resultats_identiques() {
        val run1 = ReferenceIngredientFixtures.kb.lookup(ReferenceIngredientFixtures.designations)
        val run2 = ReferenceIngredientFixtures.kb.lookup(ReferenceIngredientFixtures.designations)
        val run3 = ReferenceIngredientFixtures.kb.lookup(ReferenceIngredientFixtures.designations)

        assertEquals(run1, run2)
        assertEquals(run2, run3)
        assertEquals(
            run1.matchedAdditives.map { it.eNumber }.toSet(),
            run3.matchedAdditives.map { it.eNumber }.toSet(),
        )
    }
}
