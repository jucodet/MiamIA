package com.foodgpt.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenLlmMockUiTest {

    @Test
    fun successStateProvidesDisplayableResponse() {
        val state = HomeUiState(
            runState = HomeLlmRunState.SUCCESS,
            responseText = "###LISTE\n- sucre"
        )

        assertEquals(HomeLlmRunState.SUCCESS, state.runState)
        assertTrue(state.responseText.contains("###LISTE"))
        assertTrue(state.canRun)
    }

    @Test
    fun failureStateExposesCategoryAndMessage() {
        val state = HomeUiState(
            runState = HomeLlmRunState.FAILURE,
            errorCategory = HomeLlmFailureCategory.RUNTIME_UNAVAILABLE,
            errorMessage = "Moteur indisponible"
        )

        assertEquals(HomeLlmFailureCategory.RUNTIME_UNAVAILABLE, state.errorCategory)
        assertEquals("Moteur indisponible", state.errorMessage)
        assertTrue(state.canRun)
    }
}

