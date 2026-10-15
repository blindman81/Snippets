package com.android.snippets.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance

// ── Harvest Modern – Orange Baseline Palette ──────────────────────────────────
// Used as fallback on devices below Android 12 (no Monet/dynamic color)

// Light scheme
val OrangePrimary        = Color(0xFF824D00)
val OrangeOnPrimary      = Color(0xFFFFFFFF)
val OrangePrimaryContainer  = Color(0xFFFFDCC0)
val OrangeOnPrimaryContainer = Color(0xFF2A1700)

val OrangeSecondary      = Color(0xFF705D48)
val OrangeOnSecondary    = Color(0xFFFFFFFF)
val OrangeSecondaryContainer = Color(0xFFFCDDC1)
val OrangeOnSecondaryContainer = Color(0xFF281909)

val OrangeTertiary       = Color(0xFF006874)
val OrangeOnTertiary     = Color(0xFFFFFFFF)
val OrangeTertiaryContainer = Color(0xFFA1EFFF)
val OrangeOnTertiaryContainer = Color(0xFF001F24)

val OrangeError          = Color(0xFFBA1A1A)
val OrangeOnError        = Color(0xFFFFFFFF)
val OrangeErrorContainer = Color(0xFFFFDAD6)
val OrangeOnErrorContainer = Color(0xFF93000A)

val OrangeBackground     = Color(0xFFFFF8F3)
val OrangeOnBackground   = Color(0xFF231A14)
val OrangeSurface        = Color(0xFFFFF8F3)
val OrangeOnSurface      = Color(0xFF231A14)
val OrangeSurfaceVariant = Color(0xFFF5DEC9)
val OrangeOnSurfaceVariant = Color(0xFF423328)
val OrangeOutline        = Color(0xFF8E786B)
val OrangeOutlineVariant = Color(0xFFE2C9B7)
val OrangeInverseSurface = Color(0xFF3B2F27)
val OrangeInverseOnSurface = Color(0xFFFDEEE3)
val OrangeInversePrimary = Color(0xFFFFB86F)
val OrangeSurfaceContainer = Color(0xFFFDEEE3)
val OrangeSurfaceContainerLow = Color(0xFFFFF4EC)
val OrangeSurfaceContainerHigh = Color(0xFFF8E7DB)
val OrangeSurfaceContainerLowest = Color(0xFFFFFFFF)
val OrangeSurfaceContainerHighest = Color(0xFFF2E0D2)

// Dark scheme
val OrangePrimaryDark    = Color(0xFFFFB86F)
val OrangeOnPrimaryDark  = Color(0xFF452700)
val OrangePrimaryContainerDark = Color(0xFF623900)
val OrangeOnPrimaryContainerDark = Color(0xFFFFDCC0)

val OrangeSecondaryDark  = Color(0xFFDDC2A8)
val OrangeOnSecondaryDark = Color(0xFF3E2E1D)
val OrangeSecondaryContainerDark = Color(0xFF564432)
val OrangeOnSecondaryContainerDark = Color(0xFFFCDDC1)

val OrangeTertiaryDark   = Color(0xFF4FD8EB)
val OrangeOnTertiaryDark = Color(0xFF00363D)
val OrangeTertiaryContainerDark = Color(0xFF004F58)
val OrangeOnTertiaryContainerDark = Color(0xFFA1EFFF)

val OrangeErrorDark      = Color(0xFFFFB4AB)
val OrangeOnErrorDark    = Color(0xFF690005)
val OrangeErrorContainerDark = Color(0xFF93000A)
val OrangeOnErrorContainerDark = Color(0xFFFFDAD6)

val OrangeBackgroundDark = Color(0xFF1F1A16)
val OrangeOnBackgroundDark = Color(0xFFF4E5DC)
val OrangeSurfaceDark    = Color(0xFF1F1A16)
val OrangeOnSurfaceDark  = Color(0xFFF4E5DC)
val OrangeSurfaceVariantDark = Color(0xFF554437)
val OrangeOnSurfaceVariantDark = Color(0xFFE0CDBF)
val OrangeOutlineDark    = Color(0xFFA69285)
val OrangeOutlineVariantDark = Color(0xFF554437)
val OrangeInverseSurfaceDark = Color(0xFFF4E5DC)
val OrangeInverseOnSurfaceDark = Color(0xFF382D26)
val OrangeInversePrimaryDark = Color(0xFF824D00)
val OrangeSurfaceContainerDark = Color(0xFF2E251E)
val OrangeSurfaceContainerLowDark = Color(0xFF261F1A)
val OrangeSurfaceContainerHighDark = Color(0xFF372D24)
val OrangeSurfaceContainerLowestDark = Color(0xFF14100D)
val OrangeSurfaceContainerHighestDark = Color(0xFF43372E)

// ── Material Design Pastel Snippet Palette ─────────────────────────────────
// Official Google Material Design pastel color hex codes (200 tone series)

