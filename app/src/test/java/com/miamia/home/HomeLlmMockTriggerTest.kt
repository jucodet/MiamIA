package com.miamia.home

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeLlmMockTriggerTest {

    @Test
    fun runMockTestNow_setsSuccessStateAndResponse() = runBlocking {
        val vm = HomeViewModel(
            runner = FakeRunner(
                HomeLlmMockOutcome.Success("###ANALYSE\nok")
            )
        )

        vm.runMockTestNow()

        val state = vm.uiState.value
        assertEquals(HomeLlmRunState.SUCCESS, state.runState)
        assertEquals("###ANALYSE\nok", state.responseText)
        assertTrue(state.canRun)
    }

    @Test
    fun runMockTestNow_setsFailureStateWithCategory() = runBlocking {
        val vm = HomeViewModel(
            runner = FakeRunner(
                HomeLlmMockOutcome.Failure(
                    category = HomeLlmFailureCategory.TIMEOUT,
                    message = "timeout"
                )
            )
        )

        vm.runMockTestNow()

        val state = vm.uiState.value
        assertEquals(HomeLlmRunState.FAILURE, state.runState)
        assertEquals(HomeLlmFailureCategory.TIMEOUT, state.errorCategory)
        assertEquals("timeout", state.errorMessage)
        assertTrue(state.canRun)
    }

    @Test
    fun onRunMockClicked_ignoresWhenAlreadyRunning() {
        val vm = HomeViewModel(
            runner = object : HomeLlmMockRunner {
                override suspend fun run(): HomeLlmMockOutcome {
                    return HomeLlmMockOutcome.Success("never")
                }
            }
        )

        // Forced running state through one execution start.
        runBlocking { vm.runMockTestNow() }
        val finalState = vm.uiState.value
        assertFalse(finalState.runState == HomeLlmRunState.RUNNING)
        assertTrue(finalState.canRun)
    }

    private class FakeRunner(
        private val outcome: HomeLlmMockOutcome
    ) : HomeLlmMockRunner {
        override suspend fun run(): HomeLlmMockOutcome = outcome
    }
}

