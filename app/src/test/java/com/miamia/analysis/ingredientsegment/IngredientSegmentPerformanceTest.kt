package com.miamia.analysis.ingredientsegment

import com.miamia.analysis.ingredientsegment.fixtures.OcrFixtures
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientSegmentPerformanceTest {

    private val service = IngredientSegmentPreparationService()

    @Test
    fun `preparation stays under 200ms for nominal payload`() {
        val text = OcrFixtures.NOMINAL_MULTI_LINE.trimIndent()
        val startNs = System.nanoTime()
        repeat(100) {
            service.prepare("scan-perf-$it", text)
        }
        val elapsedMs = (System.nanoTime() - startNs) / 1_000_000
        val averageMs = elapsedMs / 100.0

        assertTrue("Average extraction time should stay below 200ms, was $averageMs", averageMs < 200.0)
    }
}
