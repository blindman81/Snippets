@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.android.snippets.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.ln.android.snippets.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val GoogleSans = FontFamily(
    Font(R.font.google_sans_flex, weight = FontWeight.Thin, variationSettings = FontVariation.Settings(FontVariation.weight(100), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.Light, variationSettings = FontVariation.Settings(FontVariation.weight(300), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.Black, variationSettings = FontVariation.Settings(FontVariation.weight(900), roundness(100f)))
)

val GoogleSansFlexTall = FontFamily(
    Font(R.font.google_sans_flex, weight = FontWeight.Thin, variationSettings = FontVariation.Settings(FontVariation.weight(100), FontVariation.width(25f), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.Light, variationSettings = FontVariation.Settings(FontVariation.weight(300), FontVariation.width(25f), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400), FontVariation.width(25f), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500), FontVariation.width(25f), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600), FontVariation.width(25f), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700), FontVariation.width(25f), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800), FontVariation.width(25f), roundness(100f))),
    Font(R.font.google_sans_flex, weight = FontWeight.Black, variationSettings = FontVariation.Settings(FontVariation.weight(900), FontVariation.width(25f), roundness(100f)))
)

val GoogleSansFlexHeavy = FontFamily(Font(resId = R.font.google_sans_flex, variationSettings = FontVariation.Settings(FontVariation.weight(1000), roundness(100f))))
val GoogleSansFlexWide = FontFamily(Font(resId = R.font.google_sans_flex, variationSettings = FontVariation.Settings(FontVariation.width(151f), roundness(100f))))
val GoogleSansFlexOptical = FontFamily(Font(resId = R.font.google_sans_flex, variationSettings = FontVariation.Settings(FontVariation.Setting("opsz", 144f), roundness(100f))))
val GoogleSansFlexSlant = FontFamily(Font(resId = R.font.google_sans_flex, variationSettings = FontVariation.Settings(FontVariation.slant(-10f), roundness(100f))))
val GoogleSansFlexGrade = FontFamily(Font(resId = R.font.google_sans_flex, variationSettings = FontVariation.Settings(grade(100f), roundness(100f))))
val GoogleSansFlexRound = FontFamily(Font(resId = R.font.google_sans_flex, variationSettings = FontVariation.Settings(roundness(100f))))

/**
 * Creates a FontVariation setting for the Roundness (ROND) axis of Google Sans Flex.
 * @param value A float between 0f (sharp, formal geometric) and 100f (fully rounded, friendly).
 */
fun roundness(value: Float): FontVariation.Setting {
    require(value in 0f..100f) { "Roundness (ROND) value must be between 0f and 100f" }
    return FontVariation.Setting("ROND", value)
}

/**
 * Creates a FontVariation setting for the Grade (GRAD) axis of Google Sans Flex.
 * @param value A float between 0f (normal) and 100f (bold without layout shift).
 */
fun grade(value: Float): FontVariation.Setting {
    require(value in 0f..100f) { "Grade (GRAD) value must be between 0f and 100f" }
    return FontVariation.Setting("GRAD", value)
}

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
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
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
    fontWeight = FontWeight.Bold
)

val titleMediumEmphasized = Typography.titleMedium.copy(
    fontFamily = GoogleSans,
    fontWeight = FontWeight.Bold
)


