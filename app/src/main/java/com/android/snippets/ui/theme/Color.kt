package com.android.snippets.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance

// ── Material 3 – Orange Baseline Palette (#FF9800 Seed) ──────────────────────
// Used as fallback on devices below Android 12 (no Monet/dynamic color)

// Light scheme
val OrangePrimary        = Color(0xFF8A5100)
val OrangeOnPrimary      = Color(0xFFFFFFFF)
val OrangePrimaryContainer  = Color(0xFFFFDCBE)
val OrangeOnPrimaryContainer = Color(0xFF2C1600)

val OrangeSecondary      = Color(0xFF735A42)
val OrangeOnSecondary    = Color(0xFFFFFFFF)
val OrangeSecondaryContainer = Color(0xFFFFDCBE)
val OrangeOnSecondaryContainer = Color(0xFF291806)

val OrangeTertiary       = Color(0xFF58633A)
val OrangeOnTertiary     = Color(0xFFFFFFFF)
val OrangeTertiaryContainer = Color(0xFFDCE8B4)
val OrangeOnTertiaryContainer = Color(0xFF161E01)

val OrangeError          = Color(0xFFBA1A1A)
val OrangeOnError        = Color(0xFFFFFFFF)
val OrangeErrorContainer = Color(0xFFFFDAD6)
val OrangeOnErrorContainer = Color(0xFF410002)

val OrangeBackground     = Color(0xFFFFF8F5)
val OrangeOnBackground   = Color(0xFF221A14)
val OrangeSurface        = Color(0xFFFFF8F5)
val OrangeOnSurface      = Color(0xFF221A14)
val OrangeSurfaceVariant = Color(0xFFF3DFD2)
val OrangeOnSurfaceVariant = Color(0xFF51443B)
val OrangeOutline        = Color(0xFF847469)
val OrangeOutlineVariant = Color(0xFFD6C3B6)
val OrangeInverseSurface = Color(0xFF382F28)
val OrangeInverseOnSurface = Color(0xFFFEEDDF)
val OrangeInversePrimary = Color(0xFFFFB870)
val OrangeSurfaceContainer = Color(0xFFFCEBE0)
val OrangeSurfaceContainerLow = Color(0xFFFFF1E8)
val OrangeSurfaceContainerHigh = Color(0xFFF6E6DA)
val OrangeSurfaceContainerLowest = Color(0xFFFFFFFF)
val OrangeSurfaceContainerHighest = Color(0xFFF0E0D5)

// Dark scheme
val OrangePrimaryDark    = Color(0xFFFFB870)
val OrangeOnPrimaryDark  = Color(0xFF4A2800)
val OrangePrimaryContainerDark = Color(0xFF693C00)
val OrangeOnPrimaryContainerDark = Color(0xFFFFDCBE)

val OrangeSecondaryDark  = Color(0xFFE3C0A4)
val OrangeOnSecondaryDark = Color(0xFF412C18)
val OrangeSecondaryContainerDark = Color(0xFF5A422D)
val OrangeOnSecondaryContainerDark = Color(0xFFFFDCBE)

val OrangeTertiaryDark   = Color(0xFFC0CC9A)
val OrangeOnTertiaryDark = Color(0xFF2B3410)
val OrangeTertiaryContainerDark = Color(0xFF414B24)
val OrangeOnTertiaryContainerDark = Color(0xFFDCE8B4)

val OrangeErrorDark      = Color(0xFFFFB4AB)
val OrangeOnErrorDark    = Color(0xFF690005)
val OrangeErrorContainerDark = Color(0xFF93000A)
val OrangeOnErrorContainerDark = Color(0xFFFFDAD6)

val OrangeBackgroundDark = Color(0xFF1A120C)
val OrangeOnBackgroundDark = Color(0xFFF0E0D5)
val OrangeSurfaceDark    = Color(0xFF1A120C)
val OrangeOnSurfaceDark  = Color(0xFFF0E0D5)
val OrangeSurfaceVariantDark = Color(0xFF51443B)
val OrangeOnSurfaceVariantDark = Color(0xFFD6C3B6)
val OrangeOutlineDark    = Color(0xFF9E8E82)
val OrangeOutlineVariantDark = Color(0xFF51443B)
val OrangeInverseSurfaceDark = Color(0xFFF0E0D5)
val OrangeInverseOnSurfaceDark = Color(0xFF382F28)
val OrangeInversePrimaryDark = Color(0xFF8A5100)
val OrangeSurfaceContainerDark = Color(0xFF271E17)
val OrangeSurfaceContainerLowDark = Color(0xFF221A14)
val OrangeSurfaceContainerHighDark = Color(0xFF322921)
val OrangeSurfaceContainerLowestDark = Color(0xFF140D07)
val OrangeSurfaceContainerHighestDark = Color(0xFF3D332C)

// ── Material You Monet Snippet Styling ──────────────────────────────────────
// Snippets dynamically adopt Monet dynamic colors from MaterialTheme (primaryContainer / onPrimaryContainer)

@androidx.compose.runtime.Composable
fun rememberSnippetBackgroundColor(): Color {
    return androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
}

@androidx.compose.runtime.Composable
fun rememberSnippetTextColor(): Color {
    return androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
}

fun getSnippetBackgroundColorInt(context: android.content.Context, isDark: Boolean = false): Int {
    return getAppColorScheme(context, isDark).primaryContainer.toArgb()
}

fun getSnippetTextColorInt(context: android.content.Context, isDark: Boolean = false): Int {
    return getAppColorScheme(context, isDark).onPrimaryContainer.toArgb()
}