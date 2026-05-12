package com.foodgpt.result

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.foodgpt.camera.BilanResultCard
import com.foodgpt.camera.CameraViewModel
import com.foodgpt.camera.StreamingBilanState
import com.foodgpt.camera.StreamingSection
import com.foodgpt.ui.shared.AnimatedWhisk
import com.foodgpt.ui.shared.CategoryChips
import com.foodgpt.ui.shared.WAITING_PHRASES
import com.foodgpt.ui.theme.FoodGptColors
import kotlinx.coroutines.delay

@Composable
fun LlmResultScreen(
    viewModel: CameraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val streamingState by viewModel.streamingBilan.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (val state = streamingState) {
                is StreamingBilanState.Idle -> {
                    StreamingHeader()
                }

                is StreamingBilanState.Streaming -> {
                    StreamingHeader()
                    StreamingContent(state)
                }

                is StreamingBilanState.Complete -> {
                    var showRaw by remember { mutableStateOf(false) }
                    BilanResultCard(
                        bilan = state.bilan,
                        rawTranscript = state.rawTranscript,
                        additiveKpi = null,
                        showRaw = showRaw,
                        onToggleRaw = { showRaw = !showRaw }
                    )
                }

                is StreamingBilanState.Error -> {
                    ErrorContent(state)
                }
            }
        }

        if (streamingState is StreamingBilanState.Complete || streamingState is StreamingBilanState.Error) {
            Button(
                onClick = {
                    viewModel.resetStreamingBilan()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("llm_result_back")
            ) {
                Text("Retour")
            }
        }
    }
}


@Composable
private fun StreamingHeader() {
    val shuffledPhrases = remember { WAITING_PHRASES.shuffled() }
    var phraseIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5_000)
            phraseIndex = (phraseIndex + 1) % shuffledPhrases.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedWhisk(modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Analyse en cours…",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("llm_result_title")
            )
        }

        AnimatedContent(
            targetState = phraseIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith
                    fadeOut(animationSpec = tween(400))
            },
            label = "phrase_rotation"
        ) { index ->
            Text(
                text = shuffledPhrases[index % shuffledPhrases.size],
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("streaming_waiting_phrase")
            )
        }
    }
}


@Composable
private fun StreamingContent(state: StreamingBilanState.Streaming) {
    AnimatedVisibility(
        visible = state.partialAnalysis != null,
        enter = fadeIn()
    ) {
        StreamingSectionCard(
            icon = Icons.Filled.Science,
            iconTint = FoodGptColors.SectionSynthese,
            title = "Synthèse",
            isLoading = state.sectionReached == StreamingSection.ANALYSE,
            testTag = "streaming_analysis_card"
        ) {
            if (state.partialAnalysis != null) {
                Text(
                    text = state.partialAnalysis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    AnimatedVisibility(
        visible = state.partialIngredients.isNotEmpty(),
        enter = fadeIn()
    ) {
        StreamingSectionCard(
            icon = Icons.AutoMirrored.Filled.List,
            iconTint = FoodGptColors.SectionIngredients,
            title = "Ingrédients identifiés",
            badge = "${state.partialIngredients.size}",
            isLoading = state.sectionReached == StreamingSection.LISTE,
            testTag = "streaming_ingredients_card"
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                state.partialIngredients.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FoodGptColors.SectionIngredients,
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

    AnimatedVisibility(
        visible = state.partialHealthImpacts.isNotEmpty(),
        enter = fadeIn()
    ) {
        StreamingSectionCard(
            icon = Icons.Filled.FavoriteBorder,
            iconTint = FoodGptColors.SectionHealth,
            title = "Verdict par ingrédient",
            badge = "${state.partialHealthImpacts.size}",
            isLoading = state.sectionReached == StreamingSection.IMPACT_SANTE,
            testTag = "streaming_health_impact_card"
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                state.partialHealthImpacts.forEach { impact ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = impactLevelColor(impact.level).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = impactLevelEmoji(impact.level),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall
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
                                    chipColor = impactLevelColor(impact.level),
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

@Composable
private fun StreamingSectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    testTag: String,
    badge: String? = null,
    isLoading: Boolean = false,
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
                        .background(iconTint.copy(alpha = 0.1f)),
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
                if (isLoading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            content()
        }
    }
}

private fun impactLevelColor(level: String): Color = when (level) {
    "VERT" -> FoodGptColors.ImpactGreen
    "ORANGE" -> FoodGptColors.ImpactOrange
    "ROUGE" -> FoodGptColors.ImpactRed
    else -> FoodGptColors.ImpactNeutral
}

private fun impactLevelEmoji(level: String): String = when (level) {
    "VERT" -> "\u2705"
    "ORANGE" -> "\u26A0\uFE0F"
    "ROUGE" -> "\u274C"
    else -> "\u2753"
}

@Composable
private fun ErrorContent(state: StreamingBilanState.Error) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Analyse — erreur",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag("llm_result_title")
        )
    }
    if (state.errorCategory != null) {
        Text(
            text = state.errorCategory,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag("llm_result_error_category")
        )
    }
    Text(
        text = state.message,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.testTag("llm_result_body")
    )
}
