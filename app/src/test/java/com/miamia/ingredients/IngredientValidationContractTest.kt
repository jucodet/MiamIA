package com.miamia.ingredients

import com.miamia.recognition.ValidateIngredientListCommand
import org.junit.Assert.assertFalse
import org.junit.Test

class IngredientValidationContractTest {
    @Test
    fun empty_list_is_rejected() {
        val result = IngredientValidationUseCase().validate(
            ValidateIngredientListCommand("scan-1", emptyList(), editedByUser = true)
        )
        assertFalse(result.accepted)
    }
}
