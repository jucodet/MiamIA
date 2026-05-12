package com.miamia.camera

import java.time.Instant

data class CaptureAction(
    val actionId: String,
    val sessionId: String,
    val triggeredAt: Instant,
    val sourceControl: String,
    val status: String,
    val ignoreReason: String? = null
) {
    companion object {
        const val SOURCE_CAPTURE_BUTTON = "capture_button"
    }
}
