package com.android.snippets.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Curated gradient palettes and configuration defaults for animated UI elements.
 */
object AnimatedGradientDefaults {
    const val DefaultDurationMillis = 4000
    const val FastDurationMillis = 2500
    const val SlowDurationMillis = 6500

    @Composable
    fun themeGradient(): List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primary
    )

    @Composable
    fun vibrantGradient(): List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        Color(0xFFFF5722),
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF8E24AA),
        MaterialTheme.colorScheme.primary
    )

    @Composable
    fun sunsetGradient(): List<Color> = listOf(
        Color(0xFFFF5E36),
        Color(0xFFFF9800),
        Color(0xFFE91E63),
        Color(0xFFFF5E36)
    )

    @Composable
    fun oceanGradient(): List<Color> = listOf(
        Color(0xFF00B4DB),
        Color(0xFF0083B0),
        Color(0xFF4FACFE),
        Color(0xFF00B4DB)
    )

    @Composable
    fun auroraGradient(): List<Color> = listOf(
        Color(0xFF00F260),
        Color(0xFF0575E6),
        Color(0xFF7B1FA2),
        Color(0xFF00F260)
    )

    @Composable
    fun subtleSurfaceGradient(): List<Color> = listOf(
        MaterialTheme.colorScheme.surfaceContainerHigh,
        MaterialTheme.colorScheme.surfaceContainerHighest,
        MaterialTheme.colorScheme.surfaceContainer,
        MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

/**
 * Creates and remembers an animated linear gradient [Brush] that flows continuously.
 */
@Composable
fun rememberAnimatedGradientBrush(
    colors: List<Color> = AnimatedGradientDefaults.themeGradient(),
    durationMillis: Int = AnimatedGradientDefaults.DefaultDurationMillis,
    angleInDegrees: Float = 45f
): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_transition")
    val offsetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_offset"
    )

    val gradientColors = remember(colors) {
        if (colors.size > 1 && colors.first() != colors.last()) {
            colors + colors.first()
        } else {
            colors
        }
    }

    val rad = Math.toRadians(angleInDegrees.toDouble())
    val cos = kotlin.math.cos(rad).toFloat()
    val sin = kotlin.math.sin(rad).toFloat()

    val spread = 800f
    val startX = (offsetProgress * spread) * cos
    val startY = (offsetProgress * spread) * sin
    val endX = startX + spread * cos
    val endY = startY + spread * sin

    return remember(startX, startY, endX, endY, gradientColors) {
        Brush.linearGradient(
            colors = gradientColors,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            tileMode = TileMode.Repeated
        )
    }
}

/**
 * Modifier to apply a smoothly animated gradient background.
 */
fun Modifier.animatedGradientBackground(
    colors: List<Color>,
    shape: Shape = RectangleShape,
    durationMillis: Int = AnimatedGradientDefaults.DefaultDurationMillis,
    angleInDegrees: Float = 45f,
    alpha: Float = 1f
): Modifier = composed {
    val brush = rememberAnimatedGradientBrush(
        colors = colors,
        durationMillis = durationMillis,
        angleInDegrees = angleInDegrees
    )
    this
        .clip(shape)
        .drawBehind {
            drawRect(
                brush = brush,
                alpha = alpha
            )
        }
}

/**
 * Modifier to apply a smoothly animated gradient border.
 */
fun Modifier.animatedGradientBorder(
    borderWidth: Dp = 1.5.dp,
    colors: List<Color>,
    shape: Shape = CircleShape,
    durationMillis: Int = AnimatedGradientDefaults.DefaultDurationMillis,
    angleInDegrees: Float = 45f
): Modifier = composed {
    val brush = rememberAnimatedGradientBrush(
        colors = colors,
        durationMillis = durationMillis,
        angleInDegrees = angleInDegrees
    )
    this.border(
        width = borderWidth,
        brush = brush,
        shape = shape
    )
}

/**
 * A Surface container with an animated gradient background.
 */
@Composable
fun AnimatedGradientSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: List<Color> = AnimatedGradientDefaults.themeGradient(),
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    durationMillis: Int = AnimatedGradientDefaults.DefaultDurationMillis,
    angleInDegrees: Float = 45f,
    content: @Composable () -> Unit
) {
    val brush = rememberAnimatedGradientBrush(
        colors = colors,
        durationMillis = durationMillis,
        angleInDegrees = angleInDegrees
    )
    Surface(
        modifier = modifier.clip(shape),
        shape = shape,
        color = Color.Transparent,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border
    ) {
        Box(
            modifier = Modifier
                .background(brush)
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}
