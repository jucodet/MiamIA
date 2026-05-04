package com.foodgpt.healthcritique

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthIngredientInputValidatorTest {

    private val validator = HealthIngredientInputValidator()

    @Test
    fun empty_thenInvalid() {
        val r = validator.validate("   ")
        assertTrue(r is HealthIngredientValidation.Invalid)
        val inv = r as HealthIngredientValidation.Invalid
        assertEquals(InputInvalidReason.EMPTY, inv.reason)
    }

    @Test
    fun tooShort_thenInvalid() {
        val r = validator.validate("abc")
        assertTrue(r is HealthIngredientValidation.Invalid)
        val inv = r as HealthIngredientValidation.Invalid
        assertEquals(InputInvalidReason.TOO_SHORT, inv.reason)
    }

    @Test
    fun longEnough_thenValid() {
        val r = validator.validate("eau, sucre, sel")
        assertEquals(HealthIngredientValidation.Valid, r)
    }
}
