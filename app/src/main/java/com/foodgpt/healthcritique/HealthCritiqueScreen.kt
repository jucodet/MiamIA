package com.foodgpt.healthcritique

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.util.Date

@Composable
fun HealthCritiqueScreen(viewModel: HealthCritiqueViewModel) {
    val state by viewModel.ui.collectAsState()
    val context = LocalContext.current
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Critique santé (liste d’ingrédients)",
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = state.ingredientText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("health_segment_list"),
            label = { Text("Liste d’ingrédients (scan, lecture seule)") },
            minLines = 4,
        )
        Button(
            onClick = { viewModel.analyze() },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Analyser")
        }
        if (state.isLoading) {
            CircularProgressIndicator()
        }
        state.restoredSnapshot?.let { snap ->
            val df = DateFormat.getDateFormat(context)
            val tf = DateFormat.getTimeFormat(context)
            val label = "${df.format(Date(snap.savedAtEpochMs))} ${tf.format(Date(snap.savedAtEpochMs))}"
            Text(
                text = "Dernière analyse enregistrée : $label",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        when (val r = state.result) {
            is HealthCritiqueResult.InputInvalid ->
                Text(
                    text = r.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )

            is HealthCritiqueResult.InferenceError ->
                Text(
                    text = r.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )

            is HealthCritiqueResult.CritiqueReady -> {
                Text(
                    text = r.disclaimer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (r.parseWarnings.isNotEmpty()) {
                    Text(
                        text = "Avertissements de structure",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    r.parseWarnings.forEach { w ->
                        Text(
                            text = "• $w",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                r.sections.forEach { (key, body) ->
                    Text(
                        text = key.name.replace('_', ' '),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        text = body.ifBlank { "(section vide)" },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            HealthCritiqueClipboard.copyPlainText(
                                context,
                                "critique_sante",
                                r.llmRawText,
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("health_copy_result"),
                    ) {
                        Text("Copier la réponse")
                    }
                    OutlinedButton(
                        onClick = {
                            HealthCritiqueClipboard.copyPlainText(
                                context,
                                "prompt_critique",
                                state.lastSystemPrompt,
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("health_copy_prompt"),
                    ) {
                        Text("Copier le prompt")
                    }
                }
            }

            null -> Unit
        }
    }
}
