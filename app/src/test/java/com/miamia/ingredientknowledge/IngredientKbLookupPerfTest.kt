package com.miamia.ingredientknowledge

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sanity check performance — lookup p95 < 20 ms pour ≤ 50 désignations (plan.md Performance Goals,
 * constitution principe IV). Micro-benchmark JVM sur [InMemoryReferenceKb].
 *
 * Non bloquant : garde-fou grossier orienté régression, pas un benchmark formel.
 */
class IngredientKbLookupPerfTest {

    @Test
    fun lookup_50_designations_sous_20ms_p95() {
        val baseVersion = "perf-0.1"
        val off = KbSource(KbSource.Origin.OFF_ADDITIVES_TAXONOMY, baseVersion)
        val additives = (1..50).map {
            AdditiveFactCard("E$it", "additif $it", listOf("alias $it"), "role", RiskLevel.FAIBLE, off)
        }
        val kb = InMemoryReferenceKb(baseVersion, additives, allergens = emptyList())

        val designations = (1..50).map { IngredientDesignation.fromRaw("additif $it, sucre") }

        val samples = mutableListOf<Long>()
        repeat(20) {
            val start = System.nanoTime()
            kb.lookup(designations)
            samples += System.nanoTime() - start
        }
        samples.sort()
        val p95Ns = samples[(samples.size * 0.95).toInt().coerceAtMost(samples.size - 1)]
        val p95Ms = p95Ns / 1_000_000.0

        assertTrue("p95 lookup trop élevé: ${p95Ms}ms", p95Ms < 20.0)
    }
}
