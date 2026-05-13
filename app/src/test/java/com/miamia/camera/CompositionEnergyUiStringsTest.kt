package com.miamia.camera

import org.junit.Assert.assertTrue
import org.junit.Test

class CompositionEnergyUiStringsTest {

    @Test
    fun primaryLine_containsEstimeAndIndicatif() {
        val line = CompositionEnergyUiStrings.primaryLine(400)
        assertTrue(line.contains("estim", ignoreCase = true))
        assertTrue(line.contains("indicatif", ignoreCase = true))
    }

    @Test
    fun helper_mentionsNonReglementaire() {
        assertTrue(
            CompositionEnergyUiStrings.HELPER.contains("réglementaire", ignoreCase = true) ||
                CompositionEnergyUiStrings.HELPER.contains("reglementaire", ignoreCase = true),
        )
    }
}
