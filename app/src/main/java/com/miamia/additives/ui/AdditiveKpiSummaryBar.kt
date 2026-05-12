package com.miamia.additives.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.miamia.additives.AdditiveRiskLevel
import com.miamia.additives.RiskSummaryKpi

@Composable
fun AdditiveKpiSummaryBar(
    summary: RiskSummaryKpi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Synthèse additifs", style = MaterialTheme.typography.titleSmall)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryChip("Total", summary.totalCount, "additive_kpi_total")
                SummaryChip("Rouges", summary.highCount, "additive_kpi_high")
                SummaryChip("Oranges", summary.mediumCount, "additive_kpi_medium")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryChip("Verts", summary.lowCount, "additive_kpi_low")
                SummaryChip("À confirmer", summary.unknownCount, "additive_kpi_unknown")
            }
        }
        val global = summary.globalLevel
        if (global != null) {
            Text(
                text = "Niveau global observé : ${globalLevelFr(global)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("additive_kpi_global_level"),
            )
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: Int, testTag: String) {
    Text(
        text = "$label : $value",
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.testTag(testTag),
    )
}

private fun globalLevelFr(level: AdditiveRiskLevel): String = when (level) {
    AdditiveRiskLevel.HIGH -> "vigilance élevée"
    AdditiveRiskLevel.MEDIUM -> "vigilance modérée"
    AdditiveRiskLevel.LOW -> "vigilance faible"
    AdditiveRiskLevel.UNKNOWN -> "indéterminé"
}
