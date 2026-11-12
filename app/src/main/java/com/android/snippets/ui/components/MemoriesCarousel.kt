package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.android.snippets.model.Photo
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.util.Motion
import com.android.snippets.viewmodel.MemoryViewModel
import androidx.hilt.navigation.compose.hiltViewModel

private val GrayscaleColorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

private fun getSaturationColorFilter(saturation: Float): ColorFilter? = when {
    saturation >= 0.999f -> null
    saturation <= 0.001f -> GrayscaleColorFilter
    else -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturation) })
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MemoriesCarousel(
    currentTab: String,
    viewModel: MemoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    cardSize: Dp = 84.dp,
    lazyListState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    isCollapsed: Boolean = false,
    pageTabs: List<String> = emptyList()
) {
    val allMemories = viewModel.historyMemories
    val isVisible = !isCollapsed && allMemories.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = 280,
                easing = LinearOutSlowInEasing
            ),
            initialOffsetY = { -it / 2 }
        ) + expandVertically(
            animationSpec = tween(
                durationMillis = 280,
                easing = LinearOutSlowInEasing
            ),
            expandFrom = Alignment.Top,
            clip = false
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 280,
                easing = LinearOutSlowInEasing
            )
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = 240,
                easing = LinearOutSlowInEasing
            ),
            targetOffsetY = { -it / 2 }
        ) + shrinkVertically(
            animationSpec = tween(
                durationMillis = 240,
                easing = LinearOutSlowInEasing
            ),
            shrinkTowards = Alignment.Top,
            clip = false
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 240,
                easing = LinearOutSlowInEasing
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false }
    ) {
        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { clip = false },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(
                items = allMemories,
                key = { _, photo -> "lib_${photo.id}" }
            ) { itemIndex, photo ->
                MemoryCarouselCard(
                    photo = photo,
                    itemIndex = itemIndex,
                    tabMemories = allMemories,
                    viewModel = viewModel,
                    cardSize = cardSize,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MemoryCarouselCard(
    photo: Photo,
    itemIndex: Int,
    tabMemories: List<Photo>,
    viewModel: MemoryViewModel,
    cardSize: Dp,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?
) {
    val view = LocalView.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = if (isPressed) Motion.PressSpring else Motion.BouncySpring,
        label = "memory_card_scale"
    )

    val saturation by animateFloatAsState(
        targetValue = if (photo.isViewed) 0f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "memory_saturation"
    )
    val cardContentAlpha by animateFloatAsState(
        targetValue = if (photo.isViewed) 0.6f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "memory_alpha"
    )
    val dynamicColorFilter = getSaturationColorFilter(saturation)

    Box(
        modifier = Modifier
            .size(cardSize)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                clip = false
            }
    ) {
        Surface(
            shape = CookieShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxSize()
                .clip(CookieShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        val intent = android.content.Intent(context, com.android.snippets.ui.MemoryActivity::class.java).apply {
                            putExtra("memory_index", itemIndex)
                            putExtra("photo_id", photo.id)
                        }
                        context.startActivity(intent)
                    }
                )
        ) {
            val imageRequest = remember(photo.uriString) {
                ImageRequest.Builder(context)
                    .data(photo.uriString)
                    .crossfade(false)
                    .memoryCacheKey(photo.uriString)
                    .placeholderMemoryCacheKey(photo.uriString)
                    .build()
            }

            AsyncImage(
                model = imageRequest,
                contentDescription = "Memory ${itemIndex + 1}",
                contentScale = ContentScale.Crop,
                colorFilter = dynamicColorFilter,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(cardContentAlpha)
            )
        }
    }
}
