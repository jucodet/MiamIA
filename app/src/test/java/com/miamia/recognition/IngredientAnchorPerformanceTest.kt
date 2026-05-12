package com.miamia.recognition

import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientAnchorPerformanceTest {
    private val pipeline = IngredientExtractionPipeline()

    @Test
    fun anchor_detection_average_stays_under_200ms() {
        val payload = IngredientAnchorFixtures.INTRO_THEN_LIST_WITH_SPACE.trimIndent()
        val started = System.nanoTime()
        repeat(200) {
            pipeline.detectAnchors("scan-$it", payload)
        }
        val elapsedMs = (System.nanoTime() - started) / 1_000_000
        val averageMs = elapsedMs / 200.0
        assertTrue("Average anchor detection should stay under 200ms, was $averageMs", averageMs < 200.0)
    }
}
