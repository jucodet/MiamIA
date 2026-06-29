package com.miamia.healthcritique

/**
 * Messages utilisateur spécifiques au flux critique santé (Feature Q — IHI-Q-FR-005).
 * Distincts de [com.miamia.composition.CompositionMessages] pour éviter la confusion
 * « analyse composition » lors d'un timeout critique.
 */
object HealthCritiqueMessages {
    const val GEMMA_TIMEOUT_USER =
        "La critique santé n'a pas pu être générée à temps. Réessayez."

    const val GEMMA_LOAD_FAILED_USER =
        "Impossible d'exécuter la critique santé localement. Vérifiez que le modèle est disponible."

    const val GEMMA_NOT_FOUND_USER =
        "Gemma introuvable sur cet appareil. Ajoutez le fichier modèle dans assets/gemma/ (voir README)."
}
