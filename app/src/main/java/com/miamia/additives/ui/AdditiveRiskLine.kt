package com.miamia.additives.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.miamia.additives.AdditiveJustificationFormatter
import com.miamia.additives.AdditiveLineConfidence
import com.miamia.additives.AdditiveRiskItem
import com.miamia.additives.AdditiveRiskLevel
import com.miamia.ui.theme.MiamIAColors

@Composable
fun AdditiveRiskLine(
    item: AdditiveRiskItem,
    index: Int,
    modifier: Modifier = Modifier,
) {
    var expanded by remember(item.canonicalName, item.justification) { mutableStateOf(false) }
    val fullText = item.justification
    val preview = AdditiveJustificationFormatter.truncatedForPreview(fullText)
    val showToggle = AdditiveJustificationFormatter.needsExpansion(fullText)
    val displayed = if (expanded) fullText else preview

    val levelLabel = levelDisplayLabel(item.level)
    val chipColor = levelContainerColor(item.level)
    val contentDesc = "${levelLabel}. ${item.displayName}. ${if (fullText.isNotBlank()) fullText else "Sans justification"}"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("additive_kpi_line_$index")
            .semantics { contentDescription = contentDesc }
            .animateContentSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                color = chipColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.semantics {
                    contentDescription = "Niveau de vigilance : $levelLabel"
                },
            ) {
                Text(
                    text = levelLabel,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall,
                )
                confidenceBadge(item.confidence)
                if (displayed.isNotBlank()) {
                    Text(
                        text = displayed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (showToggle) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Replier" else "Voir la justification complète")
                    }
                }
            }
        }
    }
}

@Composable
private fun confidenceBadge(confidence: AdditiveLineConfidence) {
    val text = when (confidence) {
        AdditiveLineConfidence.OK -> null
        AdditiveLineConfidence.NEEDS_CONFIRMATION -> "À confirmer"
        AdditiveLineConfidence.INCOHERENT -> "Incohérence"
        AdditiveLineConfidence.DUPLICATE_MERGED -> "Fusion doublon"
    }
    if (text != null) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(4.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

private fun levelDisplayLabel(level: AdditiveRiskLevel): String = when (level) {
    AdditiveRiskLevel.HIGH -> "Élevé"
    AdditiveRiskLevel.MEDIUM -> "Modéré"
    AdditiveRiskLevel.LOW -> "Faible"
    AdditiveRiskLevel.UNKNOWN -> "À confirmer"
}

private fun levelContainerColor(level: AdditiveRiskLevel): Color = when (level) {
    AdditiveRiskLevel.HIGH -> MiamIAColors.ImpactRed
    AdditiveRiskLevel.MEDIUM -> MiamIAColors.ImpactOrange
    AdditiveRiskLevel.LOW -> MiamIAColors.ImpactGreen
    AdditiveRiskLevel.UNKNOWN -> MiamIAColors.ImpactNeutral
}
