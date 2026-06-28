package com.miamia.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.miamia.additives.AnalysisDisplayResult
import com.miamia.additives.ui.AdditiveKpiPanel
import com.miamia.composition.CompositionBilan
import com.miamia.composition.IngredientHealthImpact
import com.miamia.gemma4local.model.BackendExecution
import com.miamia.ui.shared.CategoryChips
import com.miamia.ui.theme.MiamIAColors

@Composable
fun BilanResultCard(
    bilan: CompositionBilan,
    rawTranscript: String,
    additiveKpi: AnalysisDisplayResult?,
    showRaw: Boolean,
    onToggleRaw: () -> Unit,
    inferenceTimeMs: Long = 0L,
    backend: BackendExecution = BackendExecution.INDETERMINATE,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BilanHeader()
        // Feature P — compte rendu en 4 sections ordonnées fixes :
        // (1) Produit identifié, (2) Synthèse, (3) Verdict par ingrédient,
        // (4) Critique santé (rendue inline par InlineCritiqueSection sur LlmResultScreen).
        // La liste brute des ingrédients identifiés est supprimée de l'UI
        // (IHI-P-FR-002) ; la Synthèse agrège la pastille kcal + les KPI additifs
        // (IHI-P-FR-003). Sections 1/3 inconditionnelles (états neutres).
        ProductSection(bilan.identifiedProduct, bilan.productConfidence)
        SyntheseSection(
            estimatedKcalPer100g = bilan.estimatedKcalPer100g,
            analysis = bilan.compositionAnalysis,
            additiveKpi = additiveKpi,
            onRequestShowRaw = onToggleRaw,
        )
        HealthImpactSection(bilan.healthImpacts)
        DisclaimerSection(bilan.disclaimer)
        if (inferenceTimeMs > 0L) {
            InferenceTimeBadge(inferenceTimeMs, backend)
        }
        RawTranscriptToggle(
            showRaw = showRaw,
            rawTranscript = rawTranscript,
            onToggle = onToggleRaw
        )
    }
}

@Composable
private fun InferenceTimeBadge(
    inferenceTimeMs: Long,
    backend: BackendExecution
) {
    val seconds = inferenceTimeMs / 1000.0
    val formatted = "%.1f".format(seconds)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackendBadge(backend)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Inférence : ${formatted}s",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("inference_time_label")
        )
    }
}

private data class BackendBadgeStyle(
    val icon: ImageVector,
    val color: Color,
)

private fun backendBadgeStyle(backend: BackendExecution): BackendBadgeStyle = when (backend) {
    BackendExecution.NPU -> BackendBadgeStyle(Icons.Filled.Memory, MiamIAColors.Primary)
    BackendExecution.GPU -> BackendBadgeStyle(Icons.Filled.DeveloperMode, MiamIAColors.SectionIngredients)
    BackendExecution.CPU -> BackendBadgeStyle(Icons.Filled.DeveloperBoard, MiamIAColors.OnSurfaceVariant)
    BackendExecution.INDETERMINATE -> BackendBadgeStyle(Icons.Filled.HelpOutline, MiamIAColors.OnSurfaceVariant)
}

