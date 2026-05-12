package com.miamia.additives

import com.miamia.composition.CompositionBilan
import org.junit.Assert.assertTrue
import org.junit.Test

class US1AdditiveEmptyStateContractTest {

    @Test
    fun givenNoAdditivesSection_whenBuildDisplay_thenEmptyStateAndWarning() {
        val bilan: CompositionBilan = AdditiveKpiFixtures.minimalBilan()
        val display = BuildAdditiveKpiDisplay(bilan, AdditiveKpiFixtures.rawEmptyAdditives())

        assertTrue(display.isEmptyState)
        assertTrue(display.itemsOrdered.isEmpty())
        assertTrue(display.parseErrors.isNotEmpty())
    }
}
