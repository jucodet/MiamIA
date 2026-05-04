package com.foodgpt.healthcritique

sealed class HealthIngredientValidation {
    data object Valid : HealthIngredientValidation()

    data class Invalid(
        val reason: InputInvalidReason,
        val message: String,
    ) : HealthIngredientValidation()
}

class HealthIngredientInputValidator(
    private val minLength: Int = HealthCritiqueConfig.MIN_INGREDIENT_TEXT_LENGTH,
) {
    fun validate(raw: String): HealthIngredientValidation {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return HealthIngredientValidation.Invalid(
                reason = InputInvalidReason.EMPTY,
                message = "Saisissez une liste d’ingrédients (texte non vide).",
            )
        }
        if (trimmed.length < minLength) {
            return HealthIngredientValidation.Invalid(
                reason = InputInvalidReason.TOO_SHORT,
                message = "Liste trop courte : ajoutez au moins $minLength caractères pour lancer l’analyse.",
            )
        }
        return HealthIngredientValidation.Valid
    }
}
