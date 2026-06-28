package com.miamia.ingredientknowledge

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ATDD — US-IKB-B1 / US-IKB-B2 : `RefreshableReferenceKb` — non-blocage du lookup pendant le
 * refresh + repli baseline quand cache absent/corrompu.
 *
 * Aligné sur IKB-B-FR-002/005/010, IKB-B-SC-002/006.
 */
class RefreshableReferenceKbTest {

    private val baselineVersion = "baseline-0.1"
    private val off = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, baselineVersion)
    private val baseline = InMemoryReferenceKb(
        baseVersion = baselineVersion,
        additives = listOf(
            AdditiveFactCard("E300", "acide ascorbique", riskLevel = RiskLevel.FAIBLE, source = off),
        ),
        allergens = emptyList(),
    )

    @Test
    fun lookup_repond_sur_baseline_pendant_le_refresh_non_bloquant() = runTest {
        val cacheStore = FakeKbCacheStore() // pas de cache → baseline
        val kb = RefreshableReferenceKb(cacheStore, baseline)
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val delayedPayload = KbRefreshPayload(
            additives = listOf(
                AdditiveFactCard("E621", "glutamate monosodique", riskLevel = RiskLevel.MODERE, source = off),
            ),
            allergens = emptyList(),
            baseVersion = "off-2026-06",
            sourcesConsulted = listOf("OFF_ADDITIVES_TAXONOMY"),
            rejectedEntries = 0,
            partial = false,
        )
        val coordinator = KbRefreshCoordinator(
            gateway = FakeKbRefreshGateway(payload = delayedPayload, delayMs = 500L),
            cacheStore = cacheStore,
            baseline = baseline,
            onRefreshed = kb::publishRefreshed,
            dispatcher = dispatcher,
            clock = { 100L },
        )

        // Le refresh est lancé (suspendu sur delay virtuel) — le lookup doit répondre immédiatement
        // sur la version courante (baseline), sans attendre la fin du refresh (IKB-B-SC-006).
        launch(dispatcher) { coordinator.refreshAtStartup() }

        val outcomeDuringRefresh = kb.lookup(listOf(IngredientDesignation.fromRaw("E300")))
        assertEquals(baselineVersion, outcomeDuringRefresh.baseVersion)
        assertTrue(outcomeDuringRefresh.matchedAdditives.any { it.eNumber == "E300" })

        testScheduler.advanceUntilIdle()

        // Après refresh : la nouvelle version est publiée (swap atomique).
        assertEquals("off-2026-06", kb.current().baseVersion())
        val outcomeAfterRefresh = kb.lookup(listOf(IngredientDesignation.fromRaw("E621")))
        assertTrue(outcomeAfterRefresh.matchedAdditives.any { it.eNumber == "E621" })
    }

    @Test
    fun cache_absent_ou_corrompu_repli_sur_baseline_embarquee() {
        val cacheStore = FakeKbCacheStore(corrupt = true)
        val kb = RefreshableReferenceKb(cacheStore, baseline)

        assertEquals(baselineVersion, kb.current().baseVersion())
        val outcome = kb.lookup(listOf(IngredientDesignation.fromRaw("E300")))
        assertEquals(baselineVersion, outcome.baseVersion)
        assertTrue(outcome.matchedAdditives.any { it.eNumber == "E300" })
    }

    @Test
    fun cache_valide_repli_sur_version_cachee_puis_swap_apres_refresh() = runTest {
        val cachedVersion = "cached-2026-05"
        val cacheStore = FakeKbCacheStore(
            initial = KbCache(
                baseVersion = cachedVersion,
                additives = listOf(
                    AdditiveFactCard("E300", "acide ascorbique", riskLevel = RiskLevel.FAIBLE, source = off),
                ),
                allergens = emptyList(),
                refreshedAt = 0L,
                sources = listOf("OFF_ADDITIVES_TAXONOMY"),
            ),
        )
        val kb = RefreshableReferenceKb(cacheStore, baseline)

        // Le cache valide est préféré à la baseline au démarrage (cascade cache → baseline).
        assertEquals(cachedVersion, kb.current().baseVersion())
    }
}
