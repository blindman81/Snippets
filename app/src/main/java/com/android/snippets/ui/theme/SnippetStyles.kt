package com.android.snippets.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import com.android.snippets.viewmodel.SnippetStyle

@Composable
fun getSnippetTextStyle(
    style: SnippetStyle = SnippetStyle.Default,
    base: TextStyle,
    isCloud: Boolean = false
): TextStyle {
    return base.copy(
        fontFamily = GoogleSansFlexRound,
        fontWeight = FontWeight.SemiBold,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle.Default.copy(mode = LineHeightStyle.Mode.PerLine)
    )
}
