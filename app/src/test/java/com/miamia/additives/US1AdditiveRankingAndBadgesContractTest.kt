package com.miamia.additives

import com.miamia.composition.CompositionBilan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class US1AdditiveRankingAndBadgesContractTest {

    @Test
    fun givenMultiLevelAdditives_whenBuildDisplay_thenOrderIsHighMediumLowUnknown() {
        val bilan: CompositionBilan = AdditiveKpiFixtures.minimalBilan()
        val display = BuildAdditiveKpiDisplay(bilan, AdditiveKpiFixtures.rawMultiLevel())

        assertEquals(4, display.itemsOrdered.size)
        assertEquals(AdditiveRiskLevel.HIGH, display.itemsOrdered[0].level)
        assertEquals(AdditiveRiskLevel.MEDIUM, display.itemsOrdered[1].level)
        assertEquals(AdditiveRiskLevel.LOW, display.itemsOrdered[2].level)
        assertEquals(AdditiveRiskLevel.UNKNOWN, display.itemsOrdered[3].level)
    }

    @Test
    fun givenLines_whenBuildDisplay_thenEachItemHasDisplayableLevelAndName() {
        val bilan = AdditiveKpiFixtures.minimalBilan()
        val display = BuildAdditiveKpiDisplay(bilan, AdditiveKpiFixtures.rawMultiLevel())
        for (item in display.itemsOrdered) {
            assertTrue(item.displayName.isNotBlank())
            assertTrue(
                item.level == AdditiveRiskLevel.HIGH ||
                    item.level == AdditiveRiskLevel.MEDIUM ||
                    item.level == AdditiveRiskLevel.LOW ||
                    item.level == AdditiveRiskLevel.UNKNOWN,
            )
        }
    }
}
