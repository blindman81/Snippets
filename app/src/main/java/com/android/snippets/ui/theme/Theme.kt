package com.android.snippets.ui.theme

import android.app.Activity
import android.app.ActivityManager
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

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

    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = (view.context as? ComponentActivity) ?: (LocalContext.current as? ComponentActivity)
        if (activity != null && activity !is com.android.snippets.ui.MemoryActivity) {
            val surfaceContainerColor = sanitizedColorScheme.surfaceContainer
            LaunchedEffect(darkTheme, surfaceContainerColor) {
                val window = activity.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme

                val argbColor = surfaceContainerColor.toArgb()
                window.setBackgroundDrawable(ColorDrawable(argbColor))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val taskDesc = ActivityManager.TaskDescription.Builder()
                        .setBackgroundColor(argbColor)
                        .build()
                    activity.setTaskDescription(taskDesc)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val taskDesc = ActivityManager.TaskDescription.Builder()
                        .setPrimaryColor(argbColor)
                        .build()
                    activity.setTaskDescription(taskDesc)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    activity.overrideActivityTransition(
                        Activity.OVERRIDE_TRANSITION_CLOSE,
                        0,
                        0,
                        argbColor
                    )
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = sanitizedColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

fun ComponentActivity.configurePredictiveBackTransition(surfaceContainerColor: Int? = null) {
    val color = surfaceContainerColor ?: run {
        val typedValue = android.util.TypedValue()
        if (theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainer, typedValue, true)) {
            typedValue.data
        } else null
    } ?: return

    window.setBackgroundDrawable(ColorDrawable(color))

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val taskDesc = ActivityManager.TaskDescription.Builder()
            .setBackgroundColor(color)
            .build()
        setTaskDescription(taskDesc)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val taskDesc = ActivityManager.TaskDescription.Builder()
            .setPrimaryColor(color)
            .build()
        setTaskDescription(taskDesc)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_CLOSE,
            0,
            0,
            color
        )
    }
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
