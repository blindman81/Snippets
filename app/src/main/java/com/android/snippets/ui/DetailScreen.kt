package com.android.snippets.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale



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
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
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
                    val filtered = viewModel.filteredPhotos.filter { !it.collections.contains("Eatlist") }
                    viewModel.sortPhotos(filtered, viewModel.getPhotoSortTypeFor("Library"))
                }
                "Favorites" -> viewModel.filteredFavoritesPhotos.values.flatten()
                else -> {
                    val collectionName = viewModel.libraryCurrentTab
                    val filtered = viewModel.filteredPhotos.filter { it.collections.contains(collectionName) }
                    viewModel.sortPhotos(filtered, viewModel.getPhotoSortTypeFor(collectionName))
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

    var dismissOffsetY by remember { mutableStateOf(0f) }
    val dismissProgress = (dismissOffsetY / 600f).coerceIn(0f, 1f)

    // Track whether the user started dragging from scroll-top position
    var gestureStartedAtTop by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If we're in dismiss mode and user pulls back up, cancel dismiss
                if (dismissOffsetY > 0 && available.y < 0) {
                    val consumed = available.y.coerceAtLeast(-dismissOffsetY)
                    dismissOffsetY += consumed
                    if (dismissOffsetY <= 0f) {
                        dismissOffsetY = 0f
                        gestureStartedAtTop = false
                    }
                    return Offset(0f, consumed)
                }
                // Any upward scroll means we're not starting a dismiss gesture
                if (available.y < 0) gestureStartedAtTop = false
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Ignore flings hitting the top boundary
                if (source.toString() == "Fling" || source.toString() == "SideEffect") {
                    return Offset.Zero
                }

                if (available.y > 0) {
                    // Child couldn't consume downward drag — it's at the top
                    if (dismissOffsetY == 0f) gestureStartedAtTop = true
                    if (gestureStartedAtTop) {
                        dismissOffsetY += available.y
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val shouldDismiss = gestureStartedAtTop && (available.y > 2000f || dismissOffsetY > 300f)
                
                if (shouldDismiss) {
                    viewModel.closeDetail()
                }
                
                // Always reset state after a fling attempt to avoid sticky states
                dismissOffsetY = 0f
                gestureStartedAtTop = false
                
                return if (shouldDismiss) available else Velocity.Zero
            }
        }
    }

    var showCurrentSnippetsModal by remember { mutableStateOf(false) }
    var showAddModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }
    var showRateModal by remember { mutableStateOf(false) }
    var showLinkModal by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.pendingOpenAddSnippetDialog, photo.id) {
        if (viewModel.pendingOpenAddSnippetDialog && viewModel.activePhotoId == photo.id) {
            showAddModal = true
            viewModel.pendingOpenAddSnippetDialog = false
        }
    }

    val context = LocalContext.current
    val isAnyPopupActive = showCurrentSnippetsModal || showAddModal || showDeleteModal || showRateModal || showLinkModal
    
    val scrollStates = remember { mutableStateMapOf<Int, Boolean>() }
    val isScrolled = scrollStates[pagerState.currentPage] ?: false
    val scrollToTopActions = remember { mutableStateMapOf<Int, () -> Unit>() }


    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = sharedBgAlpha.floatValue }
                .background(MaterialTheme.colorScheme.surface)
        )
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Box(modifier = Modifier.graphicsLayer { alpha = sharedUiAlpha.floatValue }) {
                    val hasSnippetsCurrent = photo.snippets.isNotEmpty()
                    DetailTopBar(
                        photo = photo,
                        viewModel = viewModel,
                        onBack = { viewModel.closeDetail() },
                        isSpinning = !isAnyPopupActive,
                        isScrolled = isScrolled,
                        onPhotoThumbnailClick = { scrollToTopActions[pagerState.currentPage]?.invoke() },
                        hasSnippets = hasSnippetsCurrent,
                        showAddButton = viewModel.libraryCurrentTab != "Eatlist",
                        onAdd = { showAddModal = true },
                        onDownload = { 
                            viewModel.downloadPhotoCard(context, photo, true, android.graphics.Color.BLACK) 
                        },
                        onEdit = { showCurrentSnippetsModal = true },
                        onShare = { viewModel.sharePhotoCard(context, photo, true, android.graphics.Color.BLACK) },
                        onDelete = { showDeleteModal = true },
                        onRate = { showRateModal = true },
                        isFavorite = photo.isFavorite,
                        onToggleFavorite = { viewModel.toggleFavorite(photo.id) },
                        hasLocationLink = !photo.locationLink.isNullOrBlank(),
                        onAddLinkClick = { showLinkModal = true },
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }
        ) { paddingValues ->
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
                                dismissProgress = dismissProgress,
                                dismissOffsetY = dismissOffsetY,
                                onDismissOffsetYChange = { dismissOffsetY = it },
                                onDismissRequest = { viewModel.closeDetail() },
                                onScrollChanged = { scrolled -> scrollStates[page] = scrolled },
                                onRegisterScrollToTop = { action -> scrollToTopActions[page] = action },
                                viewModel = viewModel,
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
                                onRemoveEatlist = { showDeleteModal = true },
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
                                dismissProgress = dismissProgress,
                                dismissOffsetY = dismissOffsetY,
                                onDismissOffsetYChange = { dismissOffsetY = it },
                                onDismissRequest = { viewModel.closeDetail() },
                                onScrollChanged = { scrollStates[page] = it },
                                onRegisterScrollToTop = { scrollToTopActions[page] = it },
                                viewModel = viewModel,
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
                                onRemoveEatlist = { showDeleteModal = true },
                                onEditLink = { showLinkModal = true },
                                onEditSnippets = { showCurrentSnippetsModal = true }
                            )
                        }
                    }
                    }
                }


            }
        }

        // Smoothly animated glassmorphic modal overlays outside the Scaffold, inside the root Box!
        AnimatedVisibility(
            visible = showCurrentSnippetsModal,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec())
        ) {
            CurrentSnippetsModal(
                photo = photo,
                onRemove = { viewModel.removeSnippet(photo.id, it) },
                onClose = { showCurrentSnippetsModal = false },
                viewModel = viewModel,

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
                onAdd = { name, color, style -> viewModel.updateSnippets(photo.id, name, color, style) },
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
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).widthIn(max = 400.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (count > 1) "Delete $count photos?" else "Delete photo?",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (count > 1) "Deleting these photos also removes the snippets you created for them." else "Deleting this photo also removes the snippets you created for this photo.",
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
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_END)
                                onDismiss()
                            },
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                                onConfirm(unpublish)
                            },
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Text("Delete", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
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
    showLoading: Boolean = false
) {
    Card(
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.then(
            if (isTransitionTarget && sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier.sharedBounds(
                        rememberSharedContentState(key = sharedKey),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(durationMillis = 380, easing = FastOutSlowInEasing)
                        },
                        resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.Fit)
                    )
                }
            } else {
                Modifier
            }
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = coil.request.ImageRequest.Builder(LocalContext.current)
                    .data(photo.uriString)
                    .crossfade(false)
                    .memoryCacheKey(photo.uriString)
                    .placeholderMemoryCacheKey(photo.uriString)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
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
    dismissProgress: Float = 0f,
    dismissOffsetY: Float = 0f,
    onDismissOffsetYChange: (Float) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onScrollChanged: (Boolean) -> Unit = {},
    onRegisterScrollToTop: (() -> Unit) -> Unit = {},
    viewModel: SnippetsViewModel? = null,
    onUiAlphaChange: (Float) -> Unit = {},
    onBgAlphaChange: (Float) -> Unit = {},
    pageOffset: Float = 0f,
    onOpenLink: (String) -> Unit = {},
    onEditLink: () -> Unit = {},
    onRemoveEatlist: () -> Unit = {}
) {
    val isEatlist = photo.collections.contains("Eatlist") || (viewModel != null && viewModel.libraryCurrentTab == "Eatlist")
    SwipeToDismissContainer(
        onDismiss = onDismissRequest,
        onUiAlphaChange = onUiAlphaChange,
        onBgAlphaChange = onBgAlphaChange,
        overlayContent = { _, uiAlphaProvider ->
            if (isEatlist) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
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
                            .padding(bottom = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EatlistSingleListCardContainer(
                            photo = photo,
                            onOpenLink = onOpenLink,
                            onEditLink = onEditLink,
                            onRemoveEatlist = onRemoveEatlist,
                            onAddEatlistToLibrary = { viewModel?.addEatlistPhotoToLibrary(photo.id) }
                        )
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DetailPhotoFrame(
                photo = photo,
                sharedKey = sharedKey,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                isTransitionTarget = isTransitionTarget,
                modifier = photo.aspectRatio?.let { Modifier.aspectRatio(it) } ?: Modifier.fillMaxSize()
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
    dismissProgress: Float = 0f,
    dismissOffsetY: Float = 0f,
    onDismissOffsetYChange: (Float) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onScrollChanged: (Boolean) -> Unit = {},
    onRegisterScrollToTop: (() -> Unit) -> Unit = {},
    viewModel: SnippetsViewModel,
    onUiAlphaChange: (Float) -> Unit = {},
    onBgAlphaChange: (Float) -> Unit = {},
    pageOffset: Float = 0f,
    onOpenLink: (String) -> Unit = {},
    onEditLink: () -> Unit = {},
    onRemoveEatlist: () -> Unit = {},
    onEditSnippets: () -> Unit = {}
) {
    val isEatlist = photo.collections.contains("Eatlist") || viewModel.libraryCurrentTab == "Eatlist"
    SwipeToDismissContainer(
        onDismiss = onDismissRequest,
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
                        .padding(bottom = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val pureSnippets = photo.snippets
                    val total = pureSnippets.size

                    val containerInteractionSource = remember { MutableInteractionSource() }
                    val isContainerPressed by containerInteractionSource.collectIsPressedAsState()
                    val containerCornerRadius by animateDpAsState(
                        targetValue = if (isContainerPressed) 100.dp else 24.dp,
                        label = "snippetContainerRadius"
                    )

                    val snippetRenderer: @Composable (String) -> Unit = { snippet ->
                        val forcedColor = viewModel.getSnippetColor(snippet)
                        val forcedStyle = viewModel.getSnippetStyle(snippet)

                        val bg = MaterialTheme.colorScheme.surfaceContainerHighest
                        val isBgDark = (0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue) < 0.5f
                        val baseSnippetColor = if (forcedColor != null) Color(forcedColor) else MaterialTheme.colorScheme.primary
                        val snippetColor = remember(baseSnippetColor, isBgDark) {
                            val lum = 0.299f * baseSnippetColor.red + 0.587f * baseSnippetColor.green + 0.114f * baseSnippetColor.blue
                            if (isBgDark && lum < 0.3f) baseSnippetColor.copy(red = (baseSnippetColor.red + 0.4f).coerceAtMost(1f), green = (baseSnippetColor.green + 0.4f).coerceAtMost(1f), blue = (baseSnippetColor.blue + 0.4f).coerceAtMost(1f))
                            else if (!isBgDark && lum > 0.7f) baseSnippetColor.copy(red = (baseSnippetColor.red - 0.4f).coerceAtLeast(0f), green = (baseSnippetColor.green - 0.4f).coerceAtLeast(0f), blue = (baseSnippetColor.blue - 0.4f).coerceAtLeast(0f))
                            else baseSnippetColor
                        }

                        val snippetGradient = remember(snippetColor) {
                            Brush.linearGradient(colors = listOf(snippetColor, snippetColor.copy(alpha = 0.55f)))
                        }

                        val view = LocalView.current

                        Surface(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onEditSnippets()
                            },
                            shape = CircleShape,
                            color = snippetColor.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, snippetColor.copy(alpha = 0.30f)),
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
                                    ).copy(brush = snippetGradient),
                                    color = Color.Unspecified,
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

                    if (isEatlist) {
                        EatlistSingleListCardContainer(
                            photo = photo,
                            onOpenLink = onOpenLink,
                            onEditLink = onEditLink,
                            onRemoveEatlist = onRemoveEatlist,
                            onAddEatlistToLibrary = { viewModel.addEatlistPhotoToLibrary(photo.id) }
                        )
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DetailPhotoFrame(
                photo = photo,
                sharedKey = sharedKey,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                isTransitionTarget = isTransitionTarget,
                modifier = photo.aspectRatio?.let { Modifier.aspectRatio(it) } ?: Modifier.fillMaxSize()
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

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).widthIn(max = 360.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rate your experience",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )

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

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(96.dp)
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
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_END)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                            onConfirm(rating)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Text("Save", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LocationLinkModal(
    initialLink: String?,
    suggestions: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    val view = LocalView.current
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

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).widthIn(max = 400.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Link to a place",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            val name = resolvedNames[linkValue] ?: LocationUtils.extractPlaceNameFromLink(linkValue) ?: LocationUtils.cleanLocationName(linkValue)
                            val finalName = if (name.startsWith("http")) null else LocationUtils.cleanLocationName(name)
                            onSave(linkValue, finalName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Text("Save", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeToDismissContainer(
    onDismiss: () -> Unit,
    onUiAlphaChange: (Float) -> Unit = {},
    onBgAlphaChange: (Float) -> Unit = {},
    overlayContent: @Composable (bgAlphaProvider: () -> Float, uiAlphaProvider: () -> Float) -> Unit = { _, _ -> },
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Use Animatable so we can smoothly spring back if the swipe is canceled
    val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val offsetY = remember { androidx.compose.animation.core.Animatable(0f) }

    // Define how far the user needs to drag to trigger the dismissal
    val density = androidx.compose.ui.platform.LocalDensity.current
    val dismissThreshold = with(density) { 40.dp.toPx() }
    val maxDrag = with(density) { 100.dp.toPx() }
    val flingVelocityThreshold = with(density) { 200.dp.toPx() }

    val velocityTracker = remember { androidx.compose.ui.input.pointer.util.VelocityTracker() }

    LaunchedEffect(offsetY) {
        androidx.compose.runtime.snapshotFlow { offsetY.value }.collect { y ->
            val progress = (kotlin.math.abs(y) / maxDrag).coerceIn(0f, 1f)
            val uiAlpha = (1f - (progress * 5f)).coerceIn(0f, 1f)
            val bgAlpha = 1f - progress
            onUiAlphaChange(uiAlpha)
            onBgAlphaChange(bgAlpha)
        }
    }

    val bgAlphaProvider = {
        val progress = (kotlin.math.abs(offsetY.value) / maxDrag).coerceIn(0f, 1f)
        1f - progress
    }
    
    val uiAlphaProvider = {
        val progress = (kotlin.math.abs(offsetY.value) / maxDrag).coerceIn(0f, 1f)
        (1f - (progress * 5f)).coerceIn(0f, 1f)
    }

    val scaleProvider = {
        val progress = (kotlin.math.abs(offsetY.value) / maxDrag).coerceIn(0f, 1f)
        1f - (0.15f * progress)
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
                        coroutineScope.launch {
                            // If the user dragged past the threshold or flicked quickly, trigger the back navigation!
                            if (abs(offsetY.value) > dismissThreshold || speedY > flingVelocityThreshold) {
                                onDismiss()
                            } else {
                                // Otherwise, smoothly spring the image back to the center
                                launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = androidx.compose.animation.core.spring(
                                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                                launch {
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = androidx.compose.animation.core.spring(
                                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            launch { offsetX.animateTo(0f) }
                            launch { offsetY.animateTo(0f) }
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
                        
                        coroutineScope.launch {
                            // Instantly snap the animatable to follow the finger in 2D space
                            offsetX.snapTo(offsetX.value + dragAmountX)
                            offsetY.snapTo(offsetY.value + dragAmountY)
                        }
                    }
                )
            }
    ) {
        // Apply the background and other overlay elements underneath the moving photo
        overlayContent(bgAlphaProvider, uiAlphaProvider)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { androidx.compose.ui.unit.IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .graphicsLayer {
                    val currentScale = scaleProvider()
                    scaleX = currentScale
                    scaleY = currentScale
                }
        ) {
            content()
        }
    }
}
