package com.miamia.gemma4local.model

import java.time.Instant
import java.util.UUID

data class AnalyseTextuelleRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val inputText: String,
    val requestedAt: Instant = Instant.now(),
    val sourceScreen: String? = null
)
