package com.miamia.composition

object CompositionMessages {
    const val GEMMA_NOT_FOUND_USER =
        "Gemma introuvable sur cet appareil. Ajoutez le fichier modèle dans assets/gemma/ (voir README)."

    const val GEMMA_LOAD_FAILED_USER =
        "Impossible d'exécuter Gemma localement. Vérifiez que le modèle est compatible et que l'appareil dispose de suffisamment de mémoire."

    const val GEMMA_TIMEOUT_USER =
        "L'analyse composition a dépassé le délai autorisé. Réessayez ou raccourcissez le texte."

    const val DISCLAIMER_DEFAULT =
        "Information indicative sur la composition : cela ne constitue pas un avis médical ni une recommandation de consommation."

    const val COMPOSITION_LIMIT_GENERIC =
        "Impossible d'extraire une liste d'ingrédients fiable à partir du texte capturé."

    const val MODEL_PRESENT_INFERENCE_NOT_READY =
        "Le fichier modèle est présent mais l'inférence Gemma complète n'est pas encore activée dans cette version de l'application."
}
