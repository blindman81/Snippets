package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Standard Jetpack Compose Vertical Scrollbar component.
 * Spec:
 * - thickness: 8.dp (Compose default)
 * - shape: RoundedCornerShape(4.dp)
 * - minThumbLength: 36.dp
 * - works strictly through drag (tapping does not trigger jumps to top or bottom)
 * - smooth color transition between idle and active states
 */
@Composable
fun VerticalScrollbar(
    state: LazyStaggeredGridState,
    totalItemsCount: Int,
    modifier: Modifier = Modifier,
    thickness: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(4.dp),
    minThumbLength: Dp = 36.dp,
    thumbColorIdle: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
    thumbColorActive: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
) {
    if (totalItemsCount <= 0) return

    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    var isDragging by remember { mutableStateOf(false) }
    var dragThumbOffsetY by remember { mutableFloatStateOf(0f) }
    var scrollJob by remember { mutableStateOf<Job?>(null) }

    val thumbColor by animateColorAsState(
        targetValue = if (isDragging) thumbColorActive else thumbColorIdle,
        animationSpec = tween(durationMillis = 200),
        label = "scrollbarThumbColor"
    )

    BoxWithConstraints(
        modifier = modifier.width(thickness + 12.dp)
    ) {
        val trackHeightPx = constraints.maxHeight.toFloat()
        if (trackHeightPx <= 0f) return@BoxWithConstraints

        val density = LocalDensity.current
        val minThumbLengthPx = with(density) { minThumbLength.toPx() }
        val thicknessPx = with(density) { thickness.toPx() }

        val layoutInfo = state.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo

        val isScrollable = totalItemsCount > visibleItems.size ||
            (visibleItems.isNotEmpty() && (visibleItems.last().offset.y + visibleItems.last().size.height) > trackHeightPx)

        if (!isScrollable) return@BoxWithConstraints

        val avgItemHeight = if (visibleItems.isNotEmpty()) {
            visibleItems.sumOf { it.size.height }.toFloat() / visibleItems.size
        } else {
            100f
        }
        val estimatedTotalHeight = (avgItemHeight * totalItemsCount).coerceAtLeast(trackHeightPx)
        val thumbHeightPx = (trackHeightPx * (trackHeightPx / estimatedTotalHeight))
            .coerceIn(minThumbLengthPx, trackHeightPx)

        val maxThumbOffset = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f)

        // When not dragging, compute thumb position from state
        val firstItem = visibleItems.firstOrNull()
        val firstItemIndex = firstItem?.index ?: state.firstVisibleItemIndex
        val firstItemOffset = state.firstVisibleItemScrollOffset.toFloat()
        val firstItemHeight = (firstItem?.size?.height?.toFloat() ?: avgItemHeight).coerceAtLeast(1f)

        val scrollProgress = ((firstItemIndex + (firstItemOffset / firstItemHeight)) / (totalItemsCount - 1).coerceAtLeast(1))
            .coerceIn(0f, 1f)

        val computedThumbOffsetY = scrollProgress * maxThumbOffset

        val currentThumbOffsetY = if (isDragging) {
            dragThumbOffsetY.coerceIn(0f, maxThumbOffset)
        } else {
            computedThumbOffsetY
        }

        fun scrollToFraction(fraction: Float) {
            val clampedFraction = fraction.coerceIn(0f, 1f)
            val targetIndex = (clampedFraction * (totalItemsCount - 1)).toInt().coerceIn(0, totalItemsCount - 1)
            scrollJob?.cancel()
            scrollJob = coroutineScope.launch {
                state.scrollToItem(targetIndex)
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(totalItemsCount, trackHeightPx, maxThumbOffset, thumbHeightPx) {
                    var dragOffsetWithinThumb = thumbHeightPx / 2f
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
                            val currentTop = currentThumbOffsetY
                            val currentBottom = currentTop + thumbHeightPx
                            dragOffsetWithinThumb = if (offset.y in currentTop..currentBottom) {
                                offset.y - currentTop
                            } else {
                                thumbHeightPx / 2f
                            }
                            val initialThumbY = (offset.y - dragOffsetWithinThumb).coerceIn(0f, maxThumbOffset)
                            dragThumbOffsetY = initialThumbY
                            if (maxThumbOffset > 0f) {
                                scrollToFraction(initialThumbY / maxThumbOffset)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            val newThumbY = (change.position.y - dragOffsetWithinThumb).coerceIn(0f, maxThumbOffset)
                            dragThumbOffsetY = newThumbY
                            if (maxThumbOffset > 0f) {
                                val fraction = newThumbY / maxThumbOffset
                                scrollToFraction(fraction)
                            }
                        }
                    )
                }
        ) {
            val x = size.width - thicknessPx
            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(x = x, y = currentThumbOffsetY),
                size = Size(width = thicknessPx, height = thumbHeightPx),
                cornerRadius = CornerRadius(thicknessPx / 2f, thicknessPx / 2f)
            )
        }
    }
}