val PastelSnippetColorsInt = listOf(
    0xFFEF9A9A.toInt(), // Red 200
    0xFFF48FB1.toInt(), // Pink 200
    0xFFCE93D8.toInt(), // Purple 200
    0xFFB39DDB.toInt(), // Deep Purple 200
    0xFF9FA8DA.toInt(), // Indigo 200
    0xFF90CAF9.toInt(), // Blue 200
    0xFF80DEEA.toInt(), // Cyan 200
    0xFF80CBC4.toInt(), // Teal 200
    0xFFA5D6A7.toInt(), // Green 200
    0xFFFFE082.toInt(), // Amber 200
    0xFFFFCC80.toInt(), // Orange 200
    0xFFFFAB91.toInt(), // Deep Orange 200
    0xFFB0BEC5.toInt()  // Blue Grey 200
)

val PastelSnippetColors = PastelSnippetColorsInt.map { Color(it) }

// Previous palette colors (300 series / legacy) mapped to the 200 series pastel equivalents
val LegacySnippetColorsInt = listOf(
    0xFFE57373.toInt(), // Red 300
    0xFFF06292.toInt(), // Pink 300
    0xFFBA68C8.toInt(), // Purple 300
    0xFF9575CD.toInt(), // Deep Purple 300
    0xFF7986CB.toInt(), // Indigo 300
    0xFF64B5F6.toInt(), // Blue 300
    0xFF4DD0E1.toInt(), // Cyan 300
    0xFF4DB6AC.toInt(), // Teal 300
    0xFF81C784.toInt(), // Green 300
    0xFFFFD54F.toInt(), // Amber 300
    0xFFFFB74D.toInt(), // Orange 300
    0xFFFF8A65.toInt(), // Deep Orange 300
    0xFF90A4AE.toInt()  // Blue Grey 300
)

fun toPastelSnippetColorInt(text: String, forcedColor: Int? = null): Int {
    if (forcedColor != null) {
        // If it's already an official pastel 200 color, keep it
        if (PastelSnippetColorsInt.contains(forcedColor)) {
            return forcedColor
        }
        // If it was one of the legacy palette colors, map to the matching 200 pastel color
        val legacyIndex = LegacySnippetColorsInt.indexOf(forcedColor)
        if (legacyIndex != -1) {
            return PastelSnippetColorsInt[legacyIndex]
        }
        // If it was white, black, or empty (e.g. from previous placeholder assignments), hash it
        if (forcedColor == android.graphics.Color.WHITE || forcedColor == android.graphics.Color.BLACK || forcedColor == 0) {
            val hash = Math.abs(text.trim().hashCode())
            return PastelSnippetColorsInt[hash % PastelSnippetColorsInt.size]
        }
        // For any other existing custom color, map to the closest Material Design pastel color
        val r = (forcedColor shr 16) and 0xFF
        val g = (forcedColor shr 8) and 0xFF
        val b = forcedColor and 0xFF
        return PastelSnippetColorsInt.minByOrNull { pastel ->
            val pr = (pastel shr 16) and 0xFF
            val pg = (pastel shr 8) and 0xFF
            val pb = pastel and 0xFF
            val dr = r - pr
            val dg = g - pg
            val db = b - pb
            dr * dr + dg * dg + db * db
        } ?: PastelSnippetColorsInt[Math.abs(text.trim().hashCode()) % PastelSnippetColorsInt.size]
    }
    val hash = Math.abs(text.trim().hashCode())
    return PastelSnippetColorsInt[hash % PastelSnippetColorsInt.size]
}

fun getPastelSnippetColor(text: String, forcedColor: Int? = null): Color {
    return Color(toPastelSnippetColorInt(text, forcedColor))
}

fun getPastelSnippetColorInt(text: String, forcedColor: Int? = null): Int {
    return toPastelSnippetColorInt(text, forcedColor)
}

@androidx.compose.runtime.Composable
fun rememberSnippetBackgroundColor(): Color {
    return androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
}

@androidx.compose.runtime.Composable
fun rememberSnippetTextColor(): Color {
    return androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
}

fun getSnippetBackgroundColorInt(context: android.content.Context, isDark: Boolean = false): Int {
    val prefs = context.getSharedPreferences("snippets_prefs", android.content.Context.MODE_PRIVATE)
    val useDynamicColors = prefs.getBoolean("use_dynamic_colors", true)
    if (useDynamicColors && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val scheme = if (isDark) {
            androidx.compose.material3.dynamicDarkColorScheme(context)
        } else {
            androidx.compose.material3.dynamicLightColorScheme(context)
        }
        return scheme.primaryContainer.toArgb()
    }
    return (if (isDark) OrangePrimaryContainerDark else OrangePrimaryContainer).toArgb()
}

fun getSnippetTextColorInt(context: android.content.Context, isDark: Boolean = false): Int {
    val prefs = context.getSharedPreferences("snippets_prefs", android.content.Context.MODE_PRIVATE)
    val useDynamicColors = prefs.getBoolean("use_dynamic_colors", true)
    if (useDynamicColors && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val scheme = if (isDark) {
            androidx.compose.material3.dynamicDarkColorScheme(context)
        } else {
            androidx.compose.material3.dynamicLightColorScheme(context)
        }
        return scheme.onPrimaryContainer.toArgb()
    }
    return (if (isDark) OrangeOnPrimaryContainerDark else OrangeOnPrimaryContainer).toArgb()
}