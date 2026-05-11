package com.foodgpt.camera

import com.foodgpt.composition.CompositionBilan
import java.util.concurrent.atomic.AtomicReference

data class CameraLlmResultNavigation(
    val body: String,
    val isError: Boolean,
    val errorCategoryWire: String? = null,
    val bilan: CompositionBilan? = null,
    val rawTranscript: String? = null
)

/**
 * Stocke le payload entre la navigation (évite les arguments d'URL trop longs).
 */
object CameraLlmResultPayloadStore {
    private val ref = AtomicReference<CameraLlmResultNavigation?>(null)

    fun set(payload: CameraLlmResultNavigation) {
        ref.set(payload)
    }

    fun getAndClear(): CameraLlmResultNavigation? = ref.getAndSet(null)
}
