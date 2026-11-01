package com.android.snippets.ui.util

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState

suspend fun LazyStaggeredGridState.smoothScrollToItemSlow(
    targetIndex: Int,
    targetOffsetPx: Int = 24,
    durationMillis: Int = 600
) {
    val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == targetIndex }
    if (visibleItem != null) {
        val itemTop = visibleItem.offset.y
        val itemBottom = itemTop + visibleItem.size.height
        val viewportTop = targetOffsetPx
        val viewportBottom = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding

        // Only scroll if the item is partially or fully out of the visible viewport
        val delta = when {
            itemTop < viewportTop -> (itemTop - viewportTop).toFloat()
            itemBottom > viewportBottom && itemTop > viewportTop -> {
                (itemBottom - viewportBottom).coerceAtMost(itemTop - viewportTop).toFloat()
            }
            else -> 0f
        }

        if (kotlin.math.abs(delta) > 4f) {
            animateScrollBy(
                value = delta,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing
                )
            )
        }
    } else {
        animateScrollToItem(targetIndex, scrollOffset = -targetOffsetPx)
    }
}
