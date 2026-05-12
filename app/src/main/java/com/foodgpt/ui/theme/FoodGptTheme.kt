package com.foodgpt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FoodGptLightColorScheme = lightColorScheme(
    primary = FoodGptColors.Primary,
    onPrimary = FoodGptColors.OnPrimary,
    primaryContainer = FoodGptColors.PrimaryContainer,
    onPrimaryContainer = FoodGptColors.OnPrimaryContainer,
    secondary = FoodGptColors.Secondary,
    onSecondary = FoodGptColors.OnSecondary,
    secondaryContainer = FoodGptColors.SecondaryContainer,
    onSecondaryContainer = FoodGptColors.OnSecondaryContainer,
    tertiary = FoodGptColors.Tertiary,
    onTertiary = FoodGptColors.OnTertiary,
    tertiaryContainer = FoodGptColors.TertiaryContainer,
    onTertiaryContainer = FoodGptColors.OnTertiaryContainer,
    background = FoodGptColors.Background,
    onBackground = FoodGptColors.OnBackground,
    surface = FoodGptColors.Surface,
    onSurface = FoodGptColors.OnSurface,
    surfaceVariant = FoodGptColors.SurfaceVariant,
    onSurfaceVariant = FoodGptColors.OnSurfaceVariant,
    outline = FoodGptColors.Outline,
    outlineVariant = FoodGptColors.OutlineVariant,
    error = FoodGptColors.Error,
    onError = FoodGptColors.OnError,
    errorContainer = FoodGptColors.ErrorContainer,
    onErrorContainer = FoodGptColors.OnErrorContainer,
)

@Composable
fun FoodGptTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FoodGptLightColorScheme,
        content = content
    )
}
