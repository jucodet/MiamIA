package com.miamia.ingredientknowledge

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * ATDD — US-IKB-B2 : `FileKbCacheStore` — persistance `filesDir` atomique + repli baseline
 * sur cache absent/corrompu (IKB-B-FR-004/010, IKB-B-SC-002).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FileKbCacheStoreRobolectricTest {

    private fun context(): Context = ApplicationProvider.getApplicationContext()

    private fun store(): FileKbCacheStore = FileKbCacheStore(context())

    private fun sample(version: String = "off-2026-06"): KbCache = KbCache(
        baseVersion = version,
        additives = listOf(
            AdditiveFactCard(
                eNumber = "E300",
                canonicalName = "acide ascorbique",
                aliases = listOf("vitamine c"),
                role = "antioxydant",
                riskLevel = RiskLevel.FAIBLE,
                source = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, version),
                ciqual = CiqualAttributes(energyKcal = 0.0),
            ),
        ),
        allergens = emptyList(),
        refreshedAt = 1_000L,
        sources = listOf("OFF_ADDITIVES_TAXONOMY", "CIQUAL"),
    )

    @Test
    fun write_puis_read_round_trip_restitue_le_cache() {
        val store = store()
        val cache = sample()

        store.write(cache)
        val read = store.read()

        assertNotNull(read)
        assertEquals(cache.baseVersion, read?.baseVersion)
        assertEquals(1, read?.additives?.size)
        assertEquals("E300", read?.additives?.first()?.eNumber)
        assertEquals(0.0, read?.additives?.first()?.ciqual?.energyKcal)
        assertEquals(1_000L, read?.refreshedAt)
    }

    @Test
    fun read_renvoie_null_si_cache_absent() {
        val store = store()
        store.clear()
        assertNull(store.read())
    }

    @Test
    fun read_renvoie_null_si_cache_corrompu() {
        val store = store()
        store.write(sample())
        // Corrompt le fichier sur disque.
        File(context().filesDir, "ingredientkb/kb-cache.json").writeText("{ ceci n'est pas du json")

        assertNull(store.read())
    }

    @Test
    fun clear_supprime_le_cache_persiste() {
        val store = store()
        store.write(sample())
        assertTrue(File(context().filesDir, "ingredientkb/kb-cache.json").exists())

        store.clear()

        assertNull(store.read())
    }

    private fun assertNotNull(actual: Any?) {
        assertTrue("attendu non null", actual != null)
    }
}
