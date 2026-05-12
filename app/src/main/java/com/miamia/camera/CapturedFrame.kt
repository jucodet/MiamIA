package com.miamia.camera

import java.time.Instant

data class CapturedFrame(
    val frameId: String,
    val actionId: String,
    val capturedAt: Instant,
    val imageUri: String,
    val matchesPreviewIntent: Boolean,
    val captureErrorCode: String? = null
)
