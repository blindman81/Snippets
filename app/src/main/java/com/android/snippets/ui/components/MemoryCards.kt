package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.android.snippets.model.Photo
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.viewmodel.Screen
import com.android.snippets.viewmodel.SnippetsViewModel

private val GrayscaleColorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

private fun getSaturationColorFilter(saturation: Float): ColorFilter? = when {
    saturation >= 0.999f -> null
    saturation <= 0.001f -> GrayscaleColorFilter
    else -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturation) })
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MemoryAvatar(
    photo: Photo, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(80.dp),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    shouldSpin: Boolean = true
) {
    val saturation by animateFloatAsState(
        targetValue = if (photo.isViewed) 0f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "saturation"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (photo.isViewed) 0.5f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "alpha"
    )

    val rotation by if (animatedVisibilityScope != null) {
        animatedVisibilityScope.transition.animateFloat(
            label = "memory_spin",
            transitionSpec = { tween(800, easing = FastOutSlowInEasing) }
        ) { state ->
            if (shouldSpin) {
                when (state) {
                    EnterExitState.PreEnter -> 360f
                    EnterExitState.Visible -> 0f
                    EnterExitState.PostExit -> 0f
                }
            } else 0f
        }
    } else {
        remember { mutableStateOf(0f) }
    }

    val MorphShapesList = remember { listOf(CookieShape, CircleShape) }
    val shapeIndex = remember(photo.id) { kotlin.math.abs(photo.id.hashCode()) % MorphShapesList.size }
    val avatarShape = MorphShapesList[shapeIndex]

    Surface(
        modifier = modifier
            .alpha(animatedAlpha)
            .graphicsLayer { rotationZ = rotation }
            .then(
                if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedBounds(
                            rememberSharedContentState(key = "memory_${photo.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> spring(dampingRatio = 0.8f, stiffness = 500f) },
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                        )
                    }
                } else Modifier
            ),
        shape = avatarShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer),
        onClick = onClick
    ) {
        AsyncImage(
            model = photo.uriString,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            colorFilter = getSaturationColorFilter(saturation),
            modifier = Modifier.fillMaxSize().padding(2.dp)
        )
    }
}

