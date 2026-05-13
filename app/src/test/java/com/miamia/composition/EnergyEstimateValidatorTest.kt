package com.miamia.composition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnergyEstimateValidatorTest {

    @Test
    fun parseKcalFromEnergyBlock_plainInteger() {
        assertEquals(420, EnergyEstimateValidator.parseKcalFromEnergyBlock("420"))
    }

    @Test
    fun parseKcalFromEnergyBlock_labelled() {
        assertEquals(
            380,
            EnergyEstimateValidator.parseKcalFromEnergyBlock("kcal_pour_100g: 380"),
        )
    }

    @Test
    fun parseKcalFromEnergyBlock_na_returnsNull() {
        assertNull(EnergyEstimateValidator.parseKcalFromEnergyBlock("NA"))
    }

    @Test
    fun parseKcalFromEnergyBlock_outOfRange_returnsNull() {
        assertNull(EnergyEstimateValidator.parseKcalFromEnergyBlock("0"))
        assertNull(EnergyEstimateValidator.parseKcalFromEnergyBlock("1101"))
    }

    @Test
    fun parseKcalFromEnergyBlock_highDensity_accepted() {
        assertEquals(951, EnergyEstimateValidator.parseKcalFromEnergyBlock("951"))
        assertEquals(1000, EnergyEstimateValidator.parseKcalFromEnergyBlock("1000"))
        assertEquals(1100, EnergyEstimateValidator.parseKcalFromEnergyBlock("1100"))
    }

    @Test
    fun sanitizeBilan_clearsInvalidKcal() {
        val bilan = CompositionBilan(
            ingredientLines = listOf("eau"),
            compositionAnalysis = "x",
            disclaimer = "d",
            estimatedKcalPer100g = 12_000,
        )
        val out = EnergyEstimateValidator.sanitizeBilan(bilan)
        assertNull(out.estimatedKcalPer100g)
    }
}
