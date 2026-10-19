package com.android.snippets.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.luminance



import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.gestures.detectTapGestures



import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.android.snippets.ui.components.LoadingIndicator
import com.android.snippets.ui.components.BlurredSurface
import com.android.snippets.ui.components.BlurredAlertDialog
import com.android.snippets.ui.components.AnimatedCookieButton
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.theme.*
import dev.chrisbanes.haze.hazeSource

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import android.content.Context
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import android.view.HapticFeedbackConstants
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.android.snippets.ui.util.rotateWithBounds
import com.android.snippets.ui.util.LocationUtils
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import com.ln.android.snippets.R
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.model.Photo
import com.android.snippets.viewmodel.Screen
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.random.Random
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import kotlin.math.roundToInt
import kotlin.math.abs

internal val DetailPhotoCornerRadius = 0.dp

// --- MAIN DETAIL SCREEN ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    viewModel: SnippetsViewModel,
    windowSizeClass: WindowSizeClass? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val view = LocalView.current
    
    val sharedUiAlpha = remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    val sharedBgAlpha = remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    
    val photos = remember(
        viewModel.libraryCurrentTab,
        viewModel.filteredPhotos,
        viewModel.filteredFavoritesPhotos,
        viewModel.detailReturnScreen,
        viewModel.curatedMemories,
        viewModel.getPhotoSortTypeFor(viewModel.libraryCurrentTab)
    ) {
        if (viewModel.detailReturnScreen == Screen.Memory) {
            viewModel.curatedMemories
        } else {
            when (viewModel.libraryCurrentTab) {
                "Library" -> {
                    viewModel.sortPhotos(viewModel.filteredPhotos, viewModel.getPhotoSortTypeFor("Library"))
                }
                "Favorites" -> viewModel.filteredFavoritesPhotos.values.flatten()
                else -> {
                    val collection = viewModel.libraryCurrentTab
                    val filtered = viewModel.filteredPhotos.filter { it.collections.contains(collection) }
                    viewModel.sortPhotos(filtered, viewModel.getPhotoSortTypeFor(collection))
                }
            }
        }
    }

    val activeId: String? = viewModel.activePhotoId
    val initialPage = photos.indexOfFirst { it.id == activeId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { photos.size }
    val transitionTargetId = photos.getOrNull(pagerState.currentPage)?.id
    
    // Sync current photo with VM for external state consistency
    LaunchedEffect(pagerState.currentPage) {
        viewModel.activePhotoId = photos[pagerState.currentPage].id
    }

    // Handle external updates to activePhotoId (e.g. from deep links)
    LaunchedEffect(viewModel.activePhotoId) {
        val targetPage = photos.indexOfFirst { it.id == viewModel.activePhotoId }
        if (targetPage != -1 && targetPage != pagerState.currentPage) {
            pagerState.scrollToPage(targetPage)
        }
    }
    
    val photo = photos.getOrNull(pagerState.currentPage) ?: return
    
    AppPredictiveBackHandler {
        viewModel.closeDetail()
    }


    var showSnippetsModal by remember { mutableStateOf(false) }
    var showAddModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }
    var showRateModal by remember { mutableStateOf(false) }
    var showLinkModal by remember { mutableStateOf(false) }
    var showNameModal by remember { mutableStateOf(false) }

    val currentPhoto = viewModel.photos.find { it.id == photo.id } ?: photos.getOrNull(pagerState.currentPage) ?: photo
    var isInfoExpanded by remember(currentPhoto.id) { mutableStateOf(false) }

    LaunchedEffect(viewModel.pendingOpenAddSnippetDialog, photo.id) {
        if (viewModel.pendingOpenAddSnippetDialog && viewModel.activePhotoId == photo.id) {
            showAddModal = true
            viewModel.pendingOpenAddSnippetDialog = false
        }
    }

    val context = LocalContext.current
    val isAnyPopupActive = showSnippetsModal || showAddModal || showDeleteModal || showRateModal || showLinkModal || showNameModal
    
    val scrollStates = remember { mutableStateMapOf<Int, Boolean>() }
    val isScrolled = scrollStates[pagerState.currentPage] ?: false
    val scrollToTopActions = remember { mutableStateMapOf<Int, () -> Unit>() }


    val focusManager = LocalFocusManager.current
    val hazeState = dev.chrisbanes.haze.rememberHazeState()

    androidx.compose.runtime.CompositionLocalProvider(
        com.android.snippets.ui.components.LocalHazeState provides hazeState,
        com.android.snippets.ui.util.LocalTiltActive provides (com.android.snippets.ui.util.LocalTiltActive.current && !isAnyPopupActive)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = sharedBgAlpha.floatValue }
                    .background(MaterialTheme.colorScheme.surface)
            )
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            Modifier.hazeSource(state = hazeState)
                        } else Modifier
                    ),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Box(modifier = Modifier.graphicsLayer { alpha = sharedUiAlpha.floatValue }) {
                    val hasSnippetsCurrent = currentPhoto.snippets.isNotEmpty()
                    val isDarkCard = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    DetailTopBar(
                        photo = currentPhoto,
                        viewModel = viewModel,
                        onBack = { viewModel.closeDetail() },
                        isSpinning = !isAnyPopupActive,
                        isScrolled = isScrolled,
                        onPhotoThumbnailClick = { scrollToTopActions[pagerState.currentPage]?.invoke() },
                        hasSnippets = hasSnippetsCurrent,
                        showAddButton = true,
                        isAddActive = showAddModal,
                        onAdd = { showAddModal = true },
                        onDownload = { 
                            viewModel.downloadPhotoCard(context, currentPhoto, isDarkCard, if (isDarkCard) android.graphics.Color.BLACK else android.graphics.Color.WHITE) 
                        },
                        onSnippets = { showSnippetsModal = true },
                        onShare = { viewModel.sharePhotoCard(context, currentPhoto, isDarkCard, if (isDarkCard) android.graphics.Color.BLACK else android.graphics.Color.WHITE) },
                        onDelete = { showDeleteModal = true },
                        onRate = { showRateModal = true },
                        isFavorite = currentPhoto.isFavorite,
                        onToggleFavorite = { viewModel.toggleFavorite(currentPhoto.id) },
                        hasLocationLink = !currentPhoto.locationLink.isNullOrBlank(),
                        onAddLinkClick = { showLinkModal = true },
                        onNamePhotoClick = { showNameModal = true },
                        isInfoExpanded = isInfoExpanded,
                        onToggleInfoExpand = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            isInfoExpanded = !isInfoExpanded
                        },
                        onOpenLink = { url ->
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val formatted = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(formatted))
                                    context.startActivity(intent)
                                } catch (e2: Exception) {
                                    e2.printStackTrace()
                                }
                            }
                        },
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }
        ) { _ ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { index -> photos[index].id },
                    pageSpacing = 4.dp,
                    contentPadding = PaddingValues(0.dp),
                    beyondViewportPageCount = 0
                ) { page ->
                    val pagePhoto = photos[page]
                    val hasSnippets = pagePhoto.snippets.isNotEmpty()
                    
                    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                        if (!hasSnippets) {
                            val sharedKey = if (viewModel.detailReturnScreen == Screen.Memory) {
                                "memory_${pagePhoto.id}"
                            } else {
                                "photo_${viewModel.libraryCurrentTab}_${pagePhoto.id}"
                            }
                            EmptyDetailContent(
                                photo = pagePhoto,
                                sharedKey = sharedKey,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                isTransitionTarget = pagePhoto.id == transitionTargetId,
                                onDismissRequest = { 
                                    viewModel.closeDetail() 
                                },
                                onScrollChanged = { scrolled -> scrollStates[page] = scrolled },
                                onRegisterScrollToTop = { action -> scrollToTopActions[page] = action },
                                viewModel = viewModel,
                                windowSizeClass = windowSizeClass,
                                onUiAlphaChange = { if (pagerState.currentPage == page) sharedUiAlpha.floatValue = it },
                                onBgAlphaChange = { if (pagerState.currentPage == page) sharedBgAlpha.floatValue = it },
                                pageOffset = pageOffset,
                                onOpenLink = { url ->
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val formatted = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(formatted))
                                            context.startActivity(intent)
                                        } catch (e2: Exception) {
                                            e2.printStackTrace()
                                        }
                                    }
                                },
                                onEditLink = { showLinkModal = true }
                            )
                        } else {
                            val sharedKey = if (viewModel.detailReturnScreen == Screen.Memory) {
                                "memory_${pagePhoto.id}"
                            } else {
                                "photo_${viewModel.libraryCurrentTab}_${pagePhoto.id}"
                            }
                            SnippetsDetailContent(
                                photo = pagePhoto,
                                sharedKey = sharedKey,
                                userCollections = viewModel.userCollections,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                isTransitionTarget = pagePhoto.id == transitionTargetId,
                                onDismissRequest = { 
                                    viewModel.closeDetail() 
                                },
                                onScrollChanged = { scrollStates[page] = it },
                                onRegisterScrollToTop = { scrollToTopActions[page] = it },
                                viewModel = viewModel,
                                windowSizeClass = windowSizeClass,
                                onUiAlphaChange = { if (pagerState.currentPage == page) sharedUiAlpha.floatValue = it },
                                onBgAlphaChange = { if (pagerState.currentPage == page) sharedBgAlpha.floatValue = it },
                                pageOffset = pageOffset,
                                onOpenLink = { url ->
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val formatted = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(formatted))
                                            context.startActivity(intent)
                                        } catch (e2: Exception) {
                                            e2.printStackTrace()
                                        }
                                    }
                                },
                                onEditLink = { showLinkModal = true },
                                onSnippets = { showSnippetsModal = true }
                            )
                        }
                    }
                }
            }
        }
    }

        if (showSnippetsModal) {
            SnippetsModal(
                photo = photo,
                onRemove = { viewModel.removeSnippet(photo.id, it) },
                onClose = { showSnippetsModal = false },
                viewModel = viewModel
            )
        }

        AnimatedVisibility(
            visible = showAddModal,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            AddSnippetsModal(
                photo = photo,
                onAdd = { name -> viewModel.updateSnippets(photo.id, name) },
                onClose = { showAddModal = false },
                viewModel = viewModel,
            )
        }
    }



    if (showDeleteModal) {
        DeleteConfirmationModal(
            hasPublicPhotos = photo.isPublic,
            onDismiss = { showDeleteModal = false },
            onConfirm = { unpublish -> 
                showDeleteModal = false
                viewModel.deletePhoto(photo.id, unpublish = unpublish)
            }
        )
    }

    if (showRateModal) {
        RateFoodDialog(
            initialRating = photo.rating,
            onDismiss = { showRateModal = false },
            onConfirm = { newRating ->
                showRateModal = false
                viewModel.setPhotoRating(photo.id, newRating)
            }
        )
    }

    if (showLinkModal) {
        LocationLinkModal(
            initialLink = photo.locationLink,
            suggestions = viewModel.allUniqueLocations,
            onDismiss = { showLinkModal = false },
            onSave = { link, name ->
                showLinkModal = false
                viewModel.updateLocationLink(photo.id, link.takeIf { it.isNotBlank() }, name)
            }
        )
    }

    if (showNameModal) {
        NamePhotoModal(
            initialName = photo.name,
            onDismiss = { showNameModal = false },
            onSave = { name ->
                showNameModal = false
                viewModel.updatePhotoName(photo.id, name)
            }
        )
    }
    }
}

