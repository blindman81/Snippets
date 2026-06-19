package com.android.snippets.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
