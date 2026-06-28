package com.miamia.gemma4local.model

import java.time.Instant

enum class AnalyseTextuelleStatus {
    SUCCESS,
    FAILED
}

enum class AnalyseTextuelleErrorType {
    API_UNAVAILABLE,
    NETWORK_LOCAL,
    INVALID_RESPONSE,
    TIMEOUT,
    UNKNOWN
}

data class AnalyseTextuelleResult(
    val requestId: String,
    val status: AnalyseTextuelleStatus,
    val outputText: String? = null,
    val errorType: AnalyseTextuelleErrorType? = null,
    val userMessage: String = "",
    val backend: BackendExecution = BackendExecution.INDETERMINATE,
    val completedAt: Instant = Instant.now()
)
