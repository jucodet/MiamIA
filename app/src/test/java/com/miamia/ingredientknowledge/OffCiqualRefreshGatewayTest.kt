package com.miamia.ingredientknowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ATDD — US-IKB-B3 : couverture exhaustive OFF + attributs Ciqual traçables + rejet des entrées
 * incohérentes. Parsing isolé via [OffCiqualRefreshGateway.parse] (JVM pur, sans réseau).
 *
 * Aligné sur IKB-B-FR-007/008/011, IKB-B-SC-004/005.
 */
class OffCiqualRefreshGatewayTest {

    private val baselineVersion = "baseline-0.1"
    private val eu = KbSource(KbSource.Origin.EU_ALLERGEN_LIST, baselineVersion)
    private val baselineAllergens = listOf(
        AllergenFactCard(id = "SOJA", regulatoryName = "soja", aliases = listOf("lecithine de soja"), source = eu),
    )

    private fun gateway() = OffCiqualRefreshGateway(
        baselineAllergens = baselineAllergens,
        config = KbRefreshConfig(
            offAdditivesUrl = "https://fixture/off",
            ciqualUrl = "https://fixture/ciqual",
        ),
        clock = { 0L },
    )

    private fun resource(name: String): String =
        requireNotNull(javaClass.getResourceAsStream("/ingredientkb/upstream/$name")) {
            "fixture manquante: $name"
        }.bufferedReader().readText()

    private fun payload() = gateway().parse(resource("off-additives.json"), resource("ciqual.json"))

    @Test
    fun couverture_exhaustive_off_tous_les_e_numbers_valides_sont_present() {
        val payload = payload()

        val eNumbers = payload.additives.map { it.eNumber }.toSet()
        assertTrue("E300 présent", "E300" in eNumbers)
        assertTrue("E621 présent", "E621" in eNumbers)
        assertTrue("E102 présent", "E102" in eNumbers)
        // E999 (risk_level invalide) rejeté → absent de l'index.
        assertFalse("E999 rejeté", "E999" in eNumbers)
        assertEquals("off-2026-06", payload.baseVersion)
    }

    @Test
    fun attributs_ciqual_presents_traçables_origin_ciqual_ou_omis_si_absents() {
        val byE = payload().additives.associateBy { it.eNumber }

        val e300 = byE.getValue("E300")
        assertEquals(KbSource.Origin.CIQUAL, e300.ciqual?.source?.origin)
        assertEquals(0.0, e300.ciqual?.energyKcal)

        val e621 = byE.getValue("E621")
        assertEquals(KbSource.Origin.CIQUAL, e621.ciqual?.source?.origin)
        assertEquals(4.32, e621.ciqual?.energyKcal)

        // E102 n'a pas d'attribut Ciqual dans la fixture → champ omis (repli silencieux).
        assertNull(byE.getValue("E102").ciqual)
    }

    @Test
    fun entree_incoherente_e_number_duplique_ou_risque_invalide_est_rejetee_et_tracee() {
        val payload = payload()

        // 1 E-number dupliqué (E300) + 1 risk_level invalide (E999) = 2 rejets.
        assertEquals(2, payload.rejectedEntries)
        assertFalse(payload.additives.any { it.eNumber == "E999" })
        // Une seule fiche E300 (la première conservée).
        assertEquals(1, payload.additives.count { it.eNumber == "E300" })
    }

    @Test
    fun allergenes_baseline_reportes_tels_quels_dans_le_payload() {
        val payload = payload()

        assertEquals(1, payload.allergens.size)
        assertEquals("SOJA", payload.allergens.first().id)
    }

    @Test
    fun source_ciqual_indisponible_payload_partiel_attributs_omis() {
        val payload = gateway().parse(resource("off-additives.json"), ciqualJson = "")

        assertTrue(payload.partial)
        assertFalse(payload.sourcesConsulted.contains("CIQUAL"))
        assertTrue(payload.additives.all { it.ciqual == null })
    }
}
