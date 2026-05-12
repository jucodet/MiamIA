package com.miamia.ingredients

import com.miamia.recognition.ValidateIngredientListCommand
import com.miamia.recognition.ValidateIngredientListResult

class IngredientValidationUseCase {
    fun validate(command: ValidateIngredientListCommand): ValidateIngredientListResult {
        val errors = mutableListOf<String>()
        if (command.finalItems.isEmpty()) errors += "La liste ne peut pas être vide"
        if (command.finalItems.any { it.isBlank() }) errors += "Un ingrédient vide est interdit"
        return ValidateIngredientListResult(
            scanId = command.scanId,
            accepted = errors.isEmpty(),
            validatedAtEpochMs = System.currentTimeMillis(),
            errors = errors
        )
    }
}
