package com.foodgpt.additives

import com.foodgpt.composition.CompositionBilan
import org.junit.Assert.assertEquals
import org.junit.Test

class US3KpiSummaryMatchesListContractTest {

    @Test
    fun givenSixItems_whenBuildDisplay_thenSc003CountsMatchList() {
        val bilan: CompositionBilan = AdditiveKpiFixtures.minimalBilan()
        val display = BuildAdditiveKpiDisplay(bilan, AdditiveKpiFixtures.rawFiveForKpi())
        val s = display.summary
        val items = display.itemsOrdered

        assertEquals(6, items.size)
        assertEquals(items.size, s.totalCount)
        assertEquals(
            s.totalCount,
            s.highCount + s.mediumCount + s.lowCount + s.unknownCount,
        )
        assertEquals(2, s.highCount)
        assertEquals(1, s.mediumCount)
        assertEquals(2, s.lowCount)
        assertEquals(1, s.unknownCount)
        assertEquals(AdditiveRiskLevel.HIGH, s.globalLevel)
    }
}
