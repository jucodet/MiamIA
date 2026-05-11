package com.foodgpt.onboarding

import android.app.Application
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.foodgpt.gemma4local.GemmaModelDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class ModelDownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val downloader = GemmaModelDownloader(application)

    private val _state = MutableStateFlow<LlmModelReadinessState>(LlmModelReadinessState.Checking)
    val state: StateFlow<LlmModelReadinessState> = _state.asStateFlow()

    private val downloadInProgress = AtomicBoolean(false)

    val isResumable: Boolean
        get() {
            val target = downloader.resolveLocalModel()
            if (target != null) return false
            val downloadingFile = java.io.File(
                getApplication<Application>().filesDir,
                "gemma/gemma_model.litertlm.downloading"
            )
            return downloadingFile.exists() && downloadingFile.length() > 0L
        }

    init {
        checkModelPresence()
    }

    private fun checkModelPresence() {
        viewModelScope.launch {
            _state.value = LlmModelReadinessState.Checking
            val hasModel = withContext(Dispatchers.IO) {
                downloader.resolveLocalModel() != null
            }
            if (hasModel) {
                _state.value = LlmModelReadinessState.Ready
            } else {
                val networkType = NetworkTypeDetector.detectCurrentNetworkType(getApplication())
                _state.value = when (networkType) {
                    NetworkType.OFFLINE -> LlmModelReadinessState.Offline
                    else -> LlmModelReadinessState.ConfirmationRequired(networkType)
                }
            }
        }
    }

    fun confirmDownload() {
        startDownload()
    }

    fun declineDownload() {
        _state.value = LlmModelReadinessState.Declined
    }

    fun retryNetworkCheck() {
        checkModelPresence()
    }

    fun retryDownload() {
        startDownload()
    }

    private fun startDownload() {
        if (!downloadInProgress.compareAndSet(false, true)) return

        if (!hasEnoughDiskSpace()) {
            _state.value = LlmModelReadinessState.Error(
                message = "Espace disque insuffisant pour télécharger le modèle (~500 Mo requis).",
                canRetry = true
            )
            downloadInProgress.set(false)
            return
        }

        _state.value = LlmModelReadinessState.Downloading(
            DownloadProgress(percent = 0, downloadedBytes = 0L, totalBytes = -1L)
        )

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    downloader.downloadModelWithProgress { percent, downloaded, total ->
                        _state.value = LlmModelReadinessState.Downloading(
                            DownloadProgress(percent, downloaded, total)
                        )
                    }
                }
                _state.value = LlmModelReadinessState.Ready
            } catch (e: Exception) {
                _state.value = LlmModelReadinessState.Error(
                    message = e.message ?: "Échec du téléchargement",
                    canRetry = true
                )
            } finally {
                downloadInProgress.set(false)
            }
        }
    }

    private fun hasEnoughDiskSpace(): Boolean {
        return try {
            val stat = StatFs(getApplication<Application>().filesDir.absolutePath)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            availableBytes > REQUIRED_SPACE_BYTES
        } catch (_: Exception) {
            true
        }
    }

    companion object {
        private const val REQUIRED_SPACE_BYTES = 600L * 1024 * 1024
    }
}
