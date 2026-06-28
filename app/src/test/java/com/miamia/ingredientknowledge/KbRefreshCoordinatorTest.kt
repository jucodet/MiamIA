package com.miamia.ingredientknowledge

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ATDD — US-IKB-B1 / US-IKB-B2 : refresh au démarrage + repli offline.
 * Aligné sur spec.md (US-IKB-B1 scénario succès, US-IKB-B2 scénario offline) et
 * IKB-B-FR-001/002/003/005, IKB-B-SC-001/003.
 */
class KbRefreshCoordinatorTest {

    private val baselineVersion = "baseline-0.1"
    private val off = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, baselineVersion)
    private val baseline = InMemoryReferenceKb(
        baseVersion = baselineVersion,
        additives = listOf(
            AdditiveFactCard("E300", "acide ascorbique", riskLevel = RiskLevel.FAIBLE, source = off),
        ),
        allergens = emptyList(),
    )

    private fun refreshedPayload(version: String = "off-2026-06"): KbRefreshPayload = KbRefreshPayload(
        additives = listOf(
            AdditiveFactCard("E300", "acide ascorbique", riskLevel = RiskLevel.FAIBLE, source = off),
            AdditiveFactCard("E621", "glutamate monosodique", riskLevel = RiskLevel.MODERE, source = off),
        ),
        allergens = emptyList(),
        baseVersion = version,
        sourcesConsulted = listOf("OFF_ADDITIVES_TAXONOMY", "CIQUAL"),
        rejectedEntries = 0,
        partial = false,
    )

    @Test
    fun refresh_succes_publie_nouvelle_version_ecrit_cache_et_trace_success() = runTest {
        val cache = FakeKbCacheStore()
        var published: ReferenceKb? = null
        val coordinator = KbRefreshCoordinator(
            gateway = FakeKbRefreshGateway(payload = refreshedPayload()),
            cacheStore = cache,
            baseline = baseline,
            onRefreshed = { published = it },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            clock = { 1_000L },
        )

        val outcome = coordinator.refreshAtStartup()

        assertEquals(KbRefreshOutcome.Status.SUCCESS, outcome.status)
        assertEquals("off-2026-06", outcome.refreshedVersion)
        assertEquals(listOf("OFF_ADDITIVES_TAXONOMY", "CIQUAL"), outcome.sourcesConsulted)
        assertEquals(1, cache.writeCount)
        assertEquals("off-2026-06", cache.written?.baseVersion)
        assertEquals(1_000L, cache.written?.refreshedAt)
        assertNotNull(published)
        assertEquals("off-2026-06", published?.baseVersion())
    }

    @Test
    fun refresh_partiel_publie_version_et_trace_partial() = runTest {
        val cache = FakeKbCacheStore()
        var published: ReferenceKb? = null
        val coordinator = KbRefreshCoordinator(
            gateway = FakeKbRefreshGateway(payload = refreshedPayload().copy(partial = true)),
            cacheStore = cache,
            baseline = baseline,
            onRefreshed = { published = it },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            clock = { 2_000L },
        )

        val outcome = coordinator.refreshAtStartup()

        assertEquals(KbRefreshOutcome.Status.PARTIAL, outcome.status)
        assertEquals("off-2026-06", outcome.refreshedVersion)
        assertNotNull(published)
    }

    @Test
    fun refresh_echec_reseau_repli_offline_fallback_cache_conserve_non_bloquant() = runTest {
        val cache = FakeKbCacheStore()
        var published: ReferenceKb? = null
        val coordinator = KbRefreshCoordinator(
            gateway = FakeKbRefreshGateway(error = KbRefreshUnavailable("HTTP 503")),
            cacheStore = cache,
            baseline = baseline,
            onRefreshed = { published = it },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            clock = { 3_000L },
        )

        val outcome = coordinator.refreshAtStartup()

        assertEquals(KbRefreshOutcome.Status.OFFLINE_FALLBACK, outcome.status)
        assertNull(outcome.refreshedVersion)
        assertEquals(0, cache.writeCount)
        assertNull(published)
        assertFalse(outcome.reason.isNullOrBlank())
    }
}
