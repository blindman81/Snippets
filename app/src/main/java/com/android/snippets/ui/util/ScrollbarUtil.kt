package com.android.snippets.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import android.view.HapticFeedbackConstants

@Composable
fun VerticalDraggableScrollbar(
    state: LazyStaggeredGridState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val scrollJobHolder = remember { arrayOf<kotlinx.coroutines.Job?>(null) }
    val view = androidx.compose.ui.platform.LocalView.current
    
    val alpha by animateFloatAsState(
        targetValue = if (state.isScrollInProgress || isDragging) 1f else 0f,
        label = "scrollbar_alpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(36.dp) // Wider invisible hit area for easier grabbing
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragY = offset.y
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                        
                        val totalItemsCount = state.layoutInfo.totalItemsCount
                        if (totalItemsCount > 1) {
                            val ratio = (dragY / size.height).coerceIn(0f, 1f)
                            val targetIndex = (ratio * (totalItemsCount - 1)).toInt()
                            scrollJobHolder[0]?.cancel()
                            scrollJobHolder[0] = coroutineScope.launch {
                                state.scrollToItem(targetIndex)
                            }
                        }
                    },
                    onDragEnd = { 
                        isDragging = false 
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    },
                    onDragCancel = { isDragging = false },
                    onVerticalDrag = { change, dragAmount ->
                        dragY += dragAmount
                        val totalItemsCount = state.layoutInfo.totalItemsCount
                        if (totalItemsCount > 1) {
                            val ratio = (dragY / size.height).coerceIn(0f, 1f)
                            val targetIndex = (ratio * (totalItemsCount - 1)).toInt()
                            scrollJobHolder[0]?.cancel()
                            scrollJobHolder[0] = coroutineScope.launch {
                                state.scrollToItem(targetIndex)
                            }
                        }
                        change.consume()
                    }
                )
            }
    ) {
        val thumbHeight = 48.dp
        
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 4.dp) // Keep it tight to the edge
                .offset {
                    val totalItemsCount = state.layoutInfo.totalItemsCount
                    if (totalItemsCount <= 1) return@offset androidx.compose.ui.unit.IntOffset.Zero
                    
                    val firstVisible = state.firstVisibleItemIndex
                    val scrollProgress = firstVisible.toFloat() / (totalItemsCount - 1).toFloat()
                    val maxOffset = constraints.maxHeight - thumbHeight.toPx()
                    val thumbOffset = maxOffset * scrollProgress
                    
                    androidx.compose.ui.unit.IntOffset(0, thumbOffset.toInt())
                }
                .size(width = 8.dp, height = thumbHeight)
                .alpha(alpha)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

// Minimal stub to avoid breaking builds while refactoring LibraryScreen
@Composable
fun Modifier.drawStaggeredScrollbar(state: LazyStaggeredGridState): Modifier = this

@Composable
fun Modifier.drawListScrollbar(state: LazyListState): Modifier = this

@Composable
fun VerticalScrollbar(
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier,
    trackWidth: androidx.compose.ui.unit.Dp = 6.dp,
    thumbMinHeight: androidx.compose.ui.unit.Dp = 24.dp,
    trackColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    thumbColor: Color = MaterialTheme.colorScheme.primary
) {
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val view = androidx.compose.ui.platform.LocalView.current

    val maxScroll = scrollState.maxValue
    val canScroll = maxScroll > 0

    val scrollFraction = if (canScroll) {
        (scrollState.value.toFloat() / maxScroll.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val alpha by animateFloatAsState(
        targetValue = if (canScroll) 1f else 0.35f,
        label = "scrollbar_alpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .width(trackWidth.coerceAtLeast(24.dp))
            .pointerInput(canScroll, maxScroll) {
                if (!canScroll) return@pointerInput
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragY = offset.y
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        val totalTrack = size.height
                        val progress = (offset.y / totalTrack).coerceIn(0f, 1f)
                        coroutineScope.launch {
                            scrollState.scrollTo((progress * maxScroll).toInt())
                        }
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onVerticalDrag = { change, dragAmount ->
                        dragY += dragAmount
                        val totalTrack = size.height
                        val progress = (dragY / totalTrack).coerceIn(0f, 1f)
                        coroutineScope.launch {
                            scrollState.scrollTo((progress * maxScroll).toInt())
                        }
                        change.consume()
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        val totalTrackHeight = maxHeight
        val density = androidx.compose.ui.platform.LocalDensity.current
        val totalTrackHeightPx = with(density) { totalTrackHeight.toPx() }
        val thumbMinHeightPx = with(density) { thumbMinHeight.toPx() }

        val thumbHeightPx = if (!canScroll) {
            totalTrackHeightPx
        } else {
            val viewportRatio = (totalTrackHeightPx / (totalTrackHeightPx + maxScroll)).coerceIn(0.2f, 0.75f)
            (totalTrackHeightPx * viewportRatio).coerceAtLeast(thumbMinHeightPx)
        }
        val thumbHeightDp = with(density) { thumbHeightPx.toDp() }
        val maxOffsetPx = (totalTrackHeightPx - thumbHeightPx).coerceAtLeast(0f)
        val thumbOffsetPx = maxOffsetPx * scrollFraction
        val thumbOffsetDp = with(density) { thumbOffsetPx.toDp() }

        // Track (background)
        Box(
            modifier = Modifier
                .width(trackWidth)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(trackColor)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        )

        // Thumb
        if (canScroll) {
            Box(
                modifier = Modifier
                    .offset(y = thumbOffsetDp)
                    .width(trackWidth)
                    .height(thumbHeightDp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(thumbColor)
            )
        }
    }
}
