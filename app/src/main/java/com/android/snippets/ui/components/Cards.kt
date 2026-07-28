package com.android.snippets.ui
import com.ln.android.snippets.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import com.android.snippets.ui.shapes.*
import com.android.snippets.ui.theme.*
import com.android.snippets.ui.util.*
import com.android.snippets.ui.components.*
import androidx.compose.material3.carousel.rememberCarouselState
import com.android.snippets.viewmodel.Screen
import com.android.snippets.viewmodel.SnippetsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.imageLoader
import com.android.snippets.model.Photo



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
            transitionSpec = { tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing) }
        ) { state ->
            if (shouldSpin) {
                when (state) {
                    androidx.compose.animation.EnterExitState.PreEnter -> 360f
                    androidx.compose.animation.EnterExitState.Visible -> 0f
                    androidx.compose.animation.EnterExitState.PostExit -> 0f
                }
            } else 0f
        }
    } else {
        remember { mutableStateOf(0f) }
    }

    val currentAppShape = LocalAppShape.current
    val MorphShapesList = remember(currentAppShape) { listOf(currentAppShape, androidx.compose.foundation.shape.CircleShape) }
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
                            boundsTransform = { _, _ -> androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 500f) },
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
            alignment = Alignment { size, space, _ ->
                val x = ((space.width - size.width) * 0.5f).toInt().coerceIn(
                    (space.width - size.width).coerceAtMost(0),
                    (space.width - size.width).coerceAtLeast(0)
                )
                val y = ((space.height - size.height) * 0.5f).toInt().coerceIn(
                    (space.height - size.height).coerceAtMost(0),
                    (space.height - size.height).coerceAtLeast(0)
                )
                androidx.compose.ui.unit.IntOffset(x, y)
            },
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturation) }),
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
    val isForwardArrow = icon == Icons.AutoMirrored.Filled.ArrowForward
    val isBackArrow = icon == Icons.AutoMirrored.Filled.ArrowBack
    val isDownArrow = icon == Icons.Default.KeyboardArrowDown
    val isUpArrow = icon == Icons.Default.KeyboardArrowUp
    val isDirectionalArrow = isForwardArrow || isBackArrow || isDownArrow || isUpArrow

    val animatedDirectionalRotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isUpArrow) 180f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "arrow_rotation"
    )

    val currentAppShape = LocalAppShape.current
    val buttonShape = when {
        isForwardArrow -> androidx.compose.foundation.shape.CircleShape
        isBackArrow -> androidx.compose.foundation.shape.CircleShape
        isDownArrow || isUpArrow -> androidx.compose.foundation.shape.CircleShape
        else -> currentAppShape
    }
    var isHolding by remember { mutableStateOf(false) }
    var isTapped by remember { mutableStateOf(false) }
    val rotation = remember { androidx.compose.animation.core.Animatable(0f) }
    val animScaleX = remember { androidx.compose.animation.core.Animatable(1f) }
    val animScaleY = remember { androidx.compose.animation.core.Animatable(1f) }

    val shapeType = LocalAppShapeType.current
    val isSpinningShape = when (shapeType) {
        AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> true
        else -> false
    }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            when (shapeType) {
                AppShape.CLOVER_4_LEAF -> {
                    animScaleX.animateTo(1.15f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
                    animScaleY.animateTo(0.85f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
                }
                AppShape.PENTAGON -> {
                    rotation.animateTo(12f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
                }
                else -> {
                    animScaleX.animateTo(0.92f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
                    animScaleY.animateTo(0.92f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
                }
            }
        } else {
            when (shapeType) {
                AppShape.PENTAGON -> {
                    rotation.animateTo(0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
                }
                else -> {
                    animScaleX.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
                    animScaleY.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
                }
            }
        }
    }

    val pointerOffset by androidx.compose.animation.core.rememberInfiniteTransition(label = "pointer").animateFloat(
        initialValue = 0f,
        targetValue = if (isPointing) 6f else 0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pointer_offset"
    )

    val animatedBackgroundColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (unviewedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 500),
        label = "arrow_background_color"
    )

    LaunchedEffect(isHolding, isTapped, isSpinning) {
        if (!isSpinning || (!isHolding && !isTapped)) return@LaunchedEffect
        if (isSpinningShape) {
            val duration = when {
                isTapped -> 150
                else -> 600
            }
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = androidx.compose.animation.core.tween(duration, easing = androidx.compose.animation.core.LinearEasing)
                )
            }
        }
    }

    LaunchedEffect(isTapped) {
        if (isTapped) {
            when (shapeType) {
                AppShape.COOKIE_4_SIDED -> {
                    launch {
                        animScaleX.animateTo(0.75f, animationSpec = androidx.compose.animation.core.tween(70, easing = androidx.compose.animation.core.FastOutLinearInEasing))
                        animScaleX.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(90, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                    launch {
                        animScaleY.animateTo(0.75f, animationSpec = androidx.compose.animation.core.tween(70, easing = androidx.compose.animation.core.FastOutLinearInEasing))
                        animScaleY.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(90, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                }
                AppShape.GEM, AppShape.SQUARE -> {
                    animScaleX.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                    animScaleX.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                }
                AppShape.PENTAGON -> {
                    rotation.animateTo(-15f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                    rotation.animateTo(15f, animationSpec = androidx.compose.animation.core.tween(120, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                    rotation.animateTo(0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                }
                AppShape.CLOVER_4_LEAF -> {
                    launch {
                        animScaleX.animateTo(1.25f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(0.8f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                    launch {
                        animScaleY.animateTo(0.75f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1.2f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                }
                AppShape.CLOVER_8_LEAF -> {
                    launch {
                        animScaleX.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1.03f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1.20f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                    launch {
                        animScaleY.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1.03f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1.20f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                }
                else -> {}
            }
            kotlinx.coroutines.delay(1200)
            isTapped = false
        }
    }


    val currentOnClick by rememberUpdatedState(onClick)
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = if (isSpinning && !isDirectionalArrow && isSpinningShape) {
                    rotation.value
                } else if (isSpinning && !isDirectionalArrow && shapeType == AppShape.PENTAGON) {
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
                    // requireUnconsumed = false ensures the DOWN event is received
                    // even inside a horizontalScroll parent, preventing missed taps.
                    awaitFirstDown(requireUnconsumed = false)
                    isHolding = true
                    val upOrCancel = waitForUpOrCancellation()
                    isHolding = false
                    if (upOrCancel != null) {
                        upOrCancel.consume()
                        isTapped = true
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_END)
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
                        rotationZ = if (isSpinning && isSpinningShape) {
                            -rotation.value
                        } else if (isSpinning && shapeType == AppShape.PENTAGON) {
                            -rotation.value
                        } else {
                            0f
                        }
                        translationX = pointerOffset
                    },
                tint = if (unviewedCount > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
            )
        } else {
            androidx.compose.animation.AnimatedVisibility(
                visible = unviewedCount > 0,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
                modifier = Modifier.align(Alignment.Center).graphicsLayer {
                    if (isDownArrow || isUpArrow) {
                        rotationZ = -animatedDirectionalRotation
                    }
                }
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = androidx.compose.foundation.shape.CircleShape,
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun PhotoMasonryItem(
    photo: Photo, 
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    matchingSnippetsCount: Int = 0,
    isMostSnippets: Boolean = false,
    isLeastSnippets: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    showFavoriteIcon: Boolean = true,
    fillCard: Boolean = false,
    grayOutIfViewed: Boolean = false,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(0.dp),
    tab: String? = null,
    viewModel: SnippetsViewModel? = null,
    onEatlistCheckClick: (() -> Unit)? = null
) {
    val isCustomPolygon = shape is com.android.snippets.ui.shapes.RoundedPolygonShape

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topPhotoCornerAnimated by animateDpAsState(
        targetValue = if (isPressed) 24.dp else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "photoMasonryTopPhotoCorner"
    )
    val bottomPhotoCornerAnimated by animateDpAsState(
        targetValue = if (isPressed) 24.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "photoMasonryBottomPhotoCorner"
    )
    val photoShape = RoundedCornerShape(
        topStart = topPhotoCornerAnimated,
        topEnd = topPhotoCornerAnimated,
        bottomStart = bottomPhotoCornerAnimated,
        bottomEnd = bottomPhotoCornerAnimated
    )

    val topBottomCardCornerAnimated by animateDpAsState(
        targetValue = if (isPressed) 24.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "photoMasonryTopBottomCardCorner"
    )
    val bottomBottomCardCornerAnimated by animateDpAsState(
        targetValue = if (isPressed) 24.dp else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "photoMasonryBottomBottomCardCorner"
    )
    val bottomCardShape = RoundedCornerShape(
        topStart = topBottomCardCornerAnimated,
        topEnd = topBottomCardCornerAnimated,
        bottomStart = bottomBottomCardCornerAnimated,
        bottomEnd = bottomBottomCardCornerAnimated
    )

    val saturation by animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "saturation"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0.5f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "alpha"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "scale"
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else if (isDark) Color.White else Color.Black
    val iconContainerColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondaryContainer

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .alpha(animatedAlpha)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        val hasPhoto = photo.uriString.isNotBlank()
        val isEatlist = tab == "Eatlist"
        val showBottomSection = !fillCard && !isEatlist && hasPhoto

        if (!showBottomSection) {
            val finalShape = if (isPressed) RoundedCornerShape(24.dp) else {
                if (viewModel?.makePhotosFollowShape == true) shape else RoundedCornerShape(16.dp)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = finalShape,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = if (isSelected) 6.dp else 2.dp,
                border = null
            ) {
                if (hasPhoto) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                    with(sharedTransitionScope) {
                                        Modifier.sharedBounds(
                                            rememberSharedContentState(key = if (tab != null) "photo_${tab}_${photo.id}" else "photo_${photo.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            boundsTransform = { _, _ ->
                                                spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessMediumLow
                                                )
                                            },
                                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center
                                            ),
                                            clipInOverlayDuringTransition = OverlayClip(finalShape)
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photo.uriString)
                                .crossfade(true)
                                .memoryCacheKey(photo.uriString)
                                .build(),
                            contentDescription = null,
                            contentScale = if (fillCard || isCustomPolygon) ContentScale.Crop else ContentScale.FillWidth,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(saturation) }),
                            alignment = Alignment { size, space, _ ->
                                val x = ((space.width - size.width) * 0.5f).toInt().coerceIn(
                                    (space.width - size.width).coerceAtMost(0),
                                    (space.width - size.width).coerceAtLeast(0)
                                )
                                val y = ((space.height - size.height) * 0.5f).toInt().coerceIn(
                                    (space.height - size.height).coerceAtMost(0),
                                    (space.height - size.height).coerceAtLeast(0)
                                )
                                androidx.compose.ui.unit.IntOffset(x, y)
                            },
                            modifier = (if (fillCard) {
                                Modifier.fillMaxSize()
                            } else if (isCustomPolygon) {
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            } else {
                                photo.aspectRatio?.let { ratio ->
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(ratio)
                                } ?: Modifier.fillMaxWidth().wrapContentHeight()
                            })
                        )

                        if (photo.isFavorite && !isSelected && showFavoriteIcon) {
                            Surface(
                                color = iconContainerColor.copy(alpha = 0.9f),
                                shape = LocalAppShape.current,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        if (photo.rating > 0 && !isSelected) {
                            Surface(
                                color = iconContainerColor.copy(alpha = 0.9f),
                                shape = LocalAppShape.current,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_star_rating),
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.fillMaxSize().padding(3.dp)
                                    )
                                    Text(
                                        text = photo.rating.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold),
                                        color = iconContainerColor,
                                        modifier = Modifier.padding(top = 0.5.dp)
                                    )
                                }
                            }
                        }

                        val showBadge = matchingSnippetsCount > 0 || isMostSnippets || isLeastSnippets
                        if (showBadge && !isSelected && photo.snippets.isNotEmpty() && fillCard) {
                            val badgeContainerColor = when {
                                matchingSnippetsCount > 0 -> MaterialTheme.colorScheme.tertiary
                                isMostSnippets -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.tertiary
                            }
                            val badgeContentColor = when {
                                matchingSnippetsCount > 0 -> MaterialTheme.colorScheme.onTertiary
                                isMostSnippets -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onTertiary
                            }
                            val displayCount = if (matchingSnippetsCount > 0) matchingSnippetsCount else photo.snippets.size
                            Surface(
                                color = badgeContainerColor,
                                shape = LocalAppShape.current,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = displayCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = badgeContentColor
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photo.snippets.isEmpty()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_text_snippet_off),
                                contentDescription = "No snippets",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                photo.snippets.forEach { snippet ->
                                    val forcedColor = viewModel?.getSnippetColor(snippet)
                                    val forcedStyle = viewModel?.getSnippetStyle(snippet)

                                    val baseSnippetColor = if (forcedColor != null) Color(forcedColor) else MaterialTheme.colorScheme.primary
                                    val snippetColor = remember(baseSnippetColor, isDark) {
                                        val lum = 0.299f * baseSnippetColor.red + 0.587f * baseSnippetColor.green + 0.114f * baseSnippetColor.blue
                                        if (isDark && lum < 0.3f) baseSnippetColor.copy(red = (baseSnippetColor.red + 0.4f).coerceAtMost(1f), green = (baseSnippetColor.green + 0.4f).coerceAtMost(1f), blue = (baseSnippetColor.blue + 0.4f).coerceAtMost(1f))
                                        else if (!isDark && lum > 0.7f) baseSnippetColor.copy(red = (baseSnippetColor.red - 0.4f).coerceAtLeast(0f), green = (baseSnippetColor.green - 0.4f).coerceAtLeast(0f), blue = (baseSnippetColor.blue - 0.4f).coerceAtLeast(0f))
                                        else baseSnippetColor
                                    }

                                    val snippetGradient = remember(snippetColor) {
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(snippetColor, snippetColor.copy(alpha = 0.55f))
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else snippetColor.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, snippetColor.copy(alpha = if (isSelected) 0.60f else 0.30f)),
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .padding(2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = snippet,
                                                style = getSnippetTextStyle(
                                                    forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default,
                                                    MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                                    isCloud = true
                                                ).copy(color = Color.Unspecified).copy(brush = snippetGradient),
                                                color = Color.Unspecified,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = photoShape,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = if (isSelected) 6.dp else 2.dp,
                border = null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                with(sharedTransitionScope) {
                                    Modifier.sharedBounds(
                                        rememberSharedContentState(key = if (tab != null) "photo_${tab}_${photo.id}" else "photo_${photo.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        boundsTransform = { _, _ ->
                                            spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        },
                                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                                            contentScale = ContentScale.Crop,
                                            alignment = Alignment.Center
                                        ),
                                        clipInOverlayDuringTransition = OverlayClip(photoShape)
                                    )
                                }
                            } else {
                                Modifier
                            }
                        )
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photo.uriString)
                            .crossfade(true)
                            .memoryCacheKey(photo.uriString)
                            .build(),
                        contentDescription = null,
                        contentScale = if (isCustomPolygon) ContentScale.Crop else ContentScale.FillWidth,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(saturation) }),
                        alignment = Alignment { size, space, _ ->
                            val x = ((space.width - size.width) * 0.5f).toInt().coerceIn(
                                (space.width - size.width).coerceAtMost(0),
                                (space.width - size.width).coerceAtLeast(0)
                            )
                            val y = ((space.height - size.height) * 0.5f).toInt().coerceIn(
                                (space.height - size.height).coerceAtMost(0),
                                (space.height - size.height).coerceAtLeast(0)
                            )
                            androidx.compose.ui.unit.IntOffset(x, y)
                        },
                        modifier = (if (isCustomPolygon) {
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        } else {
                            photo.aspectRatio?.let { ratio ->
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(ratio)
                            } ?: Modifier.fillMaxWidth().wrapContentHeight()
                        })
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.Start),
                shape = bottomCardShape,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = if (isSelected) 4.dp else 1.dp,
                border = null
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (photo.snippets.isEmpty()) {
                        Box(
                            modifier = Modifier.wrapContentWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_text_snippet_off),
                                contentDescription = "No snippets",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            photo.snippets.forEach { snippet ->
                                val forcedColor = viewModel?.getSnippetColor(snippet)
                                val forcedStyle = viewModel?.getSnippetStyle(snippet)

                                val baseSnippetColor = if (forcedColor != null) Color(forcedColor) else MaterialTheme.colorScheme.primary
                                val snippetColor = remember(baseSnippetColor, isDark) {
                                    val lum = 0.299f * baseSnippetColor.red + 0.587f * baseSnippetColor.green + 0.114f * baseSnippetColor.blue
                                    if (isDark && lum < 0.3f) baseSnippetColor.copy(red = (baseSnippetColor.red + 0.4f).coerceAtMost(1f), green = (baseSnippetColor.green + 0.4f).coerceAtMost(1f), blue = (baseSnippetColor.blue + 0.4f).coerceAtMost(1f))
                                    else if (!isDark && lum > 0.7f) baseSnippetColor.copy(red = (baseSnippetColor.red - 0.4f).coerceAtLeast(0f), green = (baseSnippetColor.green - 0.4f).coerceAtLeast(0f), blue = (baseSnippetColor.blue - 0.4f).coerceAtLeast(0f))
                                    else baseSnippetColor
                                }

                                val snippetGradient = remember(snippetColor) {
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(snippetColor, snippetColor.copy(alpha = 0.55f))
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else snippetColor.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, snippetColor.copy(alpha = if (isSelected) 0.60f else 0.30f)),
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = snippet,
                                            style = getSnippetTextStyle(
                                                forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default,
                                                MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                                isCloud = true
                                            ).copy(color = Color.Unspecified).copy(brush = snippetGradient),
                                            color = Color.Unspecified,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoListItem(
    photo: Photo, 
    position: CardPosition = CardPosition.Single,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    showFavoriteIcon: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(0.dp),
    tab: String? = null,
    isMostSnippets: Boolean = false,
    isLeastSnippets: Boolean = false,
    grayOutIfViewed: Boolean = false,
    viewModel: SnippetsViewModel,
    onEatlistCheckClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topCornerTarget = if (isPressed || isSelected) 24.dp else {
        when (position) {
            CardPosition.Single, CardPosition.First -> 16.dp
            else -> 4.dp
        }
    }
    val bottomCornerTarget = if (isPressed || isSelected) 24.dp else {
        when (position) {
            CardPosition.Single, CardPosition.Last -> 16.dp
            else -> 4.dp
        }
    }

    val topCornerAnimated by animateDpAsState(
        targetValue = topCornerTarget,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "photoListItemTopCorner"
    )
    val bottomCornerAnimated by animateDpAsState(
        targetValue = bottomCornerTarget,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "photoListItemBottomCorner"
    )

    val cardShape = RoundedCornerShape(
        topStart = topCornerAnimated,
        topEnd = topCornerAnimated,
        bottomStart = bottomCornerAnimated,
        bottomEnd = bottomCornerAnimated
    )
    val finalShape = cardShape
    val photoShape = if (viewModel.makePhotosFollowShape) {
        LocalAppShape.current
    } else {
        RoundedCornerShape(12.dp)
    }

    val saturation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0.5f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
        label = "saturation"
    )
    val animatedAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0.8f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
        label = "alpha"
    )
    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .alpha(animatedAlpha)
            .clip(cardShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 1.dp),
        border = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 2.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .then(
                        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                            with(sharedTransitionScope) {
                                Modifier.sharedBounds(
                                    rememberSharedContentState(key = if (tab != null) "photo_${tab}_${photo.id}" else "photo_${photo.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ ->
                                        androidx.compose.animation.core.spring(
                                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                        )
                                    },
                                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                                        contentScale = ContentScale.Crop,
                                        alignment = Alignment.Center
                                    ),
                                    clipInOverlayDuringTransition = OverlayClip(photoShape)
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
                    .clip(photoShape)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.uriString)
                        .crossfade(true)
                        .memoryCacheKey(photo.uriString)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                        androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(saturation) }
                    ),
                    modifier = Modifier.fillMaxSize()
                )

                if ((isMostSnippets || isLeastSnippets) && !isSelected) {
                    val badgeContainerColor = if (isMostSnippets) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    val badgeContentColor = if (isMostSnippets) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiary
                    Surface(
                        color = badgeContainerColor.copy(alpha = 0.9f),
                        shape = LocalAppShape.current,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(2.dp)
                            .size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = photo.snippets.size.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
                                color = badgeContentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                if (photo.snippets.isEmpty()) {
                    if (tab != "Eatlist") {
                        Surface(
                            shape = LocalAppShape.current,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_text_snippet_off),
                                    contentDescription = "No snippets",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    val topSnippets = photo.snippets.take(2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        topSnippets.forEach { snippet ->
                            val forcedColor = viewModel.getSnippetColor(snippet)
                            val forcedStyle = viewModel.getSnippetStyle(snippet)

                            val isDark = !MaterialTheme.colorScheme.surface.let { it.red + it.green + it.blue > 1.5f }
                            val baseSnippetColor = if (forcedColor != null) Color(forcedColor) else MaterialTheme.colorScheme.primary
                            val snippetColor = remember(baseSnippetColor, isDark) {
                                val lum = 0.299f * baseSnippetColor.red + 0.587f * baseSnippetColor.green + 0.114f * baseSnippetColor.blue
                                if (isDark && lum < 0.3f) baseSnippetColor.copy(red = (baseSnippetColor.red + 0.4f).coerceAtMost(1f), green = (baseSnippetColor.green + 0.4f).coerceAtMost(1f), blue = (baseSnippetColor.blue + 0.4f).coerceAtMost(1f))
                                else if (!isDark && lum > 0.7f) baseSnippetColor.copy(red = (baseSnippetColor.red - 0.4f).coerceAtLeast(0f), green = (baseSnippetColor.green - 0.4f).coerceAtLeast(0f), blue = (baseSnippetColor.blue - 0.4f).coerceAtLeast(0f))
                                else baseSnippetColor
                            }

                            val snippetGradient = remember(snippetColor) {
                                androidx.compose.ui.graphics.Brush.linearGradient(colors = listOf(snippetColor, snippetColor.copy(alpha = 0.55f)))
                            }

                            Surface(
                                onClick = { onClick() },
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else snippetColor.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, snippetColor.copy(alpha = if (isSelected) 0.60f else 0.30f)),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = snippet,
                                        style = getSnippetTextStyle(
                                            forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default,
                                            MaterialTheme.typography.labelLarge,
                                            isCloud = true
                                        ).copy(brush = snippetGradient),
                                        color = Color.Unspecified,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (photo.rating > 0 || (photo.isFavorite && showFavoriteIcon)) {
                val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                val iconContainerColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondaryContainer

                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (photo.rating > 0) {
                        Surface(
                            color = iconContainerColor.copy(alpha = 0.9f),
                            shape = LocalAppShape.current,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_star_rating),
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.fillMaxSize().padding(3.dp)
                                )
                                Text(
                                    text = photo.rating.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
                                    color = iconContainerColor,
                                    modifier = Modifier.padding(top = 0.5.dp)
                                )
                            }
                        }
                    }

                    if (photo.isFavorite && showFavoriteIcon) {
                        Surface(
                            color = iconContainerColor.copy(alpha = 0.9f),
                            shape = LocalAppShape.current,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorite",
                                    tint = iconColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoCardListItem(
    photo: Photo,
    position: CardPosition,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    showFavoriteIcon: Boolean = true,
    tab: String? = null,
    isMostSnippets: Boolean = false,
    isLeastSnippets: Boolean = false,
    grayOutIfViewed: Boolean = false,
    viewModel: SnippetsViewModel,
    onEatlistCheckClick: (() -> Unit)? = null
) {
    val saturation by animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0.5f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "saturation"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0.8f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "alpha"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "scale"
    )

    val view = LocalView.current

    val photoShape = when (position) {
        CardPosition.Single -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        CardPosition.First -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        CardPosition.Middle -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
        CardPosition.Last -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topCornerSize by animateDpAsState(
        targetValue = if (isPressed) 24.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bottomCardTopCorner"
    )
    val bottomCornerSize by animateDpAsState(
        targetValue = if (isPressed) 24.dp else {
            if (position == CardPosition.Last || position == CardPosition.Single) 16.dp else 4.dp
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bottomCardBottomCorner"
    )
    val bottomCardShape = RoundedCornerShape(
        topStart = topCornerSize,
        topEnd = topCornerSize,
        bottomStart = bottomCornerSize,
        bottomEnd = bottomCornerSize
    )
    val finalShape = if (isPressed) {
        RoundedCornerShape(24.dp)
    } else {
        RoundedCornerShape(16.dp)
    }
    val blockColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val isCompact = photo.snippets.size <= 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = if (position == CardPosition.Middle || position == CardPosition.Last) 0.dp else 2.dp,
                bottom = if (position == CardPosition.First || position == CardPosition.Middle) 0.dp else 2.dp
            )
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .alpha(animatedAlpha)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onClick()
                },
                onLongClick = {
                    onLongClick?.let {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        it.invoke()
                    }
                }
            )
    ) {
        val hasPhoto = photo.uriString.isNotBlank()
        val isEatlist = tab == "Eatlist"
        val showBottomCard = !isEatlist && hasPhoto

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            if (!showBottomCard) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = finalShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else blockColor,
                    shadowElevation = if (isSelected) 4.dp else 1.dp
                ) {
                    if (isEatlist) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.layout.BoxWithConstraints(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val size = androidx.compose.ui.unit.min(maxWidth, maxHeight)
                                val makePhotosFollowShape = viewModel.makePhotosFollowShape
                                val appShape = LocalAppShape.current
                                val photoClipShape = if (makePhotosFollowShape) appShape else finalShape
                                Box(
                                    modifier = (if (makePhotosFollowShape) Modifier.size(size) else Modifier.fillMaxSize()).then(
                                        if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                            with(sharedTransitionScope) {
                                                Modifier.sharedBounds(
                                                    rememberSharedContentState(key = if (tab != null) "photo_${tab}_${photo.id}" else "photo_${photo.id}"),
                                                    animatedVisibilityScope = animatedVisibilityScope,
                                                    boundsTransform = { _, _ ->
                                                        spring(
                                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                                            stiffness = Spring.StiffnessMediumLow
                                                        )
                                                    },
                                                    resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                                                        contentScale = ContentScale.Crop,
                                                        alignment = Alignment.Center
                                                    ),
                                                    clipInOverlayDuringTransition = OverlayClip(photoClipShape)
                                                )
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ).clip(photoClipShape)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(photo.uriString)
                                            .crossfade(true)
                                            .memoryCacheKey(photo.uriString)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                                            androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(saturation) }
                                        ),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else if (isDark) Color.White else Color.Black
                            val iconContainerColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondaryContainer

                            if (photo.rating > 0) {
                                Surface(
                                    color = iconContainerColor.copy(alpha = 0.9f),
                                    shape = LocalAppShape.current,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_star_rating),
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.fillMaxSize().padding(4.dp)
                                        )
                                        Text(
                                            text = photo.rating.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                                            color = iconContainerColor,
                                            modifier = Modifier.padding(top = 1.dp)
                                        )
                                    }
                                }
                            }

                            if (photo.isFavorite && showFavoriteIcon) {
                                Surface(
                                    color = iconContainerColor.copy(alpha = 0.9f),
                                    shape = LocalAppShape.current,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Favorite",
                                            tint = iconColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photo.snippets.isEmpty()) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_text_snippet_off),
                                    contentDescription = "No snippets",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(36.dp)
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    photo.snippets.forEach { snippet ->
                                        val forcedColor = viewModel.getSnippetColor(snippet)
                                        val forcedStyle = viewModel.getSnippetStyle(snippet)

                                        val baseSnippetColor = if (forcedColor != null) Color(forcedColor) else MaterialTheme.colorScheme.primary
                                        val snippetColor = remember(baseSnippetColor, isDark) {
                                            val lum = 0.299f * baseSnippetColor.red + 0.587f * baseSnippetColor.green + 0.114f * baseSnippetColor.blue
                                            if (isDark && lum < 0.3f) baseSnippetColor.copy(red = (baseSnippetColor.red + 0.4f).coerceAtMost(1f), green = (baseSnippetColor.green + 0.4f).coerceAtMost(1f), blue = (baseSnippetColor.blue + 0.4f).coerceAtMost(1f))
                                            else if (!isDark && lum > 0.7f) baseSnippetColor.copy(red = (baseSnippetColor.red - 0.4f).coerceAtLeast(0f), green = (baseSnippetColor.green - 0.4f).coerceAtLeast(0f), blue = (baseSnippetColor.blue - 0.4f).coerceAtLeast(0f))
                                            else baseSnippetColor
                                        }

                                        val snippetGradient = remember(snippetColor) {
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                colors = listOf(snippetColor, snippetColor.copy(alpha = 0.55f))
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else snippetColor.copy(alpha = 0.18f),
                                            border = BorderStroke(1.dp, snippetColor.copy(alpha = if (isSelected) 0.60f else 0.30f)),
                                            modifier = Modifier
                                                .wrapContentWidth()
                                                .padding(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(
                                                    text = snippet,
                                                    style = getSnippetTextStyle(
                                                        forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default,
                                                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        isCloud = true
                                                    ).copy(color = Color.Unspecified).copy(brush = snippetGradient),
                                                    color = Color.Unspecified,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val photoWeight = 1f

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(photoWeight),
                    shape = photoShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else blockColor,
                    shadowElevation = if (isSelected) 4.dp else 1.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.layout.BoxWithConstraints(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val size = androidx.compose.ui.unit.min(maxWidth, maxHeight)
                            val makePhotosFollowShape = viewModel.makePhotosFollowShape
                            val appShape = LocalAppShape.current
                            val photoClipShape = if (makePhotosFollowShape) appShape else photoShape
                            Box(
                                modifier = (if (makePhotosFollowShape) Modifier.size(size) else Modifier.fillMaxSize()).then(
                                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                        with(sharedTransitionScope) {
                                            Modifier.sharedBounds(
                                                rememberSharedContentState(key = if (tab != null) "photo_${tab}_${photo.id}" else "photo_${photo.id}"),
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                boundsTransform = { _, _ ->
                                                    spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                },
                                                resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                                                    contentScale = ContentScale.Crop,
                                                    alignment = Alignment.Center
                                                ),
                                                clipInOverlayDuringTransition = OverlayClip(photoClipShape)
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }
                                ).clip(photoClipShape)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(photo.uriString)
                                        .crossfade(true)
                                        .memoryCacheKey(photo.uriString)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                                        androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(saturation) }
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else if (isDark) Color.White else Color.Black
                        val iconContainerColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondaryContainer

                        if (photo.rating > 0) {
                            Surface(
                                color = iconContainerColor.copy(alpha = 0.9f),
                                shape = LocalAppShape.current,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_star_rating),
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.fillMaxSize().padding(4.dp)
                                    )
                                    Text(
                                        text = photo.rating.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                                        color = iconContainerColor,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }
                            }
                        }

                        if (photo.isFavorite && showFavoriteIcon) {
                            Surface(
                                color = iconContainerColor.copy(alpha = 0.9f),
                                shape = LocalAppShape.current,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Favorite",
                                        tint = iconColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val bottomCardModifier = Modifier.wrapContentHeight()

                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .then(bottomCardModifier),
                    shape = bottomCardShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else blockColor,
                    shadowElevation = if (isSelected) 4.dp else 1.dp
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (photo.snippets.isEmpty()) {
                            Box(
                                modifier = Modifier.wrapContentWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_text_snippet_off),
                                    contentDescription = "No snippets",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                photo.snippets.forEach { snippet ->
                                    val forcedColor = viewModel.getSnippetColor(snippet)
                                    val forcedStyle = viewModel.getSnippetStyle(snippet)

                                    val baseSnippetColor = if (forcedColor != null) Color(forcedColor) else MaterialTheme.colorScheme.primary
                                    val snippetColor = remember(baseSnippetColor, isDark) {
                                        val lum = 0.299f * baseSnippetColor.red + 0.587f * baseSnippetColor.green + 0.114f * baseSnippetColor.blue
                                        if (isDark && lum < 0.3f) baseSnippetColor.copy(red = (baseSnippetColor.red + 0.4f).coerceAtMost(1f), green = (baseSnippetColor.green + 0.4f).coerceAtMost(1f), blue = (baseSnippetColor.blue + 0.4f).coerceAtMost(1f))
                                        else if (!isDark && lum > 0.7f) baseSnippetColor.copy(red = (baseSnippetColor.red - 0.4f).coerceAtLeast(0f), green = (baseSnippetColor.green - 0.4f).coerceAtLeast(0f), blue = (baseSnippetColor.blue - 0.4f).coerceAtLeast(0f))
                                        else baseSnippetColor
                                    }

                                    val snippetGradient = remember(snippetColor) {
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(snippetColor, snippetColor.copy(alpha = 0.55f))
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else snippetColor.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, snippetColor.copy(alpha = if (isSelected) 0.60f else 0.30f)),
                                        modifier = Modifier.wrapContentWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = snippet,
                                                style = getSnippetTextStyle(
                                                    forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default,
                                                    MaterialTheme.typography.labelLarge,
                                                    isCloud = true
                                                ).copy(color = Color.Unspecified).copy(brush = snippetGradient),
                                                color = Color.Unspecified,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} // closes PhotoCardListItem

enum class CardPosition {
    Single, First, Middle, Last
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicCardContainer(
    modifier: Modifier = Modifier,
    position: CardPosition = CardPosition.Single,
    isSelected: Boolean = false,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topCornerTarget = if (isPressed || isSelected) 24.dp else {
        when (position) {
            CardPosition.Single, CardPosition.First -> 20.dp
            else -> 4.dp
        }
    }
    val bottomCornerTarget = if (isPressed || isSelected) 24.dp else {
        when (position) {
            CardPosition.Single, CardPosition.Last -> 20.dp
            else -> 4.dp
        }
    }

    val topCornerAnimated by animateDpAsState(
        targetValue = topCornerTarget,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "dynamicCardTopCorner"
    )
    val bottomCornerAnimated by animateDpAsState(
        targetValue = bottomCornerTarget,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "dynamicCardBottomCorner"
    )

    val shape = RoundedCornerShape(
        topStart = topCornerAnimated,
        topEnd = topCornerAnimated,
        bottomStart = bottomCornerAnimated,
        bottomEnd = bottomCornerAnimated
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val view = LocalView.current
    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = if (position == CardPosition.Middle || position == CardPosition.Last) 0.dp else 2.dp,
                bottom = if (position == CardPosition.First || position == CardPosition.Middle) 0.dp else 2.dp
            )
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = onClick != null || onLongClick != null,
                onClick = {
                    onClick?.let {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        it.invoke()
                    }
                },
                onLongClick = {
                    onLongClick?.let {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        it.invoke()
                    }
                }
            ),
        shape = shape,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else (containerColor ?: MaterialTheme.colorScheme.surfaceContainerHighest),
        shadowElevation = if (isSelected) 6.dp else 1.dp,
        border = null
    ) {
        content()
    }
}

@Composable
fun SettingsCardItem(
    icon: Any,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    trailingContent: @Composable () -> Unit = {},
    titleContent: @Composable (() -> Unit)? = null,
    position: CardPosition = CardPosition.Single,
    isExpressive: Boolean = false,
    containerColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val secondaryColor = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    DynamicCardContainer(
        onClick = onClick,
        position = position,
        isSelected = isSelected,
        containerColor = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = if (isExpressive) 16.dp else 12.dp,
                    vertical = if (isExpressive) 20.dp else 16.dp
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.android.snippets.ui.CollectionIcon(
                icon = icon,
                modifier = Modifier.size(
                    if (isSelected) 44.dp
                    else if (isExpressive) 36.dp
                    else 32.dp
                ),
                tint = if (isSelected) contentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                if (titleContent != null) {
                    titleContent()
                } else {
                    Text(
                        text = title,
                        style = if (isSelected) {
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide
                            )
                        } else if (isExpressive) {
                            MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                letterSpacing = 0.15.sp
                            )
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryColor
                    )
                }
            }
            Box(
                contentAlignment = Alignment.CenterEnd
            ) {
                trailingContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun SameDayPhotoCarousel(
    photoList: List<Photo>,
    viewModel: com.android.snippets.viewmodel.SnippetsViewModel,
    screen: Screen,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    showFavoriteIcon: Boolean = true
) {
    val view = LocalView.current
    val context = LocalContext.current
    
    // Preload the first few photos so they don't pop in when scrolling
    LaunchedEffect(photoList) {
        val imageLoader = context.imageLoader
        photoList.take(15).forEach { photo ->
            val request = ImageRequest.Builder(context)
                .data(photo.uriString)
                .build()
            imageLoader.enqueue(request)
        }
    }

    // rememberCarouselState keeps item count; we key scroll restoration by photo list identity
    val state = rememberCarouselState(initialItem = 0) { photoList.size }
    val carouselItemShape = remember { RoundedCornerShape(48.dp) }

    // Scroll back to the photo that was last opened when returning from Detail
    LaunchedEffect(viewModel.activePhotoId, viewModel.currentScreen) {
        if (
            viewModel.currentScreen != Screen.Detail &&
            viewModel.activePhotoId != null
        ) {
            val targetIndex = photoList.indexOfFirst { it.id == viewModel.activePhotoId }
            if (targetIndex != -1) {
                kotlinx.coroutines.delay(250)
                state.scrollToItem(targetIndex)
            }
        }
    }

    // HorizontalUncontainedCarousel: items have FIXED width, no masking/scaling by the
    // carousel itself. This prevents the first item from shifting after returning from Detail.
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

        // maskClip is required by the carousel DSL; using the same rounded shape keeps
        // corners consistent while letting the carousel handle edge clipping naturally.
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EatlistSingleListCardContainer(
    photo: Photo,
    onOpenLink: (String) -> Unit,
    onEditLink: () -> Unit,
    onRemoveEatlist: () -> Unit,
    onAddEatlistToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val photoShape = LocalAppShape.current
    var showActionDialog by remember { mutableStateOf(false) }

    val placeName = remember(photo) {
        LocationUtils.getLocationFromExif(context, photo)
            ?: LocationUtils.extractPlaceNameFromLink(photo.locationLink ?: "")
            ?: "Eatlist photo"
    }

    if (showActionDialog) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = {
                Text(
                    text = "Eatlist options",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = {
                            showActionDialog = false
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onAddEatlistToLibrary()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryAdd,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Add to library",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            showActionDialog = false
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            onRemoveEatlist()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Mark as eaten and delete",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showActionDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Thumbnail Photo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(photoShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photo.uriString)
                        .crossfade(true)
                        .memoryCacheKey(photo.uriString)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (photo.isFavorite) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                        shape = photoShape,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                    }
                }
            }

            // Middle Location/Link Info Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = placeName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )

                if (!photo.locationLink.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onOpenLink(photo.locationLink)
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_link),
                            contentDescription = "Link",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = photo.locationLink,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    }
                }
            }

            // Trailing Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Link Icon Action Button
                AnimatedCookieButton(
                    onClick = {
                        if (!photo.locationLink.isNullOrBlank()) {
                            onOpenLink(photo.locationLink)
                        } else {
                            onEditLink()
                        }
                    },
                    icon = R.drawable.ic_add_link,
                    contentDescription = "Link Action",
                    tooltip = "Link Action",
                    size = 36.dp,
                    containerColor = if (!photo.locationLink.isNullOrBlank()) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = if (!photo.locationLink.isNullOrBlank()) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Checkmark / Eatlist Action Button
                AnimatedCookieButton(
                    onClick = {
                        showActionDialog = true
                    },
                    icon = Icons.Default.Check,
                    contentDescription = "Eatlist check",
                    tooltip = "Eatlist options",
                    size = 36.dp,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
