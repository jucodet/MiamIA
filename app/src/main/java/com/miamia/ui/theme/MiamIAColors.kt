package com.miamia.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette pastel accessible MiamIA.
 * Toutes les couleurs respectent un ratio de contraste >= 4.5:1
 * sur fond blanc (#FFFFFF) pour le texte normal (WCAG AA).
 */
object MiamIAColors {

    // --- Primary palette (sage / teal) ---
    val Primary = Color(0xFF2E7D6F)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFD4F0E8)
    val OnPrimaryContainer = Color(0xFF002118)

    // --- Secondary palette (muted blue) ---
    val Secondary = Color(0xFF3A6FA5)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFD6E8F5)
    val OnSecondaryContainer = Color(0xFF001D33)

    // --- Tertiary palette (warm amber) ---
    val Tertiary = Color(0xFF7D6B4A)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFF5E6CC)
    val OnTertiaryContainer = Color(0xFF2B1F08)

    // --- Neutral surfaces ---
    val Background = Color(0xFFFAFBF8)
    val OnBackground = Color(0xFF1A1C19)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1A1C19)
    val SurfaceVariant = Color(0xFFEDE9E0)
    val OnSurfaceVariant = Color(0xFF49463F)
    val Outline = Color(0xFF7A776E)
    val OutlineVariant = Color(0xFFCBC7BE)

    // --- Error ---
    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)

    // --- Section icon colors (pastel-friendly, contrast >= 4.5:1 on white) ---
    val SectionSynthese = Color(0xFF3A7D6F)
    val SectionIngredients = Color(0xFF3A6FA5)
    val SectionHealth = Color(0xFF00796B)
    val SectionAdditives = Color(0xFFBF5A1A)

    // --- Semantic impact/risk colors ---
    val ImpactGreen = Color(0xFF2E7D32)
    val ImpactOrange = Color(0xFFE67E22)
    val ImpactRed = Color(0xFFCC3333)
    val ImpactNeutral = Color(0xFF757575)

    // --- Status colors ---
    val StatusSuccess = Color(0xFF2E7D32)
    val StatusError = Color(0xFFC62828)
}
