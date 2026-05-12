package com.miamia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MiamIALightColorScheme = lightColorScheme(
    primary = MiamIAColors.Primary,
    onPrimary = MiamIAColors.OnPrimary,
    primaryContainer = MiamIAColors.PrimaryContainer,
    onPrimaryContainer = MiamIAColors.OnPrimaryContainer,
    secondary = MiamIAColors.Secondary,
    onSecondary = MiamIAColors.OnSecondary,
    secondaryContainer = MiamIAColors.SecondaryContainer,
    onSecondaryContainer = MiamIAColors.OnSecondaryContainer,
    tertiary = MiamIAColors.Tertiary,
    onTertiary = MiamIAColors.OnTertiary,
    tertiaryContainer = MiamIAColors.TertiaryContainer,
    onTertiaryContainer = MiamIAColors.OnTertiaryContainer,
    background = MiamIAColors.Background,
    onBackground = MiamIAColors.OnBackground,
    surface = MiamIAColors.Surface,
    onSurface = MiamIAColors.OnSurface,
    surfaceVariant = MiamIAColors.SurfaceVariant,
    onSurfaceVariant = MiamIAColors.OnSurfaceVariant,
    outline = MiamIAColors.Outline,
    outlineVariant = MiamIAColors.OutlineVariant,
    error = MiamIAColors.Error,
    onError = MiamIAColors.OnError,
    errorContainer = MiamIAColors.ErrorContainer,
    onErrorContainer = MiamIAColors.OnErrorContainer,
)

@Composable
fun MiamIATheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MiamIALightColorScheme,
        content = content
    )
}
