package com.miamia.ingredientknowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ATDD — US-IKB-A2 : ReferenceContext borné + priorisé + qualifié GENERAL, sans fait étiquette.
 * Aligné sur spec.md US-IKB-A2 scénario 1 et IKB-A-FR-004/005/011.
 */
class ReferenceContextBuilderTest {

    private val baseVersion = "test-0.1"
    private val off = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, baseVersion)
    private val eu = KbSource(KbSource.Origin.EU_ALLERGEN_LIST, baseVersion)

    private fun additive(e: String, risk: RiskLevel) = AdditiveFactCard(
        eNumber = e, canonicalName = "nom $e", riskLevel = risk, source = off,
    )

    private fun allergen(id: String) = AllergenFactCard(
        id = id, regulatoryName = "nom $id", source = eu,
    )

    @Test
    fun qualification_toujours_general_aucun_fait_etiquette() {
        val outcome = LookupOutcome(
            matchedAdditives = listOf(additive("E300", RiskLevel.FAIBLE)),
            matchedAllergens = emptyList(),
            unmatchedDesignations = emptyList(),
            baseVersion = baseVersion,
        )
        val ctx = ReferenceContextBuilder().build(outcome)

        assertEquals(ReferenceContextQualification.GENERAL, ctx.qualification)
        assertTrue(ctx.cards.none { it.display.contains("étiquette", ignoreCase = true) })
    }

    @Test
    fun plafond_borne_le_nombre_de_fiches_injectees() {
        val outcome = LookupOutcome(
            matchedAdditives = (1..20).map { additive("E$it", RiskLevel.FAIBLE) },
            matchedAllergens = emptyList(),
            unmatchedDesignations = emptyList(),
            baseVersion = baseVersion,
        )
        val ctx = ReferenceContextBuilder(maxCards = 5).build(outcome)

        assertEquals(5, ctx.cards.size)
    }

    @Test
    fun priorisation_allergenes_puis_risque_eleve_puis_modere_puis_faible() {
        val outcome = LookupOutcome(
            matchedAdditives = listOf(
                additive("E_FAIBLE", RiskLevel.FAIBLE),
                additive("E_ELEVE", RiskLevel.ELEVE),
                additive("E_MODERE", RiskLevel.MODERE),
            ),
            matchedAllergens = listOf(allergen("SOJA")),
            unmatchedDesignations = emptyList(),
            baseVersion = baseVersion,
        )
        val ctx = ReferenceContextBuilder(maxCards = 4).build(outcome)

        assertEquals(4, ctx.cards.size)
        assertEquals(ReferenceContextEntry.Kind.ALLERGEN, ctx.cards[0].kind)
        assertEquals("SOJA", ctx.cards[0].key)
        assertEquals("E_ELEVE", ctx.cards[1].key)
        assertEquals("E_MODERE", ctx.cards[2].key)
        assertEquals("E_FAIBLE", ctx.cards[3].key)
    }

    @Test
    fun fiches_au_dela_du_plafond_omises_sans_blocage() {
        val outcome = LookupOutcome(
            matchedAdditives = (1..10).map { additive("E$it", RiskLevel.ELEVE) },
            matchedAllergens = listOf(allergen("LAIT")),
            unmatchedDesignations = emptyList(),
            baseVersion = baseVersion,
        )
        val ctx = ReferenceContextBuilder(maxCards = 3).build(outcome)

        assertEquals(3, ctx.cards.size)
        assertEquals("LAIT", ctx.cards[0].key)
    }

    @Test
    fun lookup_sans_correspondance_contexte_vide_nominal() {
        val outcome = LookupOutcome(
            matchedAdditives = emptyList(),
            matchedAllergens = emptyList(),
            unmatchedDesignations = listOf(IngredientDesignation.fromRaw("sucre")),
            baseVersion = baseVersion,
        )
        val ctx = ReferenceContextBuilder().build(outcome)

        assertTrue(ctx.cards.isEmpty())
        assertEquals(baseVersion, ctx.baseVersion)
    }
}
