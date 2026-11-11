package com.android.snippets.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A modifier that reports the true bounding box of a rotated element to its parent layout (like FlowRow),
 * preventing rotated items from visually overlapping with adjacent items.
 */
fun Modifier.rotateWithBounds(degrees: Float): Modifier {
    if (degrees == 0f) return this
    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        
        val rad = Math.toRadians(degrees.toDouble())
        val absCos = Math.abs(Math.cos(rad)).toFloat()
        val absSin = Math.abs(Math.sin(rad)).toFloat()
        
        val boundingW = (placeable.width * absCos + placeable.height * absSin).toInt()
        val boundingH = (placeable.height * absCos + placeable.width * absSin).toInt()
        
        layout(boundingW, boundingH) {
            val x = (boundingW - placeable.width) / 2
            val y = (boundingH - placeable.height) / 2
            placeable.placeRelative(x, y)
        }
    }.graphicsLayer { rotationZ = degrees }
}

/**
 * Applies a smooth vertical fading edge gradient to the content, preventing harsh clipping
 * on scrollable containers and letting content peek through naturally.
 */
fun Modifier.fadingEdge(
    top: Dp = 0.dp,
    bottom: Dp = 0.dp
): Modifier {
    if (top <= 0.dp && bottom <= 0.dp) return this
    return this
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawWithContent {
            drawContent()
            val topPx = top.toPx()
            val bottomPx = bottom.toPx()
            if (topPx > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                        startY = 0f,
                        endY = topPx
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
            if (bottomPx > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        startY = size.height - bottomPx,
                        endY = size.height
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
        }
}

