package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import com.android.snippets.ui.shapes.LocalAppShape
import com.android.snippets.ui.shapes.LocalAppShapeType
import com.android.snippets.ui.shapes.AppShape
import androidx.compose.material3.carousel.rememberCarouselState
import com.android.snippets.viewmodel.Screen
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
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    showFavoriteIcon: Boolean = true,
    fillCard: Boolean = false,
    grayOutIfViewed: Boolean = false,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(0.dp)
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .alpha(animatedAlpha)
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
                    color = MaterialTheme.colorScheme.primary,
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
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            if (matchingSnippetsCount > 0 && !isSelected) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = LocalAppShape.current,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = matchingSnippetsCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        }
    }
}

enum class CardPosition {
    Single, First, Middle, Last
}

@Composable
fun DynamicCardContainer(
    modifier: Modifier = Modifier,
    position: CardPosition = CardPosition.Single,
    isSelected: Boolean = false,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = if (isSelected) {
        RoundedCornerShape(24.dp)
    } else {
        when (position) {
            CardPosition.Single -> RoundedCornerShape(16.dp)
            CardPosition.First -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
            CardPosition.Middle -> RoundedCornerShape(4.dp)
            CardPosition.Last -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val view = LocalView.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = shape,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            containerColor ?: MaterialTheme.colorScheme.surfaceContainerHighest
        },
        onClick = { 
            onClick?.let {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                it.invoke()
            }
        },
        enabled = onClick != null
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
    containerColor: Color? = null
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val secondaryColor = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    DynamicCardContainer(
        onClick = onClick,
        position = position,
        isSelected = isSelected,
        containerColor = containerColor
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
                modifier = Modifier.size(if (isExpressive) 36.dp else 32.dp),
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
                        style = if (isExpressive) {
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
                        viewModel.openDetail(photo.id, screen)
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
