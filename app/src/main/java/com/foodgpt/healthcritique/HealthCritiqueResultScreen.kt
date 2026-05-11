package com.foodgpt.healthcritique

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun HealthCritiqueResultScreen(
    viewModel: HealthCritiqueViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.ui.collectAsState()
    val streamingText by viewModel.streamingText.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(streamingText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Critique santé",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("health_result_title"),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                state.result != null -> {
                    when (val r = state.result) {
                        is HealthCritiqueResult.InputInvalid -> {
                            Text(
                                text = r.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.testTag("health_result_error"),
                            )
                        }

                        is HealthCritiqueResult.InferenceError -> {
                            Text(
                                text = r.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.testTag("health_result_error"),
                            )
                        }

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
                            Spacer(modifier = Modifier.height(8.dp))
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
                                        .testTag("health_result_copy"),
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
                                        .testTag("health_result_copy_prompt"),
                                ) {
                                    Text("Copier le prompt")
                                }
                            }
                        }

                        null -> Unit
                    }
                }

                state.isLoading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.testTag("health_result_loading"),
                        )
                        Text(
                            text = "Analyse en cours…",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    AnimatedVisibility(
                        visible = streamingText.isNotBlank(),
                        enter = fadeIn(),
                    ) {
                        Text(
                            text = streamingText,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("health_result_streaming_text"),
                        )
                    }
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("health_result_back"),
        ) {
            Text("Retour")
        }
    }
}