@Composable
fun DeleteConfirmationModal(
    count: Int = 1,
    hasPublicPhotos: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    val view = androidx.compose.ui.platform.LocalView.current
    var unpublish by remember { mutableStateOf(true) }
    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (count > 1) "Delete $count photos?" else "Delete photo?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (count > 1) "Deleting these photos also deletes the snippets you created for them." else "Deleting this photo also deletes the snippets you created for this photo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (hasPublicPhotos) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Checkbox(
                            checked = unpublish,
                            onCheckedChange = { unpublish = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Also remove from public community feed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        onConfirm = {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            onConfirm(unpublish)
        }
    )
}
data class DetailDismissConfig(
    val targetWidthPx: Float,
    val targetHeightPx: Float,
    val targetScale: Float = 1f,
    val targetShape: (progress: Float) -> Shape
)

@Composable
fun rememberDetailDismissConfig(
    photo: com.android.snippets.model.Photo,
    viewModel: SnippetsViewModel?,
    windowSizeClass: WindowSizeClass?
): DetailDismissConfig {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val cardPaddingPx = with(density) { 12.dp.toPx() }
    val photoCardHeightPx = with(density) { 262.dp.toPx() }
    val targetCardWidthPx = screenWidthPx - (cardPaddingPx * 2f)

    return remember(photo.id, photo.aspectRatio, viewModel?.detailReturnScreen, viewModel?.customGridColumns, windowSizeClass, screenWidthPx) {
        val isMemory = viewModel?.detailReturnScreen == Screen.Memory
        val rawColumns = viewModel?.customGridColumns?.coerceIn(-1, 3)
        val currentGridColumns = if (rawColumns == 1) -1 else (rawColumns ?: when (windowSizeClass?.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> 3
            WindowWidthSizeClass.Medium -> 3
            else -> -1
        })

        val (targetW, targetH, targetScale) = when {
            isMemory -> {
                val size = screenWidthPx - with(density) { 48.dp.toPx() }
                Triple(size, size, 0.72f)
            }
            currentGridColumns == 3 -> {
                val colW = (screenWidthPx - with(density) { (12.dp * 2 + 8.dp * 2).toPx() }) / 3f
                val colH = photo.aspectRatio?.let { colW / it } ?: (colW * 1.33f)
                Triple(colW, colH, 0.33f)
            }
            currentGridColumns == 2 -> {
                val colW = (screenWidthPx - with(density) { (12.dp * 2 + 8.dp).toPx() }) / 2f
                val colH = photo.aspectRatio?.let { colW / it } ?: (colW * 1.33f)
                Triple(colW, colH, 0.48f)
            }
            else -> {
                Triple(targetCardWidthPx, photoCardHeightPx, 1.0f)
            }
        }

        val targetShape: (Float) -> Shape = { progress ->
            val p = progress.coerceIn(0f, 1f)
            when {
                p <= 0f -> RectangleShape
                isMemory -> {
                    if (p < 0.2f) {
                        RoundedCornerShape((32f * (p / 0.2f)).dp)
                    } else {
                        CookieShape
                    }
                }
                else -> {
                    RoundedCornerShape(
                        topStart = (12f * p).dp,
                        topEnd = (12f * p).dp,
                        bottomStart = (4f * p).dp,
                        bottomEnd = (4f * p).dp
                    )
                }
            }
        }

        DetailDismissConfig(
            targetWidthPx = targetW,
            targetHeightPx = targetH,
            targetScale = targetScale,
            targetShape = targetShape
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DetailPhotoFrame(
    photo: com.android.snippets.model.Photo,
    sharedKey: String,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    isTransitionTarget: Boolean,
    modifier: Modifier = Modifier,
    showLoading: Boolean = false,
    shapeProvider: (() -> Shape)? = null,
    offsetProvider: (() -> IntOffset)? = null,
    progressProvider: (() -> Float)? = null,
    dismissConfig: DetailDismissConfig? = null
) {
    var imageRatio by remember(photo.id) { mutableStateOf(photo.aspectRatio) }
    val photoModifier = (imageRatio ?: photo.aspectRatio)?.let { Modifier.aspectRatio(it) } ?: modifier

    Card(
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .then(photoModifier)
            .offset { offsetProvider?.invoke() ?: IntOffset.Zero }
            .then(
                if (isTransitionTarget && sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedBounds(
                            rememberSharedContentState(key = sharedKey),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = com.android.snippets.ui.util.Motion.SharedPhotoBoundsTransform,
                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            )
                        )
                    }
                } else {
                    Modifier
                }
            )
            .graphicsLayer {
                if (shapeProvider != null) {
                    val currentShape = shapeProvider()
                    shape = currentShape
                    clip = currentShape != RectangleShape
                }
            },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            val imageRequest = remember(photo.uriString) {
                coil.request.ImageRequest.Builder(context)
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
                    if (imageRatio == null) {
                        val w = state.result.drawable.intrinsicWidth
                        val h = state.result.drawable.intrinsicHeight
                        if (w > 0 && h > 0) {
                            imageRatio = w.toFloat() / h.toFloat()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun EmptyDetailContent(
    photo: com.android.snippets.model.Photo,
    sharedKey: String,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    isTransitionTarget: Boolean = false,
    onDismissRequest: () -> Unit = {},
    onScrollChanged: (Boolean) -> Unit = {},
    onRegisterScrollToTop: (() -> Unit) -> Unit = {},
    viewModel: SnippetsViewModel? = null,
    windowSizeClass: WindowSizeClass? = null,
    onUiAlphaChange: (Float) -> Unit = {},
    onBgAlphaChange: (Float) -> Unit = {},
    pageOffset: Float = 0f,
    onOpenLink: (String) -> Unit = {},
    onEditLink: () -> Unit = {}
) {
    val dismissConfig = rememberDetailDismissConfig(photo, viewModel, windowSizeClass)

    SwipeToDismissContainer(
        onDismiss = onDismissRequest,
        targetScale = dismissConfig.targetScale,
        shapeProvider = dismissConfig.targetShape,
        onUiAlphaChange = onUiAlphaChange,
        onBgAlphaChange = onBgAlphaChange
    ) { currentShapeProvider, progressProvider, offsetProvider ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DetailPhotoFrame(
                photo = photo,
                sharedKey = sharedKey,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                isTransitionTarget = isTransitionTarget,
                modifier = photo.aspectRatio?.let { Modifier.aspectRatio(it) } ?: Modifier.fillMaxSize(),
                shapeProvider = currentShapeProvider,
                offsetProvider = offsetProvider,
                progressProvider = progressProvider,
                dismissConfig = dismissConfig
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SnippetsDetailContent(
    photo: com.android.snippets.model.Photo,
    sharedKey: String,
    userCollections: List<String> = emptyList(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    isTransitionTarget: Boolean = false,
    onDismissRequest: () -> Unit = {},
    onScrollChanged: (Boolean) -> Unit = {},
    onRegisterScrollToTop: (() -> Unit) -> Unit = {},
    viewModel: SnippetsViewModel,
    windowSizeClass: WindowSizeClass? = null,
    onUiAlphaChange: (Float) -> Unit = {},
    onBgAlphaChange: (Float) -> Unit = {},
    pageOffset: Float = 0f,
    onOpenLink: (String) -> Unit = {},
    onEditLink: () -> Unit = {},
    onSnippets: () -> Unit = {}
) {
    val dismissConfig = rememberDetailDismissConfig(photo, viewModel, windowSizeClass)

    SwipeToDismissContainer(
        onDismiss = onDismissRequest,
        targetScale = dismissConfig.targetScale,
        shapeProvider = dismissConfig.targetShape,
        onUiAlphaChange = onUiAlphaChange,
        onBgAlphaChange = onBgAlphaChange,
        overlayContent = { _, uiAlphaProvider ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .then(
                            if (animatedVisibilityScope != null) {
                                with(animatedVisibilityScope) {
                                    val motionScheme = MaterialTheme.motionScheme
                                    Modifier.animateEnterExit(
                                        enter = fadeIn(animationSpec = motionScheme.fastEffectsSpec()) + slideInVertically(animationSpec = motionScheme.fastSpatialSpec()) { it / 2 },
                                        exit = fadeOut(animationSpec = motionScheme.fastEffectsSpec()) + slideOutVertically(animationSpec = motionScheme.fastSpatialSpec()) { it / 2 }
                                    )
                                }
                            } else Modifier
                        )
                        .graphicsLayer { alpha = uiAlphaProvider() }
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val pureSnippets = photo.snippets
                    val total = pureSnippets.size

                    val containerInteractionSource = remember { MutableInteractionSource() }
                    val isContainerPressed by containerInteractionSource.collectIsPressedAsState()
                    val containerCornerRadius by animateDpAsState(
                        targetValue = if (isContainerPressed) 20.dp else 12.dp,
                        label = "snippetContainerRadius"
                    )

                    val snippetRenderer: @Composable (String) -> Unit = { snippet ->
                        val forcedStyle = viewModel.getSnippetStyle(snippet)
                        val snippetColor = rememberSnippetBackgroundColor()

                        val view = LocalView.current

                        Surface(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onSnippets()
                            },
                            shape = CircleShape,
                            color = snippetColor,
                            border = null,
                            modifier = Modifier.height(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = snippet,
                                    style = getSnippetTextStyle(
                                        forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default,
                                        MaterialTheme.typography.titleMedium,
                                        isCloud = true
                                    ),
                                    color = rememberSnippetTextColor(),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (pureSnippets.size == 1) {
                        FlowRow(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .widthIn(max = 600.dp)
                                .wrapContentWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            snippetRenderer(pureSnippets[0])
                        }
                    } else if (pureSnippets.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .widthIn(max = 600.dp)
                                .clip(RoundedCornerShape(containerCornerRadius))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .combinedClickable(
                                    interactionSource = containerInteractionSource,
                                    indication = null,
                                    onClick = {},
                                    onLongClick = {}
                                )
                        ) {
                            FlowRow(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                                    .wrapContentWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pureSnippets.forEach { snippet ->
                                    snippetRenderer(snippet)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { currentShapeProvider, progressProvider, offsetProvider ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DetailPhotoFrame(
                photo = photo,
                sharedKey = sharedKey,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                isTransitionTarget = isTransitionTarget,
                modifier = photo.aspectRatio?.let { Modifier.aspectRatio(it) } ?: Modifier.fillMaxSize(),
                shapeProvider = currentShapeProvider,
                offsetProvider = offsetProvider,
                progressProvider = progressProvider,
                dismissConfig = dismissConfig
            )
        }
    }
}

@Composable
fun RateFoodDialog(
    initialRating: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val view = androidx.compose.ui.platform.LocalView.current
    var rating by remember { mutableStateOf(initialRating) }
    
    // Stamp animation for the star and count box
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(rating) {
        scale.snapTo(1f)
        scale.animateTo(
            targetValue = 0.75f,
            animationSpec = tween(durationMillis = 80, easing = FastOutLinearInEasing)
        )
        scale.animateTo(
            targetValue = 1.3f,
            animationSpec = tween(durationMillis = 120, easing = LinearOutSlowInEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        onConfirm = {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
            onConfirm(rating)
        },
        title = {
            Text(
                text = "Rate",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                            if (rating > 0) rating--
                        },
                        enabled = rating > 0,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease rating",
                            tint = if (rating > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Cookie 9-sided container for the star
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CookieShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                                if (rating < 5) rating++
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(68.dp)
                                .graphicsLayer {
                                    scaleX = scale.value
                                    scaleY = scale.value
                                }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_star_rating),
                                contentDescription = "Star icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = rating.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                            if (rating < 5) rating++
                        },
                        enabled = rating < 5,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase rating",
                            tint = if (rating < 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun LocationLinkModal(
    initialLink: String?,
    suggestions: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    var linkValue by remember { mutableStateOf(initialLink ?: "") }
    val resolvedNames = remember { mutableStateMapOf<String, String>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(suggestions) {
        suggestions.forEach { suggestion ->
            if (suggestion.startsWith("http")) {
                val localName = LocationUtils.extractPlaceNameFromLink(suggestion)
                if (localName != null) {
                    resolvedNames[suggestion] = LocationUtils.cleanLocationName(localName)
                } else {
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        var connection: java.net.HttpURLConnection? = null
                        try {
                            var currentUrl = suggestion
                            var redirects = 0
                            while (redirects < 5) {
                                val url = java.net.URL(currentUrl)
                                connection = url.openConnection() as java.net.HttpURLConnection
                                connection.instanceFollowRedirects = false
                                connection.connectTimeout = 3000
                                connection.readTimeout = 3000
                                connection.requestMethod = "HEAD"
                                val responseCode = connection.responseCode
                                if (responseCode in 300..399) {
                                    val loc = connection.getHeaderField("Location")
                                    if (!loc.isNullOrBlank()) {
                                        currentUrl = if (loc.startsWith("http")) loc else {
                                            val baseUri = android.net.Uri.parse(currentUrl)
                                            baseUri.buildUpon().path(loc).build().toString()
                                        }
                                        redirects++
                                    } else {
                                        break
                                    }
                                } else {
                                    break
                                }
                            }
                            connection?.disconnect()
                            connection = null
                            
                            val name = LocationUtils.extractPlaceNameFromLink(currentUrl)
                            if (name != null) {
                                resolvedNames[suggestion] = LocationUtils.cleanLocationName(name)
                            }
                        } catch (e: Exception) {
                            try {
                                var currentUrl = suggestion
                                var redirects = 0
                                while (redirects < 5) {
                                    val url = java.net.URL(currentUrl)
                                    connection = url.openConnection() as java.net.HttpURLConnection
                                    connection.instanceFollowRedirects = false
                                    connection.connectTimeout = 3000
                                    connection.readTimeout = 3000
                                    connection.requestMethod = "GET"
                                    val responseCode = connection.responseCode
                                    if (responseCode in 300..399) {
                                        val loc = connection.getHeaderField("Location")
                                        if (!loc.isNullOrBlank()) {
                                            currentUrl = if (loc.startsWith("http")) loc else {
                                                val baseUri = android.net.Uri.parse(currentUrl)
                                                baseUri.buildUpon().path(loc).build().toString()
                                            }
                                            redirects++
                                        } else {
                                            break
                                        }
                                    } else {
                                        break
                                    }
                                }
                                connection?.disconnect()
                                connection = null
                                
                                val name = LocationUtils.extractPlaceNameFromLink(currentUrl)
                                if (name != null) {
                                    resolvedNames[suggestion] = LocationUtils.cleanLocationName(name)
                                }
                            } catch (ex: Exception) {
                                // ignore
                            }
                        } finally {
                            connection?.disconnect()
                        }
                    }
                }
            } else {
                resolvedNames[suggestion] = LocationUtils.cleanLocationName(suggestion)
            }
        }
    }
    
    val filteredSuggestions = remember(linkValue, suggestions, resolvedNames.toMap()) {
        val baseList = if (initialLink != null) {
            suggestions.filter { it != initialLink }
        } else {
            suggestions
        }
        
        if (linkValue.isBlank()) {
            baseList.take(8)
        } else {
            baseList.filter { suggestion ->
                val displayName = resolvedNames[suggestion] ?: LocationUtils.extractPlaceNameFromLink(suggestion) ?: LocationUtils.cleanLocationName(suggestion)
                displayName.contains(linkValue, ignoreCase = true) || suggestion.contains(linkValue, ignoreCase = true)
            }.take(8)
        }
    }

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            val name = resolvedNames[linkValue] ?: LocationUtils.extractPlaceNameFromLink(linkValue) ?: LocationUtils.cleanLocationName(linkValue)
            val finalName = if (name.startsWith("http")) null else LocationUtils.cleanLocationName(name)
            onSave(linkValue, finalName)
        },
        title = {
            Text(
                text = "Link to a place",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Paste a Google Maps or location link below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = linkValue,
                    onValueChange = { linkValue = it },
                    placeholder = { Text("https://maps.app.goo.gl/...") },
                    singleLine = true,
                    leadingIcon = {
                        IconButton(onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            coroutineScope.launch {
                                val clipEntry = clipboard.getClipEntry()
                                val textToPaste = clipEntry?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                                    ?: run {
                                        val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        sysClipboard?.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                                    }
                                if (!textToPaste.isNullOrBlank()) {
                                    val urlRegex = Regex("""https?://\S+""")
                                    val extractedUrl = urlRegex.find(textToPaste)?.value
                                    linkValue = extractedUrl ?: textToPaste.trim()
                                }
                            }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                        }
                    },
                    trailingIcon = if (linkValue.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                linkValue = ""
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (filteredSuggestions.isNotEmpty()) {
                    Text(
                        text = "Suggested locations",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredSuggestions) { suggestion ->
                            val displayName = remember(suggestion, resolvedNames[suggestion]) {
                                resolvedNames[suggestion] ?: LocationUtils.extractPlaceNameFromLink(suggestion) ?: LocationUtils.cleanLocationName(suggestion)
                            }
                            SuggestionChip(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    linkValue = suggestion
                                },
                                label = {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = null
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun NamePhotoModal(
    initialName: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    var nameValue by remember { mutableStateOf(initialName ?: "") }

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onSave(nameValue.trim().takeIf { it.isNotBlank() })
        },
        title = {
            Text(
                text = "Name photo",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nameValue,
                    onValueChange = {
                        if (it.length <= 25) {
                            nameValue = it
                        }
                    },
                    placeholder = { Text("Photo name") },
                    singleLine = true,
                    leadingIcon = {
                        IconButton(onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            coroutineScope.launch {
                                val clipEntry = clipboard.getClipEntry()
                                val textToPaste = clipEntry?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                                    ?: run {
                                        val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        sysClipboard?.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                                    }
                                if (!textToPaste.isNullOrBlank()) {
                                    nameValue = textToPaste.trim().replace("\n", " ").replace("\r", "").take(25)
                                }
                            }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                        }
                    },
                    trailingIcon = if (nameValue.isNotEmpty()) {
                        {
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    nameValue = ""
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = "${nameValue.length}/25",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (nameValue.length >= 25) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = if (nameValue.isNotEmpty()) 36.dp else 12.dp)
                )
            }
        }
    )
}

@Composable
fun SwipeToDismissContainer(
    onDismiss: () -> Unit,
    targetScale: Float = 0.65f,
    shapeProvider: (progress: Float) -> Shape = { RectangleShape },
    onUiAlphaChange: (Float) -> Unit = {},
    onBgAlphaChange: (Float) -> Unit = {},
    overlayContent: @Composable (bgAlphaProvider: () -> Float, uiAlphaProvider: () -> Float) -> Unit = { _, _ -> },
    content: @Composable (shapeProvider: () -> Shape, progressProvider: () -> Float, offsetProvider: () -> IntOffset) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val animOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val animOffsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    var isSettling by remember { mutableStateOf(false) }

    // Define how far the user needs to drag to trigger the dismissal
    val density = androidx.compose.ui.platform.LocalDensity.current
    val dismissThreshold = with(density) { 90.dp.toPx() }
    val maxDrag = with(density) { 180.dp.toPx() }
    val flingVelocityThreshold = with(density) { 400.dp.toPx() }
    val quickResizeDistance = with(density) { 50.dp.toPx() }

    val velocityTracker = remember { androidx.compose.ui.input.pointer.util.VelocityTracker() }

    val currentOffsetY = { if (isSettling) animOffsetY.value else dragOffsetY }
    val currentOffsetX = { if (isSettling) animOffsetX.value else dragOffsetX }
    val offsetProvider = {
        IntOffset(
            kotlin.math.round(currentOffsetX()).toInt(),
            kotlin.math.round(currentOffsetY()).toInt()
        )
    }

    val bgAlphaProvider = {
        val progress = (kotlin.math.abs(currentOffsetY()) / maxDrag).coerceIn(0f, 1f)
        (1f - progress).coerceIn(0f, 1f)
    }
    
    val uiAlphaProvider = {
        val progress = (kotlin.math.abs(currentOffsetY()) / maxDrag).coerceIn(0f, 1f)
        (1f - (progress * 4f)).coerceIn(0f, 1f)
    }

    val resizeProgressProvider = {
        val raw = (kotlin.math.abs(currentOffsetY()) / quickResizeDistance).coerceIn(0f, 1f)
        androidx.compose.animation.core.FastOutSlowInEasing.transform(raw)
    }

    val currentShapeProvider = {
        shapeProvider(resizeProgressProvider())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var previousPosition = androidx.compose.ui.geometry.Offset.Unspecified
                
                // Use detectVerticalDragGestures so HorizontalPager can still intercept left/right swipes!
                detectVerticalDragGestures(
                    onDragStart = { position ->
                        previousPosition = position
                        velocityTracker.resetTracking()
                    },
                    onDragEnd = {
                        val velocityY = velocityTracker.calculateVelocity().y
                        val speedY = abs(velocityY)
                        // If the user dragged past the threshold or flicked quickly, trigger back navigation immediately!
                        if (abs(dragOffsetY) > dismissThreshold || speedY > flingVelocityThreshold) {
                            onDismiss()
                        } else {
                            coroutineScope.launch {
                                isSettling = true
                                animOffsetX.snapTo(dragOffsetX)
                                animOffsetY.snapTo(dragOffsetY)
                                val springSpec = androidx.compose.animation.core.spring<Float>(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                )
                                launch { animOffsetX.animateTo(0f, springSpec) }
                                launch {
                                    animOffsetY.animateTo(0f, springSpec) {
                                        val p = (kotlin.math.abs(value) / maxDrag).coerceIn(0f, 1f)
                                        onUiAlphaChange((1f - (p * 4f)).coerceIn(0f, 1f))
                                        onBgAlphaChange((1f - p).coerceIn(0f, 1f))
                                    }
                                    dragOffsetX = 0f
                                    dragOffsetY = 0f
                                    isSettling = false
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            isSettling = true
                            animOffsetX.snapTo(dragOffsetX)
                            animOffsetY.snapTo(dragOffsetY)
                            val springSpec = androidx.compose.animation.core.spring<Float>(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                            )
                            launch { animOffsetX.animateTo(0f, springSpec) }
                            launch {
                                animOffsetY.animateTo(0f, springSpec) {
                                    val p = (kotlin.math.abs(value) / maxDrag).coerceIn(0f, 1f)
                                    onUiAlphaChange((1f - (p * 4f)).coerceIn(0f, 1f))
                                    onBgAlphaChange((1f - p).coerceIn(0f, 1f))
                                }
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                                isSettling = false
                            }
                        }
                    },
                    onVerticalDrag = { change, dragAmountY ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        
                        // We intercept vertically, but we still track exact X delta for true 2D motion!
                        val dragAmountX = if (previousPosition != androidx.compose.ui.geometry.Offset.Unspecified) {
                            change.position.x - previousPosition.x
                        } else 0f
                        previousPosition = change.position
                        
                        dragOffsetX += dragAmountX
                        dragOffsetY += dragAmountY
                        
                        val progress = (kotlin.math.abs(dragOffsetY) / maxDrag).coerceIn(0f, 1f)
                        onUiAlphaChange((1f - (progress * 4f)).coerceIn(0f, 1f))
                        onBgAlphaChange((1f - progress).coerceIn(0f, 1f))
                    }
                )
            }
    ) {
        // Apply the background and other overlay elements underneath the moving photo
        overlayContent(bgAlphaProvider, uiAlphaProvider)
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            content(currentShapeProvider, resizeProgressProvider, offsetProvider)
        }
    }
}
