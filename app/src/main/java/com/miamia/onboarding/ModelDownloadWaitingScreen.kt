package com.miamia.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miamia.ui.shared.AnimatedWhisk
import com.miamia.ui.shared.WAITING_PHRASES
import kotlinx.coroutines.delay

@Composable
fun ModelDownloadWaitingScreen(
    progress: DownloadProgress?,
    error: LlmModelReadinessState.Error?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shuffledPhrases = remember { WAITING_PHRASES.shuffled() }
    var phraseIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5_000)
            phraseIndex = (phraseIndex + 1) % shuffledPhrases.size
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("download_waiting_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedWhisk(
                modifier = Modifier
                    .size(64.dp)
                    .testTag("download_whisk_animation")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Téléchargement du modèle de langage en cours...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (error != null) {
                Text(
                    text = error.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (error.canRetry) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.testTag("download_retry_button")
                    ) {
                        Text("Réessayer")
                    }
                }
            } else if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.percent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .testTag("download_progress_bar")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${progress.percent}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = phraseIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith
                        fadeOut(animationSpec = tween(400))
                },
                label = "download_phrase_rotation"
            ) { index ->
                Text(
                    text = shuffledPhrases[index % shuffledPhrases.size],
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("download_waiting_phrase")
                )
            }
        }
    }
}
