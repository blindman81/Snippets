@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.android.snippets.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.ln.android.snippets.R

/**
 * Creates a FontVariation setting for the Roundness (ROND) axis of Google Sans Flex.
 * @param value A float between 0f (sharp, formal geometric) and 100f (fully rounded, friendly).
 */
fun roundness(value: Float): FontVariation.Setting {
    require(value in 0f..100f) { "Roundness (ROND) value must be between 0f and 100f" }
    return FontVariation.Setting("ROND", value)
}

val baseVariation = FontVariation.Settings(roundness(100f))
val wideBaseVariation = FontVariation.Settings(FontVariation.width(151f), roundness(100f))

val GoogleSans = FontFamily(
    Font(R.font.google_sans_flex, weight = FontWeight.Thin, variationSettings = baseVariation.merge(FontVariation.weight(100))),
    Font(R.font.google_sans_flex, weight = FontWeight.Light, variationSettings = baseVariation.merge(FontVariation.weight(300))),
    Font(R.font.google_sans_flex, weight = FontWeight.Normal, variationSettings = baseVariation.merge(FontVariation.weight(400))),
    Font(R.font.google_sans_flex, weight = FontWeight.Medium, variationSettings = baseVariation.merge(FontVariation.weight(500))),
    Font(R.font.google_sans_flex, weight = FontWeight.SemiBold, variationSettings = baseVariation.merge(FontVariation.weight(600))),
    Font(R.font.google_sans_flex, weight = FontWeight.Bold, variationSettings = baseVariation.merge(FontVariation.weight(700))),
    Font(R.font.google_sans_flex, weight = FontWeight.ExtraBold, variationSettings = baseVariation.merge(FontVariation.weight(800))),
    Font(R.font.google_sans_flex, weight = FontWeight.Black, variationSettings = baseVariation.merge(FontVariation.weight(900)))
)

val GoogleSansFlexRound = GoogleSans

val GoogleSansFlexWide = FontFamily(
    Font(R.font.google_sans_flex, weight = FontWeight.Thin, variationSettings = wideBaseVariation.merge(FontVariation.weight(100))),
    Font(R.font.google_sans_flex, weight = FontWeight.Light, variationSettings = wideBaseVariation.merge(FontVariation.weight(300))),
    Font(R.font.google_sans_flex, weight = FontWeight.Normal, variationSettings = wideBaseVariation.merge(FontVariation.weight(400))),
    Font(R.font.google_sans_flex, weight = FontWeight.Medium, variationSettings = wideBaseVariation.merge(FontVariation.weight(500))),
    Font(R.font.google_sans_flex, weight = FontWeight.SemiBold, variationSettings = wideBaseVariation.merge(FontVariation.weight(600))),
    Font(R.font.google_sans_flex, weight = FontWeight.Bold, variationSettings = wideBaseVariation.merge(FontVariation.weight(700))),
    Font(R.font.google_sans_flex, weight = FontWeight.ExtraBold, variationSettings = wideBaseVariation.merge(FontVariation.weight(800))),
    Font(R.font.google_sans_flex, weight = FontWeight.Black, variationSettings = wideBaseVariation.merge(FontVariation.weight(900)))
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = LineHeightStyle.Default.copy(mode = LineHeightStyle.Mode.PerLine)
    ),
    bodyMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        lineHeightStyle = LineHeightStyle.Default.copy(mode = LineHeightStyle.Mode.PerLine)
    ),
    bodySmall = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        lineHeightStyle = LineHeightStyle.Default.copy(mode = LineHeightStyle.Mode.PerLine)
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

val titleEmphasized = Typography.titleLarge.copy(
    fontFamily = GoogleSans,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    fontWeight = FontWeight.SemiBold
)
