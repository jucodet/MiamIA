package com.miamia.ui.shared

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun AnimatedMarmite(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "marmite_fill"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "marmite_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val steamPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steam_phase"
    )

    val potColor = Color(0xFF6D4C2D)
    val potDarkColor = Color(0xFF4A3320)
    val handleColor = Color(0xFF5C3D1E)
    val liquidColor = Color(0xFF2E7D6F)
    val liquidSurfaceColor = Color(0xFF3DA08E)
    val steamColor = Color(0xFFCCCCCC)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("download_marmite")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp)
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val w = size.width
                val h = size.height

                val potLeft = w * 0.2f
                val potRight = w * 0.8f
                val potTop = h * 0.3f
                val potBottom = h * 0.85f
                val potWidth = potRight - potLeft
                val potHeight = potBottom - potTop
                val cornerR = potWidth * 0.15f

                drawSteam(steamPhase, animatedProgress, potLeft, potRight, potTop, steamColor)

                val rimHeight = h * 0.04f
                drawRoundRect(
                    color = potDarkColor,
                    topLeft = Offset(potLeft - 4.dp.toPx(), potTop - rimHeight),
                    size = Size(potWidth + 8.dp.toPx(), rimHeight + 4.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                drawHandle(
                    cx = potLeft - 6.dp.toPx(),
                    cy = potTop + potHeight * 0.3f,
                    radius = 12.dp.toPx(),
                    color = handleColor,
                    strokeWidth = 5.dp.toPx(),
                    isLeft = true
                )
                drawHandle(
                    cx = potRight + 6.dp.toPx(),
                    cy = potTop + potHeight * 0.3f,
                    radius = 12.dp.toPx(),
                    color = handleColor,
                    strokeWidth = 5.dp.toPx(),
                    isLeft = false
                )

                val potPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(potLeft, potTop, potRight, potBottom),
                            topLeft = CornerRadius(0f),
                            topRight = CornerRadius(0f),
                            bottomRight = CornerRadius(cornerR),
                            bottomLeft = CornerRadius(cornerR)
                        )
                    )
                }

                drawPath(potPath, color = potColor)

                if (animatedProgress > 0.005f) {
                    clipPath(potPath) {
                        val fillHeight = potHeight * animatedProgress
                        val fillTop = potBottom - fillHeight

                        drawRect(
                            color = liquidColor,
                            topLeft = Offset(potLeft, fillTop + 4.dp.toPx()),
                            size = Size(potWidth, fillHeight)
                        )

                        val wavePath = Path().apply {
                            val waveAmplitude = 3.dp.toPx()
                            val steps = 40
                            moveTo(potLeft, fillTop + waveAmplitude)
                            for (i in 0..steps) {
                                val x = potLeft + (potWidth * i / steps)
                                val y = fillTop + sin(
                                    wavePhase + (i.toFloat() / steps) * 4f * Math.PI.toFloat()
                                ) * waveAmplitude
                                lineTo(x, y)
                            }
                            lineTo(potRight, fillTop + 10.dp.toPx())
                            lineTo(potLeft, fillTop + 10.dp.toPx())
                            close()
                        }
                        drawPath(wavePath, color = liquidSurfaceColor)
                    }
                }

                drawPath(potPath, color = potDarkColor, style = Stroke(width = 2.dp.toPx()))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("download_progress_percent")
        )
    }
}

private fun DrawScope.drawSteam(
    phase: Float,
    progress: Float,
    potLeft: Float,
    potRight: Float,
    potTop: Float,
    color: Color
) {
    if (progress < 0.15f) return
    val alpha = ((progress - 0.15f) / 0.85f).coerceIn(0f, 0.5f)
    val potCenterX = (potLeft + potRight) / 2f
    val steamPositions = listOf(-0.15f, 0f, 0.15f)

    for ((i, offset) in steamPositions.withIndex()) {
        val x = potCenterX + offset * (potRight - potLeft)
        val phaseOffset = phase + i * 2.1f
        val yOffset = (phaseOffset % (2f * Math.PI.toFloat())) /
            (2f * Math.PI.toFloat()) * 30.dp.toPx()
        val sway = sin(phaseOffset * 2f) * 6.dp.toPx()
        val y = potTop - 10.dp.toPx() - yOffset
        val currentAlpha = alpha * (1f - yOffset / (30.dp.toPx()))
        if (currentAlpha > 0.02f) {
            drawCircle(
                color = color.copy(alpha = currentAlpha),
                radius = 4.dp.toPx(),
                center = Offset(x + sway, y)
            )
        }
    }
}

private fun DrawScope.drawHandle(
    cx: Float,
    cy: Float,
    radius: Float,
    color: Color,
    strokeWidth: Float,
    isLeft: Boolean
) {
    val sweepAngle = 180f
    val startAngle = if (isLeft) 90f else -90f
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(cx - radius, cy - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}
