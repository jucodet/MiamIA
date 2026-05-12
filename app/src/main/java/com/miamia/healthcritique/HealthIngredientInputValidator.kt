package com.miamia.healthcritique

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
    /**
     * Valide le segment **validé** issu du scan (FR-001, contrat 002).
     * - [null] → pas de segment disponible (`NO_VALIDATED_SEGMENT`).
     * - Chaîne blanche après trim → liste vide (`EMPTY`).
     */
    fun validate(validatedSegment: String?): HealthIngredientValidation {
        if (validatedSegment == null) {
            return HealthIngredientValidation.Invalid(
                reason = InputInvalidReason.NO_VALIDATED_SEGMENT,
                message = "Aucun segment validé issu du scan. Effectuez un scan jusqu’au bilan avec segment confirmé, puis revenez sur cet onglet.",
            )
        }
        val trimmed = validatedSegment.trim()
        if (trimmed.isEmpty()) {
            return HealthIngredientValidation.Invalid(
                reason = InputInvalidReason.EMPTY,
                message = "La liste d’ingrédients capturée est vide.",
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
