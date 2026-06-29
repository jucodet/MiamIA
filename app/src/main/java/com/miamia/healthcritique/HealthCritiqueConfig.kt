package com.miamia.healthcritique

object HealthCritiqueConfig {
    /** Seuil minimal (caractères après trim) — FR-005, aligné `research.md`. */
    const val MIN_INGREDIENT_TEXT_LENGTH: Int = 10

    const val MAX_INGREDIENT_TEXT_CHARS: Int = 12_000

    /** Seuil « liste très longue » en nombre d'ingrédients — IHI-L-FR-012 (clarify Q4, 2026-06-28). */
    const val LONG_LIST_INGREDIENT_THRESHOLD: Int = 20

    /** Délai d'inférence par défaut (ms) — aligné `Gemma4LocalConfig.DEFAULT_TIMEOUT_MS` (Feature Q). */
    const val DEFAULT_MAX_INFERENCE_MS: Long = 180_000L
}
