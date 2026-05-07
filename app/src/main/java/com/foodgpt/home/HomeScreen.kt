package com.foodgpt.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Test pipeline LLM local",
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = viewModel::onRunMockClicked,
            enabled = state.canRun,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_run_llm_mock_button")
        ) {
            Text("Lancer le test LLM")
        }

        val statusText = when (state.runState) {
            HomeLlmRunState.IDLE -> "Pret"
            HomeLlmRunState.RUNNING -> "Execution en cours..."
            HomeLlmRunState.SUCCESS -> "Succes"
            HomeLlmRunState.FAILURE -> "Echec"
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("home_llm_mock_status")
        )

        if (state.runState == HomeLlmRunState.SUCCESS) {
            Text(
                text = state.responseText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_llm_response_text")
            )
        }

        if (state.runState == HomeLlmRunState.FAILURE) {
            Text(
                text = "Categorie: ${state.errorCategory?.wireValue ?: "inconnue"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("home_llm_error_category")
            )
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("home_llm_error_message")
            )
        }
    }
}

