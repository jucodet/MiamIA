package com.miamia.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BilanHeader()
        if (!bilan.identifiedProduct.isNullOrBlank()) {
            ProductSection(bilan.identifiedProduct, bilan.productConfidence)
        }
        AnalysisSection(bilan.compositionAnalysis)
        IngredientsSection(bilan.ingredientLines)
        if (bilan.healthImpacts.isNotEmpty()) {
            HealthImpactSection(bilan.healthImpacts)
        }
        additiveKpi?.let { kpi ->
            AdditivesSection(kpi, onRequestShowRaw = onToggleRaw)
        }
        DisclaimerSection(bilan.disclaimer)
        if (inferenceTimeMs > 0L) {
            InferenceTimeBadge(inferenceTimeMs)
        }
        RawTranscriptToggle(
            showRaw = showRaw,
            rawTranscript = rawTranscript,
            onToggle = onToggleRaw
        )
    }
}

@Composable
private fun InferenceTimeBadge(inferenceTimeMs: Long) {
    val seconds = inferenceTimeMs / 1000.0
    val formatted = "%.1f".format(seconds)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Column {
            Text(
                text = "Bilan composition",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Analyse terminée",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IngredientsSection(ingredients: List<String>) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bilan_ingredients_section")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MiamIAColors.SectionIngredients.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = MiamIAColors.SectionIngredients,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Ingrédients identifiés",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MiamIAColors.SectionIngredients.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${ingredients.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MiamIAColors.SectionIngredients,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Replier" else "Déplier",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    ingredients.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MiamIAColors.SectionIngredients,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp, top = 1.dp)
                            )
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductSection(product: String, confidence: Int?) {
    SectionCard(
        icon = Icons.Filled.LocalDining,
        iconTint = MiamIAColors.SectionSynthese,
        iconBackground = MiamIAColors.SectionSynthese.copy(alpha = 0.1f),
        title = "Produit identifié",
        testTag = "bilan_product_section"
    ) {
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

@Composable
private fun AnalysisSection(analysis: String) {
    SectionCard(
        icon = Icons.Filled.Science,
        iconTint = MiamIAColors.SectionSynthese,
        iconBackground = MiamIAColors.SectionSynthese.copy(alpha = 0.1f),
        title = "Synthèse",
        testTag = "bilan_analysis_section"
    ) {
        Surface(
            color = MiamIAColors.SectionSynthese.copy(alpha = 0.04f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = analysis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun AdditivesSection(
    kpi: AnalysisDisplayResult,
    onRequestShowRaw: () -> Unit
) {
    SectionCard(
        icon = Icons.Filled.Warning,
        iconTint = MiamIAColors.SectionAdditives,
        iconBackground = MiamIAColors.SectionAdditives.copy(alpha = 0.1f),
        title = "Additifs",
        testTag = "bilan_additives_section"
    ) {
        AdditiveKpiPanel(
            result = kpi,
            onRequestShowRaw = onRequestShowRaw,
            modifier = Modifier.padding(top = 8.dp)
        )
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
