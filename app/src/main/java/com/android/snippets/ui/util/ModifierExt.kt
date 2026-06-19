package com.android.snippets.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.graphicsLayer

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
