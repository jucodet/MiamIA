package com.miamia.ingredientknowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ATDD — US-IKB-A2 : contract test du read-model `ReferenceContext`
 * (contracts/reference-context-read-model.md).
 *
 * Vérifie les garde-fous : qualification GENERAL, ensemble borné, aucune entrée ne déclare
 * un alias hors normalisation comme équivalence.
 */
class ReferenceContextContractTest {

    private val baseVersion = "test-0.1"
    private val off = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, baseVersion)

    @Test
    fun read_model_qualification_general_obligatoire() {
        val outcome = LookupOutcome(
            matchedAdditives = listOf(
                AdditiveFactCard("E300", "acide ascorbique", riskLevel = RiskLevel.FAIBLE, source = off),
            ),
            matchedAllergens = emptyList(),
            unmatchedDesignations = emptyList(),
            baseVersion = baseVersion,
        )
        val ctx = ReferenceContextBuilder().build(outcome)

        assertEquals(ReferenceContextQualification.GENERAL, ctx.qualification)
        assertEquals(baseVersion, ctx.baseVersion)
    }

    @Test
    fun read_model_toujours_borne_par_plafond() {
        val additives = (1..50).map {
            AdditiveFactCard("E$it", "nom $it", riskLevel = RiskLevel.FAIBLE, source = off)
        }
        val outcome = LookupOutcome(
            matchedAdditives = additives,
            matchedAllergens = emptyList(),
            unmatchedDesignations = emptyList(),
            baseVersion = baseVersion,
        )
        val ctx = ReferenceContextBuilder().build(outcome)

        assertTrue(ctx.cards.size <= ReferenceContextBuilder.DEFAULT_MAX_CARDS)
    }

    @Test
    fun read_model_entries_ne_portent_aucune_regle_de_synonyme() {
        val outcome = LookupOutcome(
            matchedAdditives = listOf(
                AdditiveFactCard("E300", "acide ascorbique", aliases = listOf("vitamine c"), riskLevel = RiskLevel.FAIBLE, source = off),
            ),
            matchedAllergens = emptyList(),
            unmatchedDesignations = emptyList(),
            baseVersion = baseVersion,
        )
        val ctx = ReferenceContextBuilder().build(outcome)

        // Le read-model publie la clé canonique (E-number) + libellé canonique, jamais les alias
        // comme équivalence admise — cohérent avec IKB-A-FR-006 / IHI-C-FR-005.
        val entry = ctx.cards.single()
        assertEquals("E300", entry.key)
        assertEquals("acide ascorbique", entry.display)
    }

    @Test
    fun read_model_avec_attributs_ciqual_reste_qualification_general_feature_c() {
        val ciqualSource = KbSource(KbSource.Origin.CIQUAL, baseVersion)
        val outcome = LookupOutcome(
            matchedAdditives = listOf(
                AdditiveFactCard(
                    eNumber = "E300",
                    canonicalName = "acide ascorbique",
                    riskLevel = RiskLevel.FAIBLE,
                    source = off,
                    ciqual = CiqualAttributes(energyKcal = 0.0, source = ciqualSource),
                ),
            ),
            matchedAllergens = emptyList(),
            unmatchedDesignations = emptyList(),
            baseVersion = baseVersion,
        )
        val ctx = ReferenceContextBuilder().build(outcome)

        // Feature C : les attributs Ciqual sont publiés comme contenu général — qualification
        // GENERAL inchangée, aucun fait étiquette (IKB-B-FR-009).
        assertEquals(ReferenceContextQualification.GENERAL, ctx.qualification)
        val entry = ctx.cards.single()
        assertEquals(0.0, entry.ciqualEnergyKcal)
    }
}
