package com.android.snippets.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import com.android.snippets.viewmodel.SnippetStyle

@Composable
fun getSnippetTextStyle(
    style: SnippetStyle = SnippetStyle.Default,
    base: TextStyle,
    isCloud: Boolean = false
): TextStyle {
    return base.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
}
