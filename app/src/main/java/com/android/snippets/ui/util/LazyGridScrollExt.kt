package com.android.snippets.ui.util

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState

suspend fun LazyStaggeredGridState.smoothScrollToItemSlow(
    targetIndex: Int,
    targetOffsetPx: Int = 24,
    durationMillis: Int = 700
) {
    val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == targetIndex }
    if (visibleItem != null) {
        val delta = (visibleItem.offset.y - targetOffsetPx).toFloat()
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
