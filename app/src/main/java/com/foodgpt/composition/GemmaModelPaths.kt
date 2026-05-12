package com.foodgpt.composition

/**
 * Convention d’emplacement du modèle Gemma pour la feature 009.
 * Voir `app/src/main/assets/gemma/README.md` et `quickstart.md`.
 */
object GemmaModelPaths {
    /** Dossier assets relatif (sans slash final). */
    const val ASSET_DIRECTORY = "gemma"

    /**
     * Modèle au format LiteRT-LM (`.litertlm`), attendu dans `assets/gemma/`.
     * Voir https://huggingface.co/litert-community
     */
    const val EXPECTED_MODEL_FILENAME = "gemma_model.litertlm"

    const val PROMPT_VERSION = "v2"

    fun expectedAssetPath(): String = "$ASSET_DIRECTORY/$EXPECTED_MODEL_FILENAME"
}
