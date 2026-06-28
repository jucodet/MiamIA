package com.miamia.ingredientknowledge

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ATDD — US-IKB-B (perf) : lookup p95 < 20 ms conservé sur la base rafraîchie exhaustive
 * (IKB-B-SC-007, plan.md performance goals). Index in-memory, swap atomique non bloquant.
 *
 * Test JVM indicatif (non device) — vérifie l'absence de régression algorithmique sur l'index
 * refresh. La mesure device est à finaliser via profiling (principe IV).
 */
class RefreshableReferenceKbPerfTest {

    private val version = "off-2026-06"
    private val off = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, version)

    private fun exhaustiveBase(size: Int = 600): InMemoryReferenceKb {
        val additives = (1..size).map { i ->
            AdditiveFactCard(
                eNumber = "E${i + 100}",
                canonicalName = "additif $i",
                aliases = listOf("alias$i"),
                role = "role",
                riskLevel = if (i % 3 == 0) RiskLevel.ELEVE else if (i % 3 == 1) RiskLevel.MODERE else RiskLevel.FAIBLE,
                source = off,
                ciqual = if (i % 2 == 0) CiqualAttributes(energyKcal = i.toDouble()) else null,
            )
        }
        return InMemoryReferenceKb(version, additives, emptyList())
    }

    @Test
    fun lookup_p95_sur_base_rafraichie_exhaustive_reste_sous_20ms() {
        val cacheStore = FakeKbCacheStore()
        val baseline = exhaustiveBase(10)
        val kb = RefreshableReferenceKb(cacheStore, baseline)
        kb.publishRefreshed(exhaustiveBase(600))

        val designations = listOf(
            IngredientDesignation.fromRaw("E500"),
            IngredientDesignation.fromRaw("alias250"),
            IngredientDesignation.fromRaw("additif 42"),
            IngredientDesignation.fromRaw("substance inconnue"),
        )

        // Warmup.
        repeat(50) { kb.lookup(designations) }

        val samples = ArrayList<Long>(500)
        repeat(500) {
            val start = System.nanoTime()
            kb.lookup(designations)
            samples.add(System.nanoTime() - start)
        }
        samples.sort()

        val p95Ns = samples[(samples.size * 0.95).toInt()]
        val p95Ms = p95Ns / 1_000_000.0
        assertTrue("p95 lookup = $p95Ms ms (attendu < 20 ms)", p95Ms < 20.0)
    }
}