@Composable
fun MemoryMoreButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier.size(80.dp),
    isSpinning: Boolean = true,
    isPointing: Boolean = false,
    unviewedCount: Int = 0,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val isForwardArrow = icon == Icons.AutoMirrored.Rounded.ArrowForward
    val isBackArrow = icon == Icons.AutoMirrored.Rounded.ArrowBack
    val isDownArrow = icon == Icons.Rounded.KeyboardArrowDown
    val isUpArrow = icon == Icons.Rounded.KeyboardArrowUp
    val isDirectionalArrow = isForwardArrow || isBackArrow || isDownArrow || isUpArrow

    val animatedDirectionalRotation by animateFloatAsState(
        targetValue = if (isUpArrow) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "arrow_rotation"
    )

    val buttonShape = when {
        isForwardArrow -> CircleShape
        isBackArrow -> CircleShape
        isDownArrow || isUpArrow -> CircleShape
        else -> CookieShape
    }
    var isHolding by remember { mutableStateOf(false) }
    var isTapped by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    val animScaleX = remember { Animatable(1f) }
    val animScaleY = remember { Animatable(1f) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            animScaleX.animateTo(0.92f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 1200f))
            animScaleY.animateTo(0.92f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 1200f))
        } else {
            animScaleX.animateTo(1f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 1200f))
            animScaleY.animateTo(1f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 1200f))
        }
    }

    val pointerOffset by rememberInfiniteTransition(label = "pointer").animateFloat(
        initialValue = 0f,
        targetValue = if (isPointing) 6f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pointer_offset"
    )

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (unviewedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        animationSpec = tween(durationMillis = 500),
        label = "arrow_background_color"
    )

    LaunchedEffect(isHolding, isTapped, isSpinning) {
        if (!isSpinning || (!isHolding && !isTapped)) return@LaunchedEffect
        val duration = when {
            isTapped -> 150
            else -> 600
        }
        while (true) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = tween(duration, easing = LinearEasing)
            )
        }
    }

    LaunchedEffect(isTapped) {
        if (isTapped) {
            kotlinx.coroutines.delay(1200)
            isTapped = false
        }
    }

    val currentOnClick by rememberUpdatedState(onClick)
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = if (isSpinning && !isDirectionalArrow) {
                    rotation.value
                } else if (isDownArrow || isUpArrow) {
                    animatedDirectionalRotation
                } else {
                    0f
                }
                scaleX = animScaleX.value
                scaleY = animScaleY.value
                if (isPointing) {
                    if (isForwardArrow || isBackArrow) {
                        translationX = pointerOffset
                    } else if (isDownArrow || isUpArrow) {
                        translationY = pointerOffset
                    }
                }
            }
            .clip(buttonShape)
            .background(animatedBackgroundColor)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isHolding = true
                    val upOrCancel = waitForUpOrCancellation()
                    isHolding = false
                    if (upOrCancel != null) {
                        upOrCancel.consume()
                        isTapped = true
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        currentOnClick()
                    }
                }
            },
    ) {
        if (!isDirectionalArrow) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(34.dp)
                    .graphicsLayer {
                        rotationZ = if (isSpinning) {
                            -rotation.value
                        } else {
                            0f
                        }
                        translationX = pointerOffset
                    },
                tint = if (unviewedCount > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
            )
        } else {
            AnimatedVisibility(
                visible = unviewedCount > 0,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center).graphicsLayer {
                    if (isDownArrow || isUpArrow) {
                        rotationZ = -animatedDirectionalRotation
                    }
                }
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = unviewedCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun SameDayPhotoCarousel(
    photoList: List<Photo>,
    viewModel: SnippetsViewModel,
    screen: Screen,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    showFavoriteIcon: Boolean = true
) {
    val view = LocalView.current
    val context = LocalContext.current
    
    LaunchedEffect(photoList) {
        val imageLoader = context.imageLoader
        photoList.take(6).forEach { photo ->
            val request = ImageRequest.Builder(context)
                .data(photo.uriString)
                .size(840, 960)
                .build()
            imageLoader.enqueue(request)
        }
    }

    val state = rememberCarouselState(initialItem = 0) { photoList.size }
    val carouselItemShape = remember { RoundedCornerShape(48.dp) }

    LaunchedEffect(viewModel.activePhotoId, viewModel.currentScreen) {
        if (viewModel.activePhotoId != null) {
            val targetIndex = photoList.indexOfFirst { it.id == viewModel.activePhotoId }
            if (targetIndex != -1) {
                kotlinx.coroutines.delay(250)
                state.scrollToItem(targetIndex)
            }
        }
    }

    HorizontalUncontainedCarousel(
        state = state,
        itemWidth = 280.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
    ) { itemIndex ->
        val photo = photoList[itemIndex]
        val isSelected = viewModel.selectedPhotoIds.contains(photo.id)
        val selectionMode = viewModel.isSelectionMode

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .maskClip(carouselItemShape)
        ) {
            PhotoMasonryItem(
                photo = photo,
                isSelected = isSelected,
                selectionMode = selectionMode,
                matchingSnippetsCount = getMatchingSnippetsCount(photo, viewModel),
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                showFavoriteIcon = showFavoriteIcon,
                fillCard = true,
                shape = carouselItemShape,
                viewModel = viewModel,
                onClick = {
                    if (viewModel.isSelectionMode) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.toggleSelection(photo.id)
                    } else {
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        viewModel.openDetail(photo.id, overrideReturnScreen = screen)
                    }
                },
                onLongClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    if (!viewModel.isSelectionMode) viewModel.toggleSelection(photo.id)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
