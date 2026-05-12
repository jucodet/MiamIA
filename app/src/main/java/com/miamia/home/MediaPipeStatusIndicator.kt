package com.miamia.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.miamia.ui.theme.MiamIAColors

@Composable
fun MediaPipeStatusIndicator(
    viewState: MediaPipeStatusViewState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("mediapipe_status_indicator"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(12.dp)
                .background(colorFor(viewState.colorToken), CircleShape)
                .testTag("mediapipe_status_dot")
        )
        Text(
            text = viewState.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("mediapipe_status_label")
        )
    }
}

private fun colorFor(token: MediaPipeStatusColorToken): Color = when (token) {
    MediaPipeStatusColorToken.NEUTRAL -> MiamIAColors.ImpactNeutral
    MediaPipeStatusColorToken.SUCCESS -> MiamIAColors.StatusSuccess
    MediaPipeStatusColorToken.WARNING_OR_ERROR -> MiamIAColors.StatusError
}
