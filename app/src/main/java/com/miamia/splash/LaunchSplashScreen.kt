package com.miamia.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.miamia.ui.theme.MiamIAColors

/**
 * Splash (Feature H) : Lottie « food », titre style graffiti (sans polices téléchargeables GMS),
 * version en pied.
 */
@Composable
fun LaunchSplashScreen(
    versionName: String,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("food_animation.lottie"),
    )
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MiamIAColors.PrimaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (composition != null) {
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieProgress },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MiamIAColors.Primary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            GraffitiStyleTitle(text = "MiamIA")
        }
        Text(
            text = "Version $versionName",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            style = TextStyle(
                fontSize = 14.sp,
                color = MiamIAColors.OnPrimaryContainer.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun GraffitiStyleTitle(text: String) {
    val fillBrush = Brush.linearGradient(
        colors = listOf(
            MiamIAColors.Secondary,
            MiamIAColors.Tertiary,
            MiamIAColors.Primary,
            MiamIAColors.SectionAdditives,
        ),
        start = Offset(0f, 0f),
        end = Offset(220f, 48f),
    )
    val outline = Color(0xFF0D0D0D)
    val baseStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 46.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 3.sp,
        textAlign = TextAlign.Center,
        shadow = Shadow(
            color = Color(0x55000000),
            offset = Offset(4f, 5f),
            blurRadius = 1f,
        ),
    )
    val offsets = listOf(
        -4.dp to -4.dp, 4.dp to -4.dp, -4.dp to 4.dp, 4.dp to 4.dp,
        -4.dp to 0.dp, 4.dp to 0.dp, 0.dp to -4.dp, 0.dp to 4.dp,
    )
    Box(
        modifier = Modifier.graphicsLayer { rotationZ = -4f },
        contentAlignment = Alignment.Center,
    ) {
        offsets.forEach { (ox, oy) ->
            Text(
                text = text,
                style = baseStyle.copy(color = outline),
                modifier = Modifier.offset(ox, oy),
            )
        }
        Text(
            text = text,
            style = baseStyle.copy(brush = fillBrush),
        )
    }
}
