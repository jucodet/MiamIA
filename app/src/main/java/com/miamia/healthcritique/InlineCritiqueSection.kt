package com.miamia.healthcritique

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.miamia.ui.theme.MiamIAColors

/**
 * Section de critique santé rendue **inline** sur l'écran principal des résultats
 * (`LlmResultScreen`) — Feature O ; widget autoportant Feature Q.
 */
@Composable
fun InlineCritiqueSection(
    viewModel: HealthCritiqueViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.ui.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("inline_critique_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Critique santé",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("health_result_title"),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                state.result != null -> {
                    when (val r = state.result) {
                        is HealthCritiqueResult.InputInvalid -> Text(
                            text = r.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.testTag("health_result_error"),
                        )

                        is HealthCritiqueResult.InferenceError -> Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = r.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.testTag("health_result_error"),
                            )
                            OutlinedButton(
                                onClick = { viewModel.analyze() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("health_result_retry"),
                            ) {
                                Text("Réessayer")
                            }
                        }

                        is HealthCritiqueResult.CritiqueReady -> CritiqueProfileContent(result = r)

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
                            text = "Analyse critique en cours…",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CritiqueProfileContent(
    result: HealthCritiqueResult.CritiqueReady,
) {
    val critique = result.profileCritique
    var showDetails by remember { mutableStateOf(false) }

    PersonaRiskSnapshotCard(
        evaluatedForHeader = critique.evaluatedForHeader,
        isDefaultProfile = result.isDefaultProfile,
        prudenceLevel = critique.prudenceLevel,
        justification = critique.prudenceJustification,
        riskCards = critique.riskCards,
        fullIngredientList = critique.fullIngredientList,
    )

    OutlinedButton(
        onClick = { showDetails = !showDetails },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("health_result_toggle_details"),
    ) {
        Text(if (showDetails) "Masquer le détail" else "Voir le détail")
    }

    AnimatedVisibility(visible = showDetails) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (critique.warnings.isNotEmpty()) {
                Text(
                    text = "Avertissements de structure",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                )
                critique.warnings.forEach { w ->
                    Text(
                        text = "• $w",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

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

            FullIngredientListToggle(entries = critique.fullIngredientList)
        }
    }

    Text(
        text = result.disclaimer,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Carte visuelle autoportante — persona + jauge 3 paliers + pastilles risque (Feature Q).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonaRiskSnapshotCard(
    evaluatedForHeader: String,
    isDefaultProfile: Boolean,
    prudenceLevel: PrudenceLevel?,
    justification: String?,
    riskCards: List<IngredientRiskCard>,
    fullIngredientList: List<FullIngredientStatutEntry>,
) {
    val (accentColor, levelLabel, levelEmoji) = prudenceVisual(prudenceLevel)
    val statutByNom = remember(fullIngredientList) {
        fullIngredientList.associateBy { it.nom.lowercase().trim() }
    }
    val shortJustification = justification?.take(120)?.let { text ->
        if ((justification?.length ?: 0) > 120) "$text…" else text
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("health_result_snapshot"),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f),
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, accentColor.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = evaluatedForHeader,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("health_result_evaluated_for"),
            )
            if (isDefaultProfile) {
                Text(
                    text = "Profil par défaut — personnalisez dans les paramètres.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.testTag("health_result_default_profile"),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("health_result_prudence"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("health_result_prudence_badge"),
                ) {
                    Text(
                        text = "$levelEmoji  $levelLabel",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
                PrudenceSegmentBar(activeLevel = prudenceLevel, accentColor = accentColor)
            }

            if (!shortJustification.isNullOrBlank()) {
                Text(
                    text = shortJustification,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("health_result_prudence_text"),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("health_result_risk_highlights"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Risques pour votre profil",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                if (riskCards.isEmpty()) {
                    RiskPill(
                        label = "Aucun risque marqué",
                        color = MiamIAColors.ImpactNeutral,
                        emoji = "\u2705",
                        testTagSuffix = "none",
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        riskCards.forEach { card ->
                            val statut = statutByNom[card.nom.lowercase().trim()]?.statut
                            val (color, emoji) = riskVisual(statut)
                            val label = buildString {
                                append(card.nom)
                                if (!card.code.isNullOrBlank()) append(" (${card.code})")
                            }
                            RiskPill(
                                label = label,
                                color = color,
                                emoji = emoji,
                                testTagSuffix = card.nom.lowercase().replace(' ', '_'),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrudenceSegmentBar(
    activeLevel: PrudenceLevel?,
    accentColor: Color,
) {
    val levels = listOf(
        PrudenceLevel.FAIBLE to Color(0xFF2E7D32),
        PrudenceLevel.MODERE to Color(0xFFF9A825),
        PrudenceLevel.ELEVE to Color(0xFFC62828),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .testTag("health_result_prudence_label"),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        levels.forEach { (level, color) ->
            val isActive = level == activeLevel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .background(
                        if (isActive) accentColor else color.copy(alpha = 0.25f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isActive) {
                    Text(
                        text = "▶",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun prudenceVisual(level: PrudenceLevel?): Triple<Color, String, String> = when (level) {
    PrudenceLevel.FAIBLE -> Triple(Color(0xFF2E7D32), "Faible", "\uD83D\uDFE2")
    PrudenceLevel.MODERE -> Triple(Color(0xFFF9A825), "Modéré", "\uD83D\uDFE1")
    PrudenceLevel.ELEVE -> Triple(Color(0xFFC62828), "Élevé", "\uD83D\uDD34")
    null -> Triple(Color(0xFF757575), "—", "\u2753")
}

@Composable
private fun RiskPill(
    label: String,
    color: Color,
    emoji: String,
    testTagSuffix: String,
) {
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("health_result_risk_pill_$testTagSuffix"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = emoji, style = MaterialTheme.typography.labelMedium)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MiamIAColors.OnSurfaceVariant,
            )
        }
    }
}

private fun riskVisual(statut: IngredientVigilanceStatut?): Pair<Color, String> = when (statut) {
    IngredientVigilanceStatut.ELEVE -> MiamIAColors.ImpactRed to "\u274C"
    IngredientVigilanceStatut.MODERE -> MiamIAColors.ImpactOrange to "\u26A0\uFE0F"
    else -> MiamIAColors.ImpactOrange to "\u26A0\uFE0F"
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
            } else if (card.impact.isNotBlank()) {
                Text(
                    text = card.impact,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Touchez pour le détail",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            Text(if (expanded) "Masquer les ingrédients à vigilance" else "Voir les ingrédients à vigilance")
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
                    Text(text = "(liste indisponible)", style = MaterialTheme.typography.bodySmall)
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
