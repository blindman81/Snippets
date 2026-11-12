package com.android.snippets.ui

import android.os.Build
import com.android.snippets.ui.theme.getSnippetTextStyle

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.android.snippets.model.Photo
import com.android.snippets.ui.components.PhotoButtonGroup
import com.android.snippets.ui.theme.rememberSnippetBackgroundColor
import com.android.snippets.ui.theme.rememberSnippetTextColor
import com.android.snippets.ui.util.LocationUtils
import com.android.snippets.ui.util.Motion
import com.android.snippets.viewmodel.SnippetsViewModel
import com.ln.android.snippets.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GrayscaleColorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

private fun getSaturationColorFilter(saturation: Float): ColorFilter? = when {
    saturation >= 0.999f -> null
    saturation <= 0.001f -> GrayscaleColorFilter
    else -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(saturation) })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoCard(
    photo: Photo,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    selectedCount: Int = 0,
    userCollections: List<String> = emptyList(),
    locationNameMap: Map<String, String> = emptyMap(),
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showFavoriteIcon: Boolean = true,
    tab: String? = null,
    isMostSnippets: Boolean = false,
    isLeastSnippets: Boolean = false,
    grayOutIfViewed: Boolean = false,
    viewModel: SnippetsViewModel,
    expandLocationIcon: Boolean = false,
    expandLabelIcon: Boolean = false,
    expandCalendarIcon: Boolean = false,
    isExpanded: Boolean = false,
    isAddSnippetOpen: Boolean = false,
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
        targetValue = if (grayOutIfViewed && photo.isViewed) 0f else 1f,
        label = "saturation"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (grayOutIfViewed && photo.isViewed) 0.5f else 1f,
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
        animationSpec = Motion.PhotoCardFade,
        label = "photoCircleBadgesAlpha"
    )

    val view = LocalView.current

    var imageRatio by remember(photo.id, photo.aspectRatio) { mutableStateOf(photo.aspectRatio) }

    val photoShape = RoundedCornerShape(16.dp)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val finalShape = if (isPressed) {
        RoundedCornerShape(24.dp)
    } else {
        RoundedCornerShape(16.dp)
    }
    val blockColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = 2.dp,
                bottom = 2.dp
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(Motion.PhotoExpandSpring)
        ) {
            if (!hasPhoto) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                        .height(240.dp),
                    shape = finalShape,
                    color = blockColor
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
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else rememberSnippetTextColor(),
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
                        .padding(start = 16.dp, end = 16.dp)
                        .aspectRatio(ratio)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                        .height(240.dp)
                }

                Surface(
                    modifier = photoModifier,
                    shape = photoShape,
                    color = blockColor
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(photoShape),
                        contentAlignment = Alignment.Center
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
                                    contentScale = ContentScale.Crop,
                                    onSuccess = { state ->
                                        val w = state.result.image.width
                                        val h = state.result.image.height
                                        if (w > 0 && h > 0) {
                                            val trueRatio = w.toFloat() / h.toFloat()
                                            if (imageRatio == null || kotlin.math.abs((imageRatio ?: 0f) - trueRatio) > 0.01f) {
                                                imageRatio = trueRatio
                                            }
                                        }
                                    },
                                    colorFilter = getSaturationColorFilter(saturation),
                                    modifier = Modifier.fillMaxSize().then(
                                        if (selectionBlurRadius > 0.dp) Modifier.blur(selectionBlurRadius) else Modifier
                                    )
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
                                    )
                                }

                                val effectiveCount = if (selectedCount > 0) selectedCount else (viewModel.selectedPhotoIds.indexOf(photo.id) + 1)
                                SelectedPhotoIndicator(
                                    isSelected = isSelected,
                                    selectionMode = selectionMode,
                                    count = effectiveCount,
                                    modifier = Modifier.align(Alignment.Center)
                                )

                            val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            val iconContainerColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondaryContainer

                            val showCollectionBadges = tab == "Library" || tab == "Favorites"
                            val effectiveUserCollections = if (userCollections.isNotEmpty()) userCollections else viewModel.userCollections
                            val validCollections = remember(photo.collections, effectiveUserCollections, showCollectionBadges) {
                                if (showCollectionBadges) {
                                    photo.collections.filter { effectiveUserCollections.isEmpty() || it in effectiveUserCollections }
                                } else {
                                    emptyList()
                                }
                            }

                            if (circleBadgesAlpha > 0f && (photo.rating > 0 || (photo.isFavorite && showFavoriteIcon) || validCollections.isNotEmpty())) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .graphicsLayer { alpha = circleBadgesAlpha },
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (photo.rating > 0) {
                                        Surface(
                                            modifier = Modifier.size(28.dp),
                                            shape = CircleShape,
                                            color = iconContainerColor,
                                            contentColor = iconColor
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
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
                                            modifier = Modifier.size(28.dp),
                                            shape = CircleShape,
                                            color = iconContainerColor,
                                            contentColor = iconColor
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Favorite,
                                                    contentDescription = "Favorite",
                                                    tint = iconColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    validCollections.forEach { col ->
                                        val iconOrEmoji = viewModel.getCollectionIcon(col)
                                        Surface(
                                            modifier = Modifier.size(28.dp),
                                            shape = CircleShape,
                                            color = iconContainerColor,
                                            contentColor = iconColor
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                when (iconOrEmoji) {
                                                    is String -> {
                                                        Text(
                                                            text = iconOrEmoji,
                                                            fontSize = 14.sp,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                    is ImageVector -> {
                                                        Icon(
                                                            imageVector = iconOrEmoji,
                                                            contentDescription = col,
                                                            tint = iconColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    is Int -> {
                                                        Icon(
                                                            painter = painterResource(id = iconOrEmoji),
                                                            contentDescription = col,
                                                            tint = iconColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    else -> {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Folder,
                                                            contentDescription = col,
                                                            tint = iconColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            val effectiveLocationNameMap = if (locationNameMap.isNotEmpty()) locationNameMap else viewModel.locationNameMap
                            val locationName = remember(photo.locationName, photo.locationLink, effectiveLocationNameMap) {
                                val rawName = photo.locationName?.trim()?.takeIf { it.isNotBlank() }
                                    ?: photo.locationLink?.let { effectiveLocationNameMap[it] }
                                rawName?.takeIf { !it.startsWith("http://", ignoreCase = true) && !it.startsWith("https://", ignoreCase = true) }
                            }

                            if (!locationName.isNullOrBlank() && circleBadgesAlpha > 0f) {
                                ExpandableCardPillIcon(
                                    icon = Icons.Rounded.LocationOn,
                                    text = locationName,
                                    contentDescription = "Photo location",
                                    iconColor = iconColor,
                                    containerColor = iconContainerColor,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .graphicsLayer { alpha = circleBadgesAlpha },
                                    alignEnd = true,
                                    defaultExpanded = expandLocationIcon,
                                    key = "${photo.id}_location"
                                )
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = isExpanded,
                                enter = fadeIn(animationSpec = Motion.PhotoCardFade) + scaleIn(initialScale = 0.85f, animationSpec = Motion.PhotoCardFade),
                                exit = fadeOut(animationSpec = Motion.PhotoCardExitFade) + scaleOut(targetScale = 0.85f, animationSpec = Motion.PhotoCardExitFade),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                PhotoButtonGroup(
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
                                    isPrimaryActive = isAddSnippetOpen,
                                    buttonSize = 56.dp
                                )
                            }

                            val photoName = photo.name
                            if (!photoName.isNullOrBlank() && circleBadgesAlpha > 0f) {
                                ExpandableCardPillIcon(
                                    icon = Icons.AutoMirrored.Rounded.Label,
                                    text = photoName,
                                    contentDescription = "Photo name",
                                    iconColor = iconColor,
                                    containerColor = iconContainerColor,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .graphicsLayer { alpha = circleBadgesAlpha },
                                    defaultExpanded = expandLabelIcon,
                                    key = "${photo.id}_name"
                                )
                            }

                            if (circleBadgesAlpha > 0f) {
                                val formattedDate = remember(photo.date) {
                                    SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(photo.date))
                                }
                                ExpandableCardPillIcon(
                                    icon = Icons.Rounded.CalendarMonth,
                                    text = formattedDate,
                                    contentDescription = "Photo date",
                                    iconColor = iconColor,
                                    containerColor = iconContainerColor,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .graphicsLayer { alpha = circleBadgesAlpha },
                                    alignEnd = true,
                                    defaultExpanded = expandCalendarIcon,
                                    key = "${photo.id}_date"
                                )
                            }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (photo.snippets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(start = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_text_snippet_off),
                            contentDescription = "No snippets",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    val stopNestedScrollConnection = remember {
                        object : NestedScrollConnection {
                            override fun onPostScroll(
                                consumed: Offset,
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                return Offset(available.x, 0f)
                            }

                            override suspend fun onPostFling(
                                consumed: Velocity,
                                available: Velocity
                            ): Velocity {
                                return Velocity(available.x, 0f)
                            }
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { clip = false }
                            .nestedScroll(stopNestedScrollConnection)
                    ) {
                        itemsIndexed(
                            items = photo.snippets,
                            key = { index, snippet -> "${index}_$snippet" }
                        ) { _, snippet ->
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
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else rememberSnippetTextColor(),
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
