package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.snippets.model.Photo
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.util.Motion
import com.android.snippets.viewmodel.SnippetsViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MemoriesCarousel(
    currentTab: String,
    viewModel: SnippetsViewModel,
    modifier: Modifier = Modifier,
    cardSize: Dp = 84.dp,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val allMemories = viewModel.historyMemories
    val tabMemories = remember(allMemories, currentTab) {
        when (currentTab) {
            "Library" -> allMemories
            "Favorites" -> allMemories.filter { it.isFavorite }
            else -> allMemories.filter { it.collections.contains(currentTab) }
        }
    }

    val isVisible = tabMemories.isNotEmpty()
    val lazyListState = viewModel.carouselLazyListState

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)),
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
                items = tabMemories,
                key = { _, photo -> photo.id }
            ) { itemIndex, photo ->
                MemoryCarouselCard(
                    photo = photo,
                    itemIndex = itemIndex,
                    tabMemories = tabMemories,
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
    viewModel: SnippetsViewModel,
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
        animationSpec = tween(durationMillis = 500, delayMillis = 200),
        label = "memory_saturation"
    )
    val cardContentAlpha by animateFloatAsState(
        targetValue = if (photo.isViewed) 0.6f else 1f,
        animationSpec = tween(durationMillis = 500, delayMillis = 200),
        label = "memory_alpha"
    )
    val colorMatrix = remember { ColorMatrix() }
    val dynamicColorFilter = remember(saturation) {
        if (saturation < 0.99f) {
            colorMatrix.setToSaturation(saturation)
            ColorFilter.colorMatrix(colorMatrix)
        } else null
    }

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
                .then(
                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                rememberSharedContentState(key = "memory_${photo.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ ->
                                    spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                },
                                resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.Crop),
                                clipInOverlayDuringTransition = OverlayClip(CookieShape)
                            )
                        }
                    } else Modifier
                )
                .clip(CookieShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        viewModel.openMemory(tabMemories, itemIndex)
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
