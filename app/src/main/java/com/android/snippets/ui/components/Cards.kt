package com.android.snippets.ui
import com.ln.android.snippets.R
import androidx.compose.ui.res.painterResource

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
    onEatlistCheckClick: (() -> Unit)? = null
) {
    val finalShape = if (isSelected) RoundedCornerShape(4.dp) else shape
    val isCustomPolygon = shape is com.android.snippets.ui.shapes.RoundedPolygonShape

    val saturation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
        label = "saturation"
    )
    val animatedAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0.5f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 600),
        label = "alpha"
    )
    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 0.85f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "scale"
    )

    val iconColor = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.White else Color.Black

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .alpha(animatedAlpha)
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
                            clipInOverlayDuringTransition = OverlayClip(finalShape)
                        )
                    }
                } else {
                    Modifier
                }
            )
            .clip(finalShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = finalShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        border = if (isSelected) BorderStroke(4.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(modifier = if (fillCard) Modifier.fillMaxSize() else Modifier) {
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
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = LocalAppShape.current,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            if (photo.rating > 0 && !isSelected) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = LocalAppShape.current,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(32.dp)
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
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }
            }
            
            val showBadge = matchingSnippetsCount > 0 || isMostSnippets || isLeastSnippets
            if (showBadge && !isSelected && photo.snippets.isNotEmpty()) {
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
                        .padding(16.dp)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = displayCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeContentColor
                        )
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
    val finalShape = if (isSelected) RoundedCornerShape(4.dp) else shape
    val cardShape = if (isSelected) RoundedCornerShape(4.dp) else RoundedCornerShape(8.dp)
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
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 1.dp),
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(8.dp),
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

                if (photo.rating > 0) {
                    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.White else Color.Black
                    val iconContainerColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondaryContainer
                    Surface(
                        color = iconContainerColor.copy(alpha = 0.9f),
                        shape = LocalAppShape.current,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(2.dp)
                            .size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_star_rating),
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.fillMaxSize().padding(2.dp)
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

                if (photo.isFavorite && showFavoriteIcon) {
                    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.White else Color.Black
                    val iconContainerColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondaryContainer
                    Surface(
                        color = iconContainerColor.copy(alpha = 0.9f),
                        shape = LocalAppShape.current,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(20.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                tint = iconColor,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

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
                        Icon(
                            painter = painterResource(id = R.drawable.ic_text_snippet_off),
                            contentDescription = "No snippets",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    val topSnippets = photo.snippets.take(6)
                    val total = photo.snippets.size
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        topSnippets.forEachIndexed { index, snippet ->
                            CloudSnippetItem(
                                text = snippet,
                                index = index,
                                totalCount = total,
                                photoColors = emptyList(),
                                forcedColor = viewModel.getSnippetColor(snippet),
                                forcedStyle = viewModel.getSnippetStyle(snippet),
                                isSegmented = true
                            )
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
    val finalShape = if (isSelected) {
        RoundedCornerShape(24.dp)
    } else {
        when (position) {
            CardPosition.Single -> RoundedCornerShape(16.dp)
            CardPosition.First -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
            CardPosition.Middle -> RoundedCornerShape(4.dp)
            CardPosition.Last -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        }
    }

    val photoShape = LocalAppShape.current

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

    val animRotation = remember { androidx.compose.animation.core.Animatable(0f) }
    val animScaleX = remember { androidx.compose.animation.core.Animatable(1f) }
    val animScaleY = remember { androidx.compose.animation.core.Animatable(1f) }
    val shapeType = com.android.snippets.ui.shapes.LocalAppShapeType.current
    var wasInDetail by remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(viewModel.currentScreen, viewModel.activePhotoId) {
        if (viewModel.currentScreen == Screen.Detail && viewModel.activePhotoId == photo.id) {
            wasInDetail = true
        } else if (wasInDetail && viewModel.currentScreen != Screen.Detail && viewModel.activePhotoId == photo.id) {
            wasInDetail = false
            // Trigger animation like the icon shape buttons!
            when (shapeType) {
                com.android.snippets.ui.shapes.AppShape.COOKIE_12_SIDED,
                com.android.snippets.ui.shapes.AppShape.PILL,
                com.android.snippets.ui.shapes.AppShape.VERY_SUNNY -> {
                    animRotation.snapTo(0f)
                    animRotation.animateTo(
                        targetValue = 360f,
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 500,
                            easing = androidx.compose.animation.core.CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
                        )
                    )
                }
                com.android.snippets.ui.shapes.AppShape.COOKIE_4_SIDED -> {
                    launch {
                        animScaleX.animateTo(0.75f, animationSpec = androidx.compose.animation.core.tween(70, easing = androidx.compose.animation.core.FastOutLinearInEasing))
                        animScaleX.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(90, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1.0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                    launch {
                        animScaleY.animateTo(0.75f, animationSpec = androidx.compose.animation.core.tween(70, easing = androidx.compose.animation.core.FastOutLinearInEasing))
                        animScaleY.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(90, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1.0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                }
                com.android.snippets.ui.shapes.AppShape.GEM,
                com.android.snippets.ui.shapes.AppShape.SQUARE -> {
                    launch {
                        animScaleX.animateTo(1.18f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1.0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                    launch {
                        animScaleY.animateTo(1.18f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1.0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                }
                com.android.snippets.ui.shapes.AppShape.PENTAGON -> {
                    animRotation.animateTo(-15f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                    animRotation.animateTo(15f, animationSpec = androidx.compose.animation.core.tween(120, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                    animRotation.animateTo(0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                }
                com.android.snippets.ui.shapes.AppShape.CLOVER_4_LEAF -> {
                    launch {
                        animScaleX.animateTo(1.25f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(0.8f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1.0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                    launch {
                        animScaleY.animateTo(0.75f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1.2f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1.0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                }
                com.android.snippets.ui.shapes.AppShape.CLOVER_8_LEAF -> {
                    launch {
                        animScaleX.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1.03f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1.20f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleX.animateTo(1.0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                    launch {
                        animScaleY.animateTo(1.15f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1.03f, animationSpec = androidx.compose.animation.core.tween(80, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1.20f, animationSpec = androidx.compose.animation.core.tween(100, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        animScaleY.animateTo(1.0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium))
                    }
                }
            }
        }
    }

    val makePhotosFollowShape = viewModel?.makePhotosFollowShape ?: false
    val cardPhotoShape = if (makePhotosFollowShape) photoShape else androidx.compose.ui.graphics.RectangleShape
    val blockColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
    DynamicCardContainer(
        position = position,
        isSelected = isSelected,
        onClick = onClick,
        onLongClick = onLongClick,
        containerColor = Color.Transparent,
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .alpha(animatedAlpha)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo Box: takes full card space for Eatlist, or 60% for cards with snippets column
            Box(
                modifier = Modifier
                    .weight(if (tab == "Eatlist") 1f else 0.60f)
                    .fillMaxHeight()
                    .background(
                        color = blockColor,
                        shape = if (tab == "Eatlist") RoundedCornerShape(0.dp) else RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                    androidx.compose.foundation.layout.BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (makePhotosFollowShape) 12.dp else 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val size = androidx.compose.ui.unit.min(maxWidth, maxHeight)
                        Box(
                            modifier = (if (makePhotosFollowShape) Modifier.size(size) else Modifier.fillMaxSize()).then(
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
                                                clipInOverlayDuringTransition = OverlayClip(cardPhotoShape)
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                                .graphicsLayer {
                                    this.rotationZ = if (makePhotosFollowShape) when (shapeType) {
                                        com.android.snippets.ui.shapes.AppShape.COOKIE_12_SIDED,
                                        com.android.snippets.ui.shapes.AppShape.PILL,
                                        com.android.snippets.ui.shapes.AppShape.VERY_SUNNY -> animRotation.value
                                        com.android.snippets.ui.shapes.AppShape.PENTAGON -> animRotation.value
                                        else -> 0f
                                    } else 0f
                                    this.scaleX = if (makePhotosFollowShape) animScaleX.value else 1f
                                    this.scaleY = if (makePhotosFollowShape) animScaleY.value else 1f
                                }
                                .clip(cardPhotoShape)
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        this.rotationZ = if (makePhotosFollowShape) when (shapeType) {
                                            com.android.snippets.ui.shapes.AppShape.COOKIE_12_SIDED,
                                            com.android.snippets.ui.shapes.AppShape.PILL,
                                            com.android.snippets.ui.shapes.AppShape.VERY_SUNNY -> -animRotation.value
                                            com.android.snippets.ui.shapes.AppShape.PENTAGON -> -animRotation.value
                                            else -> 0f
                                        } else 0f
                                    }
                            )

                    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.White else Color.Black
                    val iconContainerColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondaryContainer

                    if (photo.rating > 0) {
                        Surface(
                            color = iconContainerColor.copy(alpha = 0.9f),
                            shape = LocalAppShape.current,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp)
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
                                .padding(6.dp)
                                .size(28.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorite",
                                    tint = iconColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    if ((isMostSnippets || isLeastSnippets) && !isSelected) {
                        val badgeContainerColor = if (isMostSnippets) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        val badgeContentColor = if (isMostSnippets) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiary
                        Surface(
                            color = badgeContainerColor.copy(alpha = 0.9f),
                            shape = LocalAppShape.current,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = photo.snippets.size.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = badgeContentColor
                                )
                            }
                        }
                    }                }
                    }
                }

            if (tab != "Eatlist") {
                // Vertical gap running all the way down
                Spacer(modifier = Modifier.width(4.dp))

                // Right side column takes 40% of space
                Column(
                    modifier = Modifier
                        .weight(0.40f)
                        .fillMaxHeight()
                ) {
                    // Snippets section taking full height
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(
                                color = blockColor,
                                shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        contentAlignment = if (photo.snippets.isEmpty()) Alignment.Center else Alignment.CenterStart
                    ) {
                        if (photo.snippets.isEmpty()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_text_snippet_off),
                                contentDescription = "No snippets",
                                tint = (if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            val topSnippets = photo.snippets.take(6)
                            val total = topSnippets.size
                            val verticalPadding = if (total >= 4) 6.dp else 8.dp
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                topSnippets.forEachIndexed { index, snippet ->
                                    val snippetPosition = when {
                                        total == 1 -> CardPosition.Single
                                        index == 0 -> CardPosition.First
                                        index == total - 1 -> CardPosition.Last
                                        else -> CardPosition.Middle
                                    }
                                    val snippetShape = when (snippetPosition) {
                                        CardPosition.Single -> RoundedCornerShape(12.dp)
                                        CardPosition.First -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                                        CardPosition.Middle -> RoundedCornerShape(2.dp)
                                        CardPosition.Last -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
                                    }

                                    val snippetStyle = viewModel.getSnippetStyle(snippet)
                                    val forcedColor = viewModel.getSnippetColor(snippet)
                                    val baseColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (forcedColor != null) Color(forcedColor) else MaterialTheme.colorScheme.primary
                                    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                                    val snippetColor = remember(baseColor, isDark) {
                                        val lum = 0.299f * baseColor.red + 0.587f * baseColor.green + 0.114f * baseColor.blue
                                        if (isDark && lum < 0.3f) baseColor.copy(red = (baseColor.red + 0.4f).coerceAtMost(1f), green = (baseColor.green + 0.4f).coerceAtMost(1f), blue = (baseColor.blue + 0.4f).coerceAtMost(1f))
                                        else if (!isDark && lum > 0.7f) baseColor.copy(red = (baseColor.red - 0.4f).coerceAtLeast(0f), green = (baseColor.green - 0.4f).coerceAtLeast(0f), blue = (baseColor.blue - 0.4f).coerceAtLeast(0f))
                                        else baseColor
                                    }

                                    val snippetGradient = remember(snippetColor) {
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(snippetColor, snippetColor.copy(alpha = 0.40f))
                                        )
                                    }

                                    Surface(
                                        shape = snippetShape,
                                        color = snippetColor.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, snippetColor.copy(alpha = 0.30f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = verticalPadding),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = snippet,
                                                style = getSnippetTextStyle(
                                                    snippetStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default,
                                                    MaterialTheme.typography.titleMedium,
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
        } // closes Row
    } // closes DynamicCardContainer
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
    val shape = if (isSelected) {
        RoundedCornerShape(24.dp)
    } else {
        when (position) {
            CardPosition.Single -> RoundedCornerShape(16.dp)
            CardPosition.First -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
            CardPosition.Middle -> RoundedCornerShape(2.dp)
            CardPosition.Last -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val view = LocalView.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(shape)
            .combinedClickable(
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
        color = if (isSelected) {
            if (containerColor == Color.Transparent) Color.Transparent else MaterialTheme.colorScheme.primary
        } else {
            containerColor ?: MaterialTheme.colorScheme.surfaceContainerHighest
        }
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
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (!photo.locationLink.isNullOrBlank()) {
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = photo.locationLink,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                onEditLink()
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_link),
                            contentDescription = "Add Link",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add link",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                    containerColor = if (!photo.locationLink.isNullOrBlank()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = if (!photo.locationLink.isNullOrBlank()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
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
