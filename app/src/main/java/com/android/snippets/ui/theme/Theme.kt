package com.android.snippets.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color


private val OrangeLightColorScheme = lightColorScheme(
    primary                 = OrangePrimary,
    onPrimary               = OrangeOnPrimary,
    primaryContainer        = OrangePrimaryContainer,
    onPrimaryContainer      = OrangeOnPrimaryContainer,
    secondary               = OrangeSecondary,
    onSecondary             = OrangeOnSecondary,
    secondaryContainer      = OrangeSecondaryContainer,
    onSecondaryContainer    = OrangeOnSecondaryContainer,
    tertiary                = OrangeTertiary,
    onTertiary              = OrangeOnTertiary,
    tertiaryContainer       = OrangeTertiaryContainer,
    onTertiaryContainer     = OrangeOnTertiaryContainer,
    error                   = OrangeError,
    onError                 = OrangeOnError,
    errorContainer          = OrangeErrorContainer,
    onErrorContainer        = OrangeOnErrorContainer,
    background              = OrangeBackground,
    onBackground            = OrangeOnBackground,
    surface                 = OrangeSurface,
    onSurface               = OrangeOnSurface,
    surfaceVariant          = OrangeSurfaceVariant,
    onSurfaceVariant        = OrangeOnSurfaceVariant,
    outline                 = OrangeOutline,
    outlineVariant          = OrangeOutlineVariant,
    inverseSurface          = OrangeInverseSurface,
    inverseOnSurface        = OrangeInverseOnSurface,
    inversePrimary          = OrangeInversePrimary,
    surfaceContainer        = OrangeSurfaceContainer,
    surfaceContainerLow     = OrangeSurfaceContainerLow,
    surfaceContainerHigh    = OrangeSurfaceContainerHigh,
    surfaceContainerLowest  = OrangeSurfaceContainerLowest,
    surfaceContainerHighest = OrangeSurfaceContainerHighest,
)

private val OrangeDarkColorScheme = darkColorScheme(
    primary                 = OrangePrimaryDark,
    onPrimary               = OrangeOnPrimaryDark,
    primaryContainer        = OrangePrimaryContainerDark,
    onPrimaryContainer      = OrangeOnPrimaryContainerDark,
    secondary               = OrangeSecondaryDark,
    onSecondary             = OrangeOnSecondaryDark,
    secondaryContainer      = OrangeSecondaryContainerDark,
    onSecondaryContainer    = OrangeOnSecondaryContainerDark,
    tertiary                = OrangeTertiaryDark,
    onTertiary              = OrangeOnTertiaryDark,
    tertiaryContainer       = OrangeTertiaryContainerDark,
    onTertiaryContainer     = OrangeOnTertiaryContainerDark,
    error                   = OrangeErrorDark,
    onError                 = OrangeOnErrorDark,
    errorContainer          = OrangeErrorContainerDark,
    onErrorContainer        = OrangeOnErrorContainerDark,
    background              = OrangeBackgroundDark,
    onBackground            = OrangeOnBackgroundDark,
    surface                 = OrangeSurfaceDark,
    onSurface               = OrangeOnSurfaceDark,
    surfaceVariant          = OrangeSurfaceVariantDark,
    onSurfaceVariant        = OrangeOnSurfaceVariantDark,
    outline                 = OrangeOutlineDark,
    outlineVariant          = OrangeOutlineVariantDark,
    inverseSurface          = OrangeInverseSurfaceDark,
    inverseOnSurface        = OrangeInverseOnSurfaceDark,
    inversePrimary          = OrangeInversePrimaryDark,
    surfaceContainer        = OrangeSurfaceContainerDark,
    surfaceContainerLow     = OrangeSurfaceContainerLowDark,
    surfaceContainerHigh    = OrangeSurfaceContainerHighDark,
    surfaceContainerLowest  = OrangeSurfaceContainerLowestDark,
    surfaceContainerHighest = OrangeSurfaceContainerHighestDark,
)

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
        darkTheme -> OrangeDarkColorScheme
        else      -> OrangeLightColorScheme
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
        onPrimaryContainer = originalColorScheme.onSurface,
        onSecondaryContainer = originalColorScheme.onSurface,
        onTertiaryContainer = originalColorScheme.onSurface,


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