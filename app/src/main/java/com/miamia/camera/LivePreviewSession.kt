package com.miamia.camera

import java.time.Instant

data class LivePreviewSession(
    val sessionId: String,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val permissionState: String,
    val previewState: String,
    val failureReason: String? = null
)
