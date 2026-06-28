package com.miamia.healthcritique

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
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

                            is HealthCritiqueResult.CritiqueReady -> CritiqueProfileContent(
                                result = r,
                                state = state,
                            )

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
                            enter = androidx.compose.animation.fadeIn(),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        val r = state.result as? HealthCritiqueResult.CritiqueReady
                        val payload = r?.llmRawText ?: ""
                        HealthCritiqueClipboard.copyPlainText(context, "critique_sante", payload)
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
}

@Composable
private fun CritiqueProfileContent(
    result: HealthCritiqueResult.CritiqueReady,
    state: HealthCritiqueScreenState,
) {
    val critique = result.profileCritique

    Text(
        text = critique.evaluatedForHeader,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.testTag("health_result_evaluated_for"),
    )
    if (result.isDefaultProfile) {
        Text(
            text = "Profil par défaut — personnalisez votre profil dans les paramètres.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.testTag("health_result_default_profile"),
        )
    }

    Text(
        text = result.disclaimer,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    if (critique.parseWarnings.isNotEmpty()) {
        Text(
            text = "Avertissements de structure",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
        )
        critique.parseWarnings.forEach { w ->
            Text(
                text = "• $w",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    PrudenceGauge(level = critique.prudenceLevel, justification = critique.prudenceJustification)

    if (critique.riskCards.isEmpty()) {
        Text(
            text = "Aucun ingrédient à vigilance (Modérée/Élevée) pour votre profil.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("health_result_no_cards"),
        )
    } else {
        critique.riskCards.forEach { card ->
            IngredientRiskCardItem(card = card)
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    FullIngredientListToggle(entries = critique.fullIngredientList)
}

@Composable
private fun PrudenceGauge(
    level: PrudenceLevel?,
    justification: String?,
) {
    val (progress, color, label) = when (level) {
        PrudenceLevel.FAIBLE -> Triple(0.16f, Color(0xFF2E7D32), "Faible")
        PrudenceLevel.MODERE -> Triple(0.5f, Color(0xFFF9A825), "Modéré")
        PrudenceLevel.ELEVE -> Triple(1f, Color(0xFFC62828), "Élevé")
        null -> Triple(0f, MaterialTheme.colorScheme.outline, "—")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("health_result_prudence"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Niveau de prudence",
            style = MaterialTheme.typography.titleSmall,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = listOf("Faible", "Modéré", "Élevé").joinToString(" | ") { base ->
                    if (base.equals(label, ignoreCase = true)) "▶ $base" else base
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("health_result_prudence_label"),
            )
        }
        LinearProgressIndicator(
            progress = progress,
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )
        if (!justification.isNullOrBlank()) {
            Text(
                text = justification,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("health_result_prudence_text"),
            )
        }
    }
}

@Composable
private fun IngredientRiskCardItem(card: IngredientRiskCard) {
    var expanded by remember { mutableStateOf(false) }
    val tag = "health_result_card_${card.nom.lowercase().replace(' ', '_')}"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        colors = CardDefaults.cardColors(),
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .clickable { expanded = !expanded },
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val title = buildString {
                append(card.nom)
                if (!card.code.isNullOrBlank()) append(" (${card.code})")
            }
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (card.type.isNotBlank()) {
                Text(text = card.type, style = MaterialTheme.typography.labelMedium)
            }
            if (expanded) {
                FieldLine("Impact", card.impact)
                FieldLine("Fait établi", card.faitEtabli)
                FieldLine("Nuance", card.nuance)
                FieldLine("Cible particulièrement", card.cibleParticulierement)
            } else {
                Text(
                    text = "Touchez pour le détail",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FieldLine(label: String, value: String) {
    if (value.isBlank()) return
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FullIngredientListToggle(entries: List<FullIngredientStatutEntry>) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("health_result_show_all"),
        ) {
            Text(if (expanded) "Masquer tous les ingrédients analysés" else "Voir tous les ingrédients analysés")
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("health_result_full_list"),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (entries.isEmpty()) {
                    Text(text = "(liste complète indisponible)", style = MaterialTheme.typography.bodySmall)
                }
                entries.forEach { e ->
                    Text(
                        text = "${e.nom} : ${e.statut.label}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
