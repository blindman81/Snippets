package com.android.snippets.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

// ── Material You Monet Snippet Styling ──────────────────────────────────────
// Snippets dynamically adopt Monet dynamic colors from MaterialTheme (secondaryContainer / onSecondaryContainer)

@androidx.compose.runtime.Composable
fun rememberSnippetBackgroundColor(): Color {
    return androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer
}

@androidx.compose.runtime.Composable
fun rememberSnippetTextColor(): Color {
    return androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer
}

fun getSnippetBackgroundColorInt(context: android.content.Context, isDark: Boolean = false): Int {
    return getAppColorScheme(context, isDark).secondaryContainer.toArgb()
}

fun getSnippetTextColorInt(context: android.content.Context, isDark: Boolean = false): Int {
    return getAppColorScheme(context, isDark).onSecondaryContainer.toArgb()
}