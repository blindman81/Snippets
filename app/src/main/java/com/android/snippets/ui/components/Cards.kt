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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.imageLoader
import com.android.snippets.model.Photo

private val GrayscaleColorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

private fun getSaturationColorFilter(saturation: Float): ColorFilter? = when {
    saturation >= 0.999f -> null
    saturation <= 0.001f -> GrayscaleColorFilter
    else -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturation) })
}
@Composable
fun SelectedPhotoIndicator(
    isSelected: Boolean,
    selectionMode: Boolean,
    count: Int,
    modifier: Modifier = Modifier
) {
    if (!isSelected) return

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CookieShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CookieShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Text(
                text = count.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            )
        }
    }
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

    val MorphShapesList = remember { listOf(CookieShape, androidx.compose.foundation.shape.CircleShape) }
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

    val buttonShape = when {
        isForwardArrow -> androidx.compose.foundation.shape.CircleShape
        isBackArrow -> androidx.compose.foundation.shape.CircleShape
        isDownArrow || isUpArrow -> androidx.compose.foundation.shape.CircleShape
        else -> CookieShape
    }
    var isHolding by remember { mutableStateOf(false) }
    var isTapped by remember { mutableStateOf(false) }
    val rotation = remember { androidx.compose.animation.core.Animatable(0f) }
    val animScaleX = remember { androidx.compose.animation.core.Animatable(1f) }
    val animScaleY = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            animScaleX.animateTo(0.92f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
            animScaleY.animateTo(0.92f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
        } else {
            animScaleX.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
            animScaleY.animateTo(1f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.75f, stiffness = 1200f))
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

@Composable
fun SnippetsMasonryRow(
    snippets: List<String>,
    isSelected: Boolean,
    viewModel: SnippetsViewModel?,
    modifier: Modifier = Modifier
) {
    if (snippets.isEmpty()) return

    val candidates = remember(snippets) { snippets.take(2) }
    val spacingPx = with(LocalDensity.current) { 4.dp.roundToPx() }

    Layout(
        content = {
            for (snippet in candidates) {
                SnippetMasonryChip(
                    snippet = snippet,
                    isSelected = isSelected,
                    viewModel = viewModel
                )
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val maxW = if (constraints.hasBoundedWidth) constraints.maxWidth else Int.MAX_VALUE
        if (measurables.isEmpty() || maxW <= 0) {
            return@Layout layout(0, 0) {}
        }

        val p0 = measurables[0].measure(Constraints(maxWidth = maxW))
        var showSecond = false
        var p1: Placeable? = null

        if (measurables.size > 1) {
            val remainingW = maxW - p0.width - spacingPx
            if (remainingW > 0) {
                val unconstrainedP1 = measurables[1].measure(Constraints())
                if (unconstrainedP1.width <= remainingW) {
                    showSecond = true
                    p1 = unconstrainedP1
                }
            }
        }

        val totalW = if (showSecond && p1 != null) {
            p0.width + spacingPx + p1.width
        } else {
            p0.width
        }
        val totalH = if (showSecond && p1 != null) {
            maxOf(p0.height, p1.height)
        } else {
            p0.height
        }

        val layoutWidth = if (constraints.hasBoundedWidth) {
            totalW.coerceIn(constraints.minWidth, constraints.maxWidth)
        } else {
            totalW
        }

        layout(layoutWidth, totalH) {
            p0.placeRelative(0, (totalH - p0.height) / 2)
            if (showSecond && p1 != null) {
                p1.placeRelative(p0.width + spacingPx, (totalH - p1.height) / 2)
            }
        }
    }
}

@Composable
private fun SnippetMasonryChip(
    snippet: String,
    isSelected: Boolean,
    viewModel: SnippetsViewModel?,
    modifier: Modifier = Modifier
) {
    val forcedStyle = viewModel?.getSnippetStyle(snippet)
    val snippetColor = rememberSnippetBackgroundColor()

    Surface(
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else snippetColor,
        border = null,
        modifier = modifier.wrapContentWidth(align = Alignment.Start)
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
                ),
                color = rememberSnippetTextColor(),
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Clip
            )
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
    viewModel: SnippetsViewModel? = null
) {
    val isCustomPolygon = shape is com.android.snippets.ui.shapes.RoundedPolygonShape

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topPhotoCornerAnimated by animateDpAsState(
        targetValue = if (isPressed) 24.dp else 12.dp,
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
        targetValue = if (isPressed) 24.dp else 12.dp,
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
    val selectionBlurRadius by animateDpAsState(
        targetValue = if (isSelected) 18.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "photoSelectionBlur"
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
        val showBottomSection = !fillCard && hasPhoto

        if (!showBottomSection) {
            val finalShape = if (isPressed) RoundedCornerShape(24.dp) else RoundedCornerShape(12.dp)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = finalShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                                            boundsTransform = com.android.snippets.ui.util.Motion.SharedPhotoBoundsTransform,
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
                        val aspectModifier = if (fillCard) {
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
                        }

                        val context = LocalContext.current
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
                            contentDescription = null,
                            contentScale = if (fillCard || isCustomPolygon) ContentScale.Crop else ContentScale.FillWidth,
                            colorFilter = getSaturationColorFilter(saturation),
                            alignment = Alignment.Center,
                            modifier = aspectModifier.then(
                                if (selectionBlurRadius > 0.dp) Modifier.blur(selectionBlurRadius) else Modifier
                            )
                        )

                        if (isSelected) {
                            val wallpaperTint = rememberMonetTintedColor(MaterialTheme.colorScheme.primary, weight = 0.5f)
                            Box(
                                modifier = aspectModifier
                                    .background(wallpaperTint.copy(alpha = 0.28f))
                            )
                        }

                        SelectedPhotoIndicator(
                            isSelected = isSelected,
                            selectionMode = selectionMode,
                            count = if (viewModel != null) viewModel.selectedPhotoIds.indexOf(photo.id) + 1 else 0,
                            modifier = Modifier.align(Alignment.Center)
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
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (photo.snippets.isEmpty()) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_text_snippet_off),
                                contentDescription = "No snippets",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            SnippetsMasonryRow(
                                snippets = photo.snippets,
                                isSelected = isSelected,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = photoShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
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
                                        boundsTransform = com.android.snippets.ui.util.Motion.SharedPhotoBoundsTransform,
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
                    val aspectModifier = if (isCustomPolygon) {
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    } else {
                        photo.aspectRatio?.let { ratio ->
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(ratio)
                        } ?: Modifier.fillMaxWidth().wrapContentHeight()
                    }

                    val context = LocalContext.current
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
                        contentDescription = null,
                        contentScale = if (isCustomPolygon) ContentScale.Crop else ContentScale.FillWidth,
                        colorFilter = getSaturationColorFilter(saturation),
                        alignment = Alignment.Center,
                        modifier = aspectModifier.then(
                            if (selectionBlurRadius > 0.dp) Modifier.blur(selectionBlurRadius) else Modifier
                        )
                    )

                    if (isSelected) {
                        val wallpaperTint = rememberMonetTintedColor(MaterialTheme.colorScheme.primary, weight = 0.5f)
                        Box(
                            modifier = aspectModifier
                                .background(wallpaperTint.copy(alpha = 0.28f))
                        )
                    }

                    SelectedPhotoIndicator(
                        isSelected = isSelected,
                        selectionMode = selectionMode,
                        count = if (viewModel != null) viewModel.selectedPhotoIds.indexOf(photo.id) + 1 else 0,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.Start)
                    .align(Alignment.Start),
                shape = bottomCardShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 0.dp,
                border = null
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentWidth(align = Alignment.Start)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (photo.snippets.isEmpty()) {
                        Box(
                            modifier = Modifier.wrapContentWidth(align = Alignment.Start),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_text_snippet_off),
                                contentDescription = "No snippets",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        SnippetsMasonryRow(
                            snippets = photo.snippets,
                            isSelected = isSelected,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TwoColumnCard(
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
    viewModel: SnippetsViewModel? = null
) {
    PhotoMasonryItem(
        photo = photo,
        isSelected = isSelected,
        selectionMode = selectionMode,
        matchingSnippetsCount = matchingSnippetsCount,
        isMostSnippets = isMostSnippets,
        isLeastSnippets = isLeastSnippets,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        showFavoriteIcon = showFavoriteIcon,
        fillCard = fillCard,
        grayOutIfViewed = grayOutIfViewed,
        shape = shape,
        tab = tab,
        viewModel = viewModel
    )
}

@Composable
fun ThreeColumnCard(
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
    viewModel: SnippetsViewModel? = null
) {
    PhotoMasonryItem(
        photo = photo,
        isSelected = isSelected,
        selectionMode = selectionMode,
        matchingSnippetsCount = matchingSnippetsCount,
        isMostSnippets = isMostSnippets,
        isLeastSnippets = isLeastSnippets,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        showFavoriteIcon = showFavoriteIcon,
        fillCard = fillCard,
        grayOutIfViewed = grayOutIfViewed,
        shape = shape,
        tab = tab,
        viewModel = viewModel
    )
}


@Composable
private fun ExpandableCardPillIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    contentDescription: String,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
    defaultExpanded: Boolean = false,
    key: Any? = null
) {
    val view = LocalView.current
    var isExpanded by remember(key, defaultExpanded) { mutableStateOf(defaultExpanded) }

    Surface(
        color = containerColor.copy(alpha = 0.9f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .padding(8.dp)
            .height(28.dp)
            .widthIn(min = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    isExpanded = !isExpanded
                }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBox = @Composable {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            val textBox = @Composable {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(durationMillis = 150)) + expandHorizontally(
                        expandFrom = if (alignEnd) Alignment.End else Alignment.Start,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(durationMillis = 100)) + shrinkHorizontally(
                        shrinkTowards = if (alignEnd) Alignment.End else Alignment.Start,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = iconColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(
                                start = if (alignEnd) 8.dp else 2.dp,
                                end = if (alignEnd) 2.dp else 8.dp
                            )
                            .widthIn(max = 140.dp)
                    )
                }
            }

            if (alignEnd) {
                textBox()
                iconBox()
            } else {
                iconBox()
                textBox()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoCard(
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
    tab: String? = null,
    isMostSnippets: Boolean = false,
    isLeastSnippets: Boolean = false,
    grayOutIfViewed: Boolean = false,
    viewModel: SnippetsViewModel,
    isExpanded: Boolean = false,
    onAddSnippet: () -> Unit = {},
    onDownload: () -> Unit = {},
    onShare: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onNamePhoto: () -> Unit = {},
    onEditLink: () -> Unit = {},
    onRatePhoto: () -> Unit = {},
    onViewSnippets: () -> Unit = {},
    onDeletePhoto: () -> Unit = {}
) {
    val saturation by animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0.5f else 1f,
        label = "saturation"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0.8f else 1f,
        label = "alpha"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        label = "scale"
    )
    val selectionBlurRadius by animateDpAsState(
        targetValue = if (isSelected) 18.dp else 0.dp,
        label = "photoSelectionBlur"
    )

    val circleBadgesAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "photoCircleBadgesAlpha"
    )

    val view = LocalView.current

    var imageRatio by remember(photo.id, photo.aspectRatio) { mutableStateOf(photo.aspectRatio) }

    val photoShape = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomStart = 4.dp,
        bottomEnd = 4.dp
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topCornerSize by animateDpAsState(
        targetValue = if (isPressed) 24.dp else 4.dp,
        label = "bottomCardTopCorner"
    )
    val bottomCornerSize by animateDpAsState(
        targetValue = if (isPressed) 24.dp else {
            if (position == CardPosition.Last || position == CardPosition.Single) 12.dp else 4.dp
        },
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
        RoundedCornerShape(12.dp)
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
        val showBottomCard = hasPhoto

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
        ) {
            if (!showBottomCard) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = finalShape,
                    color = blockColor,
                    shadowElevation = if (isSelected) 4.dp else 1.dp
                ) {
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
                                        val forcedStyle = viewModel.getSnippetStyle(snippet)
                                        val snippetColor = rememberSnippetBackgroundColor()

                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else snippetColor,
                                            border = null,
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
                                                    ),
                                                    color = rememberSnippetTextColor(),
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Clip
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            } else {
                val photoModifier = if (isExpanded) {
                    val ratio = imageRatio ?: photo.aspectRatio ?: 1f
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                }

                Surface(
                    modifier = photoModifier,
                    shape = photoShape,
                    color = blockColor,
                    shadowElevation = if (isSelected) 4.dp else 1.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoClipShape = photoShape
                            Box(
                                modifier = Modifier.fillMaxSize().then(
                                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                        with(sharedTransitionScope) {
                                            Modifier.sharedBounds(
                                                rememberSharedContentState(key = if (tab != null) "photo_${tab}_${photo.id}" else "photo_${photo.id}"),
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                boundsTransform = com.android.snippets.ui.util.Motion.SharedPhotoBoundsTransform,
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
                                val context = LocalContext.current
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
                                    contentDescription = null,
                                    contentScale = if (isExpanded) ContentScale.Fit else ContentScale.Crop,
                                    onSuccess = { state ->
                                        if (imageRatio == null) {
                                            val w = state.result.drawable.intrinsicWidth
                                            val h = state.result.drawable.intrinsicHeight
                                            if (w > 0 && h > 0) {
                                                imageRatio = w.toFloat() / h.toFloat()
                                            }
                                        }
                                    },
                                    colorFilter = getSaturationColorFilter(saturation),
                                    modifier = Modifier.fillMaxSize().then(
                                        if (selectionBlurRadius > 0.dp) Modifier.blur(selectionBlurRadius) else Modifier
                                    )
                                )

                                if (isSelected) {
                                    val wallpaperTint = rememberMonetTintedColor(MaterialTheme.colorScheme.primary, weight = 0.5f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(wallpaperTint.copy(alpha = 0.28f))
                                    )
                                }

                                SelectedPhotoIndicator(
                                    isSelected = isSelected,
                                    selectionMode = selectionMode,
                                    count = viewModel.selectedPhotoIds.indexOf(photo.id) + 1,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else if (isDark) Color.White else Color.Black
                        val iconContainerColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondaryContainer

                        if (photo.rating > 0 && circleBadgesAlpha > 0f) {
                            Surface(
                                color = iconContainerColor.copy(alpha = 0.9f * circleBadgesAlpha),
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .graphicsLayer { alpha = circleBadgesAlpha }
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

                        if (photo.isFavorite && showFavoriteIcon && circleBadgesAlpha > 0f) {
                            Surface(
                                color = iconContainerColor.copy(alpha = 0.9f * circleBadgesAlpha),
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .graphicsLayer { alpha = circleBadgesAlpha }
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

                        androidx.compose.animation.AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn(animationSpec = tween(250)) + androidx.compose.animation.scaleIn(initialScale = 0.85f),
                            exit = fadeOut(animationSpec = tween(200)) + androidx.compose.animation.scaleOut(targetScale = 0.85f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            PhotoCookieSplitButton(
                                photo = photo,
                                onAdd = onAddSnippet,
                                onDownload = onDownload,
                                onShare = onShare,
                                onToggleFavorite = onToggleFavorite,
                                onNamePhotoClick = onNamePhoto,
                                onAddLinkClick = onEditLink,
                                onRate = onRatePhoto,
                                onSnippets = onViewSnippets,
                                onDelete = onDeletePhoto,
                                buttonSize = 44.dp
                            )
                        }

                        if (!photo.name.isNullOrBlank() && circleBadgesAlpha > 0f) {
                            ExpandableCardPillIcon(
                                icon = Icons.AutoMirrored.Filled.Label,
                                text = photo.name,
                                contentDescription = "Photo name",
                                iconColor = iconColor,
                                containerColor = iconContainerColor,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .graphicsLayer { alpha = circleBadgesAlpha },
                                defaultExpanded = viewModel.expandPhotoNameIcon,
                                key = "${photo.id}_name"
                            )
                        }

                        if (circleBadgesAlpha > 0f) {
                            val formattedDate = remember(photo.date) {
                                SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(photo.date))
                            }
                            ExpandableCardPillIcon(
                                icon = Icons.Default.CalendarMonth,
                                text = formattedDate,
                                contentDescription = "Photo date",
                                iconColor = iconColor,
                                containerColor = iconContainerColor,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .graphicsLayer { alpha = circleBadgesAlpha },
                                alignEnd = true,
                                defaultExpanded = viewModel.expandDayAndDateIcon,
                                key = "${photo.id}_date"
                            )
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
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 0.dp
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
                                    val forcedStyle = viewModel.getSnippetStyle(snippet)
                                    val snippetColor = rememberSnippetBackgroundColor()

                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else snippetColor,
                                        border = null,
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
                                                ),
                                                color = rememberSnippetTextColor(),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Clip
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
}


enum class ViewItem(val columns: Int) {
    PhotoCard(1),
    TwoColumnCard(2),
    ThreeColumnCard(3)
}

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
    useBlur: Boolean = false,
    alpha: Float = 0.55f,
    hazeState: dev.chrisbanes.haze.HazeState? = LocalHazeState.current,
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
        label = "dynamicCardTopCorner"
    )
    val bottomCornerAnimated by animateDpAsState(
        targetValue = bottomCornerTarget,
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
        label = "scale"
    )

    val baseColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else (containerColor ?: MaterialTheme.colorScheme.surfaceContainerHighest)

    val containerModifier = modifier
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
        )

    if (useBlur) {
        BlurredSurface(
            modifier = containerModifier,
            shape = shape,
            color = baseColor,
            alpha = alpha,
            hazeState = hazeState
        ) {
            content()
        }
    } else {
        Surface(
            modifier = containerModifier,
            shape = shape,
            color = baseColor,
            shadowElevation = if (isSelected) 6.dp else 1.dp,
            border = null
        ) {
            content()
        }
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
    
    // Preload the first few visible photos with explicit size so they don't consume uncompressed full-res RAM
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
