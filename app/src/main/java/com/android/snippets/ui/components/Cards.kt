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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LibraryAdd
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocationOn
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

                        if (!isSelected && (photo.rating > 0 || (photo.isFavorite && showFavoriteIcon))) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (photo.rating > 0) {
                                    Surface(
                                        color = iconContainerColor.copy(alpha = 0.9f),
                                        shape = LocalAppShape.current,
                                        modifier = Modifier.size(24.dp)
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

                                if (photo.isFavorite && showFavoriteIcon) {
                                    Surface(
                                        color = iconContainerColor.copy(alpha = 0.9f),
                                        shape = LocalAppShape.current,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Rounded.Favorite,
                                                contentDescription = null,
                                                tint = iconColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val masonryLocationName = remember(photo.locationName, photo.locationLink) {
                            val rawName = photo.locationName?.trim()?.takeIf { it.isNotBlank() }
                                ?: LocationUtils.getLocationFromExif(null, photo)?.trim()
                            rawName?.takeIf { !it.startsWith("http://", ignoreCase = true) && !it.startsWith("https://", ignoreCase = true) }
                        }

                        if (!masonryLocationName.isNullOrBlank() && !isSelected) {
                            ExpandableCardPillIcon(
                                icon = Icons.Rounded.LocationOn,
                                text = masonryLocationName,
                                contentDescription = "Photo location",
                                iconColor = iconColor,
                                containerColor = iconContainerColor,
                                modifier = Modifier.align(Alignment.TopEnd),
                                alignEnd = true,
                                defaultExpanded = false,
                                key = "${photo.id}_masonry_location"
                            )
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

                    if (!isSelected && (photo.rating > 0 || (photo.isFavorite && showFavoriteIcon))) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (photo.rating > 0) {
                                Surface(
                                    color = iconContainerColor.copy(alpha = 0.9f),
                                    shape = LocalAppShape.current,
                                    modifier = Modifier.size(24.dp)
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

                            if (photo.isFavorite && showFavoriteIcon) {
                                Surface(
                                    color = iconContainerColor.copy(alpha = 0.9f),
                                    shape = LocalAppShape.current,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.Favorite,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val masonryLocationName2 = remember(photo.locationName, photo.locationLink) {
                        val rawName = photo.locationName?.trim()?.takeIf { it.isNotBlank() }
                            ?: LocationUtils.getLocationFromExif(null, photo)?.trim()
                        rawName?.takeIf { !it.startsWith("http://", ignoreCase = true) && !it.startsWith("https://", ignoreCase = true) }
                    }

                    if (!masonryLocationName2.isNullOrBlank() && !isSelected) {
                        ExpandableCardPillIcon(
                            icon = Icons.Rounded.LocationOn,
                            text = masonryLocationName2,
                            contentDescription = "Photo location",
                            iconColor = iconColor,
                            containerColor = iconContainerColor,
                            modifier = Modifier.align(Alignment.TopEnd),
                            alignEnd = true,
                            defaultExpanded = false,
                            key = "${photo.id}_masonry2_location"
                        )
                    }
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
fun ExpandableCardPillIcon(
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
