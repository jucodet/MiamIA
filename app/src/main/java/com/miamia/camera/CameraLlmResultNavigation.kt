package com.miamia.camera

/**
 * Signal de navigation vers l'écran de résultats LLM.
 * Les données réelles (streaming/bilan) sont dans [CameraViewModel.streamingBilan].
 */
data class CameraLlmResultNavigation(
    val body: String,
    val isError: Boolean,
    val errorCategoryWire: String? = null
)
