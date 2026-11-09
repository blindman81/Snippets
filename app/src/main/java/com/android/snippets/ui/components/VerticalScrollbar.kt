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
    var activeThumbHeightPx by remember { mutableFloatStateOf(0f) }
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

        val isScrollable = state.canScrollForward || state.canScrollBackward ||
            totalItemsCount > visibleItems.size ||
            (visibleItems.isNotEmpty() && (visibleItems.last().offset.y + visibleItems.last().size.height) > trackHeightPx)

        if (!isScrollable) return@BoxWithConstraints

        val avgItemHeight = if (visibleItems.isNotEmpty()) {
            visibleItems.sumOf { it.size.height }.toFloat() / visibleItems.size
        } else {
            100f
        }
        val estimatedTotalHeight = (avgItemHeight * totalItemsCount).coerceAtLeast(trackHeightPx)
        val calculatedThumbHeightPx = (trackHeightPx * (trackHeightPx / estimatedTotalHeight))
            .coerceIn(minThumbLengthPx, trackHeightPx)

        val thumbHeightPx = if (isDragging && activeThumbHeightPx > 0f) activeThumbHeightPx else calculatedThumbHeightPx
        val maxThumbOffset = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f)

        // When not dragging, compute thumb position from state
        val firstItem = visibleItems.firstOrNull()
        val firstItemIndex = firstItem?.index ?: state.firstVisibleItemIndex
        val firstItemOffset = state.firstVisibleItemScrollOffset.toFloat()
        val firstItemHeight = (firstItem?.size?.height?.toFloat() ?: avgItemHeight).coerceAtLeast(1f)

        val maxScrollIndex = (totalItemsCount - visibleItems.size).coerceAtLeast(1).toFloat()
        val rawProgress = (firstItemIndex + (firstItemOffset / firstItemHeight)) / maxScrollIndex
        val scrollProgress = when {
            !state.canScrollBackward -> 0f
            !state.canScrollForward -> 1f
            else -> rawProgress.coerceIn(0f, 1f)
        }

        val computedThumbOffsetY = scrollProgress * maxThumbOffset

        val currentThumbOffsetY = if (isDragging) {
            dragThumbOffsetY.coerceIn(0f, maxThumbOffset)
        } else {
            computedThumbOffsetY
        }

        val currentTotalItemsCount by rememberUpdatedState(totalItemsCount)
        val currentTrackHeightPx by rememberUpdatedState(trackHeightPx)
        val currentCalculatedThumbHeightPx by rememberUpdatedState(calculatedThumbHeightPx)
        val currentComputedThumbOffsetY by rememberUpdatedState(computedThumbOffsetY)
        val currentVisibleItemsCount by rememberUpdatedState(visibleItems.size)
        val currentAvgItemHeight by rememberUpdatedState(avgItemHeight)

        fun scrollToFraction(fraction: Float) {
            val clampedFraction = fraction.coerceIn(0f, 1f)
            val maxScrollable = (currentTotalItemsCount - currentVisibleItemsCount).coerceAtLeast(1)
            scrollJob?.cancel()
            scrollJob = coroutineScope.launch {
                if (clampedFraction <= 0.001f) {
                    state.scrollToItem(0, 0)
                } else if (clampedFraction >= 0.999f) {
                    state.scrollToItem(currentTotalItemsCount - 1, 0)
                } else {
                    val exactIndex = clampedFraction * maxScrollable
                    val targetIndex = exactIndex.toInt().coerceIn(0, currentTotalItemsCount - 1)
                    val itemFraction = exactIndex - targetIndex
                    val targetOffsetPx = (itemFraction * currentAvgItemHeight).toInt()
                    state.scrollToItem(targetIndex, targetOffsetPx)
                }
            }
        }

        val currentScrollToFraction by rememberUpdatedState(::scrollToFraction)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state) {
                    var dragOffsetWithinThumb = thumbHeightPx / 2f
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
                            activeThumbHeightPx = currentCalculatedThumbHeightPx
                            val maxOffset = (currentTrackHeightPx - activeThumbHeightPx).coerceAtLeast(0f)
                            val currentTop = currentComputedThumbOffsetY
                            val currentBottom = currentTop + activeThumbHeightPx
                            dragOffsetWithinThumb = if (offset.y in currentTop..currentBottom) {
                                offset.y - currentTop
                            } else {
                                activeThumbHeightPx / 2f
                            }
                            val initialThumbY = (offset.y - dragOffsetWithinThumb).coerceIn(0f, maxOffset)
                            dragThumbOffsetY = initialThumbY
                            if (maxOffset > 0f) {
                                currentScrollToFraction(initialThumbY / maxOffset)
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                scrollJob?.join()
                                isDragging = false
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            val maxOffset = (currentTrackHeightPx - activeThumbHeightPx).coerceAtLeast(0f)
                            val newThumbY = (change.position.y - dragOffsetWithinThumb).coerceIn(0f, maxOffset)
                            dragThumbOffsetY = newThumbY
                            if (maxOffset > 0f) {
                                val fraction = newThumbY / maxOffset
                                currentScrollToFraction(fraction)
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
