package com.miamia.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeLlmRunState {
    IDLE,
    RUNNING,
    SUCCESS,
    FAILURE
}

enum class HomeLlmFailureCategory(val wireValue: String) {
    TIMEOUT("timeout"),
    RUNTIME_UNAVAILABLE("runtime-unavailable"),
    NON_ANALYSABLE_RESPONSE("non-analysable-response")
}

sealed class HomeLlmMockOutcome {
    data class Success(val responseText: String) : HomeLlmMockOutcome()
    data class Failure(
        val category: HomeLlmFailureCategory,
        val message: String
    ) : HomeLlmMockOutcome()
}

data class HomeUiState(
    val runState: HomeLlmRunState = HomeLlmRunState.IDLE,
    val responseText: String = "",
    val errorCategory: HomeLlmFailureCategory? = null,
    val errorMessage: String = ""
) {
    val canRun: Boolean = runState != HomeLlmRunState.RUNNING
}

class HomeViewModel(
    private val runner: HomeLlmMockRunner
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onRunMockClicked() {
        if (!_uiState.value.canRun) return
        viewModelScope.launch { runMockTestNow() }
    }

    suspend fun runMockTestNow() {
        _uiState.update {
            it.copy(
                runState = HomeLlmRunState.RUNNING,
                responseText = "",
                errorCategory = null,
                errorMessage = ""
            )
        }
        when (val outcome = runner.run()) {
            is HomeLlmMockOutcome.Success -> {
                _uiState.update {
                    it.copy(
                        runState = HomeLlmRunState.SUCCESS,
                        responseText = outcome.responseText,
                        errorCategory = null,
                        errorMessage = ""
                    )
                }
            }
            is HomeLlmMockOutcome.Failure -> {
                _uiState.update {
                    it.copy(
                        runState = HomeLlmRunState.FAILURE,
                        responseText = "",
                        errorCategory = outcome.category,
                        errorMessage = outcome.message
                    )
                }
            }
        }
    }

    companion object {
        fun factory(runner: HomeLlmMockRunner): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(HomeViewModel::class.java))
                    return HomeViewModel(runner) as T
                }
            }
        }
    }
}

