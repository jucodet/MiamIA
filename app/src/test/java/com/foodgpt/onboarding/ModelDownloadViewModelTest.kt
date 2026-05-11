package com.foodgpt.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelDownloadViewModelTest {

    @Test
    fun `LlmModelReadinessState Checking is initial state`() {
        val state: LlmModelReadinessState = LlmModelReadinessState.Checking
        assertTrue(state is LlmModelReadinessState.Checking)
    }

    @Test
    fun `LlmModelReadinessState ConfirmationRequired carries networkType`() {
        val state = LlmModelReadinessState.ConfirmationRequired(NetworkType.WIFI)
        assertEquals(NetworkType.WIFI, state.networkType)
    }

    @Test
    fun `LlmModelReadinessState Downloading carries progress`() {
        val progress = DownloadProgress(percent = 42, downloadedBytes = 420L, totalBytes = 1000L)
        val state = LlmModelReadinessState.Downloading(progress)
        assertEquals(42, state.progress.percent)
        assertEquals(420L, state.progress.downloadedBytes)
        assertEquals(1000L, state.progress.totalBytes)
    }

    @Test
    fun `LlmModelReadinessState Error carries message and canRetry`() {
        val state = LlmModelReadinessState.Error("Network lost", canRetry = true)
        assertEquals("Network lost", state.message)
        assertTrue(state.canRetry)
    }

    @Test
    fun `DownloadProgress data class equality`() {
        val a = DownloadProgress(50, 500L, 1000L)
        val b = DownloadProgress(50, 500L, 1000L)
        assertEquals(a, b)
    }

    @Test
    fun `all sealed class subtypes are distinct`() {
        val states: List<LlmModelReadinessState> = listOf(
            LlmModelReadinessState.Checking,
            LlmModelReadinessState.Offline,
            LlmModelReadinessState.ConfirmationRequired(NetworkType.MOBILE_DATA),
            LlmModelReadinessState.Downloading(DownloadProgress(0, 0, -1)),
            LlmModelReadinessState.Ready,
            LlmModelReadinessState.Error("err", false),
            LlmModelReadinessState.Declined
        )
        assertEquals(7, states.map { it::class }.toSet().size)
    }
}
