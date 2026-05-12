package com.miamia.additives

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdditiveKpiParserPerformanceTest {

    @Test
    fun parse_eightyLines_completesQuickly() {
        val lines = (1..80).joinToString("\n") { i ->
            "VERT|E$i|Court $i"
        }
        val raw = """
            ###LISTE
            - x
            ###ANALYSE
            y
            ###ADDITIFS_RISQUE
            $lines
        """.trimIndent()

        val t0 = System.nanoTime()
        val outcome = AdditiveKpiParser.parse(raw)
        val ms = (System.nanoTime() - t0) / 1_000_000.0

        assertEquals(80, outcome.items.size)
        assertTrue("parsing took ${ms}ms", ms < 500.0)
    }
}
