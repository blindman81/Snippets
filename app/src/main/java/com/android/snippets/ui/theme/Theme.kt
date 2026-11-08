package com.android.snippets.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color


private val LightColorScheme = lightColorScheme()
private val DarkColorScheme = darkColorScheme()

@Composable
fun SnippetsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ (API 31+)
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val originalColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val textBase = if (darkTheme) {
        originalColorScheme.onSurface
    } else {
        Color.Black
    }

    val sanitizedColorScheme = originalColorScheme.copy(
        onSurface = originalColorScheme.onSurface,
        onBackground = originalColorScheme.onSurface,
        onSurfaceVariant = originalColorScheme.onSurfaceVariant,
        onPrimaryContainer = originalColorScheme.onPrimaryContainer,
        onSecondaryContainer = originalColorScheme.onSecondaryContainer,
        onTertiaryContainer = originalColorScheme.onTertiaryContainer,


        onPrimary = if (darkTheme) Color.Black else Color.White,
        onSecondary = if (darkTheme) Color.Black else Color.White,
        onTertiary = if (darkTheme) Color.Black else Color.White,
        outline = originalColorScheme.onSurface.copy(alpha = 0.6f),
        outlineVariant = originalColorScheme.outlineVariant
    )

    MaterialTheme(
        colorScheme = sanitizedColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

fun getAppColorScheme(context: android.content.Context, isDark: Boolean = false): ColorScheme {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }
}
