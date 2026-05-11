package com.foodgpt.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.foodgpt.camera.BilanResultCard
import com.foodgpt.composition.CompositionBilan

@Composable
fun LlmResultScreen(
    body: String,
    isError: Boolean,
    errorCategoryWire: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    bilan: CompositionBilan? = null,
    rawTranscript: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isError) {
            Text(
                text = "Analyse — erreur",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag("llm_result_title")
            )
            if (errorCategoryWire != null) {
                Text(
                    text = errorCategoryWire,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("llm_result_error_category")
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .testTag("llm_result_body")
            )
        } else if (bilan != null) {
            var showRaw by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BilanResultCard(
                    bilan = bilan,
                    rawTranscript = rawTranscript ?: body,
                    additiveKpi = null,
                    showRaw = showRaw,
                    onToggleRaw = { showRaw = !showRaw }
                )
            }
        } else {
            Text(
                text = "Résultat analyse",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag("llm_result_title")
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .testTag("llm_result_body")
            )
        }
        Spacer(modifier = Modifier.weight(0.01f))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("llm_result_back")
        ) {
            Text("Retour")
        }
    }
}
