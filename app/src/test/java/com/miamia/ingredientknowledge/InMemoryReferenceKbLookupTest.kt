package com.miamia.ingredientknowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ATDD — US-IKB-A1 : lookup offline additif/allergène + repli silencieux.
 * Aligné sur spec.md (US-IKB-A1 scénarios 1 & 3, edge case E-number/alias) et IKB-A-FR-003/007/012.
 */
class InMemoryReferenceKbLookupTest {

    private val baseVersion = "test-0.1"
    private val offSource = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, baseVersion)
    private val euSource = KbSource(KbSource.Origin.EU_ALLERGEN_LIST, baseVersion)

    private fun kb(): InMemoryReferenceKb = InMemoryReferenceKb(
        baseVersion = baseVersion,
        additives = listOf(
            AdditiveFactCard(
                eNumber = "E300",
                canonicalName = "acide ascorbique",
                aliases = listOf("vitamine c", "ascorbate"),
                role = "antioxydant",
                riskLevel = RiskLevel.FAIBLE,
                source = offSource,
            ),
            AdditiveFactCard(
                eNumber = "E621",
                canonicalName = "glutamate monosodique",
                aliases = listOf("msg"),
                role = "exhausteur de gout",
                riskLevel = RiskLevel.MODERE,
                source = offSource,
            ),
        ),
        allergens = listOf(
            AllergenFactCard(
                id = "SOJA",
                regulatoryName = "soja",
                aliases = listOf("lecithine de soja"),
                source = euSource,
            ),
        ),
    )

    @Test
    fun lookup_additif_par_enumber_et_alias_renvoie_la_meme_fiche_canonique() {
        val outcomeByENumber = kb().lookup(listOf(IngredientDesignation.fromRaw("E300")))
        val outcomeByAlias = kb().lookup(listOf(IngredientDesignation.fromRaw("acide ascorbique")))
        val outcomeByAliasCourant = kb().lookup(listOf(IngredientDesignation.fromRaw("vitamine C")))

        assertEquals(1, outcomeByENumber.matchedAdditives.size)
        assertEquals(1, outcomeByAlias.matchedAdditives.size)
        assertEquals(1, outcomeByAliasCourant.matchedAdditives.size)
        assertEquals("E300", outcomeByENumber.matchedAdditives[0].eNumber)
        assertEquals("E300", outcomeByAlias.matchedAdditives[0].eNumber)
        assertEquals("E300", outcomeByAliasCourant.matchedAdditives[0].eNumber)
    }

    @Test
    fun lookup_allergene_reglementaire_renvoie_la_fiche() {
        val outcome = kb().lookup(listOf(IngredientDesignation.fromRaw("lécithines de SOJA")))

        assertEquals(1, outcome.matchedAllergens.size)
        assertEquals("SOJA", outcome.matchedAllergens[0].id)
    }

    @Test
    fun lookup_substance_non_referencee_repli_silencieux_sans_invention() {
        val outcome = kb().lookup(listOf(IngredientDesignation.fromRaw("sucre de canne bio")))

        assertTrue(outcome.matchedAdditives.isEmpty())
        assertTrue(outcome.matchedAllergens.isEmpty())
        assertEquals(1, outcome.unmatchedDesignations.size)
        assertEquals("sucre de canne bio", outcome.unmatchedDesignations[0].rawText)
    }

    @Test
    fun lookup_deduplique_les_fiches_canoniques_par_enumber() {
        val outcome = kb().lookup(
            listOf(
                IngredientDesignation.fromRaw("E300"),
                IngredientDesignation.fromRaw("vitamine C"),
            )
        )

        assertEquals(1, outcome.matchedAdditives.size)
        assertEquals("E300", outcome.matchedAdditives[0].eNumber)
    }

    @Test
    fun lookup_trace_la_version_de_base() {
        assertEquals(baseVersion, kb().baseVersion())
        assertEquals(baseVersion, kb().lookup(emptyList()).baseVersion)
    }
}
