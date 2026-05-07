package com.foodgpt.camera

import java.util.concurrent.atomic.AtomicReference

data class CameraLlmResultNavigation(
    val body: String,
    val isError: Boolean,
    val errorCategoryWire: String? = null
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
