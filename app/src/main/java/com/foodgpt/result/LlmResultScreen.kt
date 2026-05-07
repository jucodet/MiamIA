package com.foodgpt.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun LlmResultScreen(
    body: String,
    isError: Boolean,
    errorCategoryWire: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isError) "Analyse — erreur" else "Résultat analyse",
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
