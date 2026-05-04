package com.foodgpt.additives.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.foodgpt.additives.AnalysisDisplayResult

@Composable
fun AdditiveKpiPanel(
    result: AnalysisDisplayResult,
    modifier: Modifier = Modifier,
    onRequestShowRaw: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("additive_kpi_panel"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (result.parseErrors.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("additive_kpi_parse_banner"),
            ) {
                Text(
                    text = result.parseErrors.joinToString(" "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        if (result.isEmptyState) {
            Text(
                text = "Aucun additif identifié dans cette analyse.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("additive_kpi_empty_state"),
            )
            if (onRequestShowRaw != null) {
                OutlinedButton(onClick = onRequestShowRaw) {
                    Text("Voir le texte original")
                }
            }
        } else {
            AdditiveKpiSummaryBar(summary = result.summary)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                result.itemsOrdered.forEachIndexed { index, item ->
                    AdditiveRiskLine(item = item, index = index)
                }
            }
        }
    }
}
