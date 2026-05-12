package com.miamia.gemma4local.model

import java.time.Instant

enum class DeviceClass {
    RECENT,
    MIN_COMPAT
}

data class ApiCallMetric(
    val requestId: String,
    val outcome: AnalyseTextuelleStatus,
    val latencyMs: Long,
    val errorType: AnalyseTextuelleErrorType? = null,
    val deviceClass: DeviceClass,
    val recordedAt: Instant = Instant.now()
)
