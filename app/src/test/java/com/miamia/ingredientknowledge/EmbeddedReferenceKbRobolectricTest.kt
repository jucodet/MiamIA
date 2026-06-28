package com.miamia.ingredientknowledge

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ATDD — US-IKB-A1 : charge les assets réels embarqués, vérifie baseVersion + lookup offline.
 * Aligné sur IKB-A-SC-005 (traçabilité source/version) et IKB-A-SC-006 (offline intégral).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EmbeddedReferenceKbRobolectricTest {

    private fun context(): Context = ApplicationProvider.getApplicationContext()

    @Test
    fun load_lit_les_assets_et_expose_la_version_de_base() {
        val kb = EmbeddedReferenceKb.load(context())
        assertEquals("0.1.0", kb.baseVersion())
    }

    @Test
    fun lookup_offline_additif_et_allergene_from_assets_reels() {
        val kb = EmbeddedReferenceKb.load(context())

        val outcome = kb.lookup(
            listOf(
                IngredientDesignation.fromRaw("émulsifiant : lécithines de SOJA, E300"),
                IngredientDesignation.fromRaw("sucre de canne"),
            )
        )

        assertTrue(outcome.matchedAdditives.any { it.eNumber == "E300" })
        assertTrue(outcome.matchedAllergens.any { it.id == "SOJA" })
        assertEquals("0.1.0", outcome.baseVersion)
    }

    @Test
    fun load_trace_la_source_des_fiches() {
        val kb = EmbeddedReferenceKb.load(context())
        val outcome = kb.lookup(listOf(IngredientDesignation.fromRaw("E300")))

        val additive = outcome.matchedAdditives.first { it.eNumber == "E300" }
        assertEquals(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, additive.source.origin)
        assertEquals("0.1.0", additive.source.baseVersion)
    }
}
