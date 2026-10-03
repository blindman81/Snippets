package com.android.snippets.ui.util

import android.os.Build
import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.blur.BlurRadiusSpec
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies Compose's progressive blur only on Android 13+ (API 33+).
 * On Android versions below 13, this modifier is a no-op (no blur).
 */
fun Modifier.progressiveBlur(
    startRadius: Dp = 4.dp,
    endRadius: Dp = 24.dp,
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle
): Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    this.blur(
        BlurRadiusSpec.verticalGradient(startRadius = startRadius, endRadius = endRadius),
        edgeTreatment = edgeTreatment
    )
} else {
    this
}




/**
 * A modifier that reports the true bounding box of a rotated element to its parent layout (like FlowRow),
 * preventing rotated items from visually overlapping with adjacent items.
 */
fun Modifier.rotateWithBounds(degrees: Float) = layout { measurable, constraints ->
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