@Composable
private fun BackendBadge(backend: BackendExecution) {
    val style = backendBadgeStyle(backend)
    Surface(
        modifier = Modifier.testTag("inference_backend_badge"),
        shape = RoundedCornerShape(8.dp),
        color = style.color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = backend.label,
                tint = style.color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = backend.label,
                style = MaterialTheme.typography.labelSmall,
                color = style.color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CompositionEnergyPastille(estimatedKcalPer100g: Int?, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("composition_energy_pastille"),
        shape = RoundedCornerShape(12.dp),
        color = MiamIAColors.StatusSuccess.copy(alpha = 0.1f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = CompositionEnergyUiStrings.TITLE,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (estimatedKcalPer100g != null) {
                    CompositionEnergyUiStrings.primaryLine(estimatedKcalPer100g)
                } else {
                    CompositionEnergyUiStrings.UNAVAILABLE
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = CompositionEnergyUiStrings.HELPER,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BilanHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MiamIAColors.StatusSuccess.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MiamIAColors.StatusSuccess,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Bilan composition",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProductSection(product: String?, confidence: Int?) {
    SectionCard(
        icon = Icons.Filled.LocalDining,
        iconTint = MiamIAColors.SectionSynthese,
        iconBackground = MiamIAColors.SectionSynthese.copy(alpha = 0.1f),
        title = "Produit identifié",
        testTag = "bilan_product_section"
    ) {
        if (product.isNullOrBlank()) {
            // Feature P — état neutre « Produit non identifié » (section inconditionnelle).
            Text(
                text = "Produit non identifié",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            val label = if (confidence != null) {
                "Identification d'un $product à $confidence\u00A0%"
            } else {
                "Identification d'un $product"
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SyntheseSection(
    estimatedKcalPer100g: Int?,
    analysis: String,
    additiveKpi: AnalysisDisplayResult?,
    onRequestShowRaw: () -> Unit,
) {
    SectionCard(
        icon = Icons.Filled.Science,
        iconTint = MiamIAColors.SectionSynthese,
        iconBackground = MiamIAColors.SectionSynthese.copy(alpha = 0.1f),
        title = "Synthèse",
        testTag = "bilan_analysis_section"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            // Feature P — pastille kcal intégrée en tête de la Synthèse (IHI-P-FR-003).
            CompositionEnergyPastille(estimatedKcalPer100g = estimatedKcalPer100g)
            Surface(
                color = MiamIAColors.SectionSynthese.copy(alpha = 0.04f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = analysis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
            // Feature P — KPI additifs juxtaposés (additive-risk-insights) intégrés à la
            // Synthèse (attribution explicite IHI-C-FR-007 préservée via AdditiveKpiPanel).
            additiveKpi?.let { kpi ->
                AdditiveKpiPanel(
                    result = kpi,
                    onRequestShowRaw = onRequestShowRaw,
                )
            }
        }
    }
}

@Composable
private fun HealthImpactSection(impacts: List<IngredientHealthImpact>) {
    SectionCard(
        icon = Icons.Filled.FavoriteBorder,
        iconTint = MiamIAColors.SectionHealth,
        iconBackground = MiamIAColors.SectionHealth.copy(alpha = 0.1f),
        title = "Verdict par ingrédient",
        badge = "${impacts.size}",
        testTag = "bilan_health_impact_section"
    ) {
        if (impacts.isEmpty()) {
            // Feature P — état neutre « Aucun ingrédient à vigilance » (section inconditionnelle).
            Text(
                text = "Aucun ingrédient à vigilance identifié.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                impacts.forEach { impact ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = healthLevelColor(impact.level).copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = healthLevelLabel(impact.level),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = impact.ingredient,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (impact.note.isNotBlank()) {
                                CategoryChips(
                                    note = impact.note,
                                    chipColor = healthLevelColor(impact.level),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun healthLevelColor(level: String): Color = when (level.uppercase()) {
    "ROUGE" -> MiamIAColors.ImpactRed
    "ORANGE" -> MiamIAColors.ImpactOrange
    "VERT" -> MiamIAColors.ImpactGreen
    else -> MiamIAColors.ImpactNeutral
}

private fun healthLevelLabel(level: String): String = when (level.uppercase()) {
    "ROUGE" -> "Vigilance"
    "ORANGE" -> "Modéré"
    "VERT" -> "OK"
    else -> "Incertain"
}

@Composable
private fun DisclaimerSection(disclaimer: String) {
    if (disclaimer.isBlank()) return
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = disclaimer,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun RawTranscriptToggle(
    showRaw: Boolean,
    rawTranscript: String,
    onToggle: () -> Unit
) {
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
    TextButton(
        onClick = onToggle,
        modifier = Modifier.testTag("toggle_raw_transcript")
    ) {
        Icon(
            imageVector = if (showRaw) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (showRaw) "Masquer le texte original" else "Voir le texte original")
    }
    AnimatedVisibility(
        visible = showRaw,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = rawTranscript,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(12.dp)
                    .testTag("raw_transcript_secondary")
            )
        }
    }
}

@Composable
private fun SectionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    testTag: String,
    badge: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (badge != null) {
                    Surface(
                        color = iconTint.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelMedium,
                            color = iconTint,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            content()
        }
    }
}
