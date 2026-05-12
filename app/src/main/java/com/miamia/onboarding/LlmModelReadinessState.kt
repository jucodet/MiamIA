package com.miamia.onboarding

data class DownloadProgress(
    val percent: Int,
    val downloadedBytes: Long,
    val totalBytes: Long
)

sealed class LlmModelReadinessState {
    data object Checking : LlmModelReadinessState()
    data object Offline : LlmModelReadinessState()
    data class ConfirmationRequired(val networkType: NetworkType) : LlmModelReadinessState()
    data class Downloading(val progress: DownloadProgress) : LlmModelReadinessState()
    data object Ready : LlmModelReadinessState()
    data class Error(val message: String, val canRetry: Boolean) : LlmModelReadinessState()
    data object Declined : LlmModelReadinessState()
}
