package com.miamia.healthcritique

object HealthCritiqueConfig {
    /** Seuil minimal (caractères après trim) — FR-005, aligné `research.md`. */
    const val MIN_INGREDIENT_TEXT_LENGTH: Int = 10

    const val MAX_INGREDIENT_TEXT_CHARS: Int = 12_000

    /** Délai d’inférence par défaut (ms) — objectif produit inférieur à 30 s perçus (`plan.md`). */
    const val DEFAULT_MAX_INFERENCE_MS: Long = 30_000L
}
