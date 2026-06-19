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
import androidx.activity.compose.BackHandler
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
    
    val photos = remember(viewModel.libraryCurrentTab, viewModel.filteredPhotos, viewModel.filteredFavoritesPhotos) {
        when (viewModel.libraryCurrentTab) {
            "Library" -> viewModel.filteredPhotos
            "Favorites" -> viewModel.filteredFavoritesPhotos.values.flatten()
            else -> viewModel.filteredPhotos
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
    
    BackHandler {
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
    val context = LocalContext.current
    val isAnyPopupActive = showCurrentSnippetsModal || showAddModal || showDeleteModal
    
    val scrollStates = remember { mutableStateMapOf<Int, Boolean>() }
    val isScrolled = scrollStates[pagerState.currentPage] ?: false
    val scrollToTopActions = remember { mutableStateMapOf<Int, () -> Unit>() }


    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                val hasSnippetsCurrent = photo.snippets.isNotEmpty()
                DetailTopBar(
                    photo = photo,
                    viewModel = viewModel,
                    onBack = { viewModel.closeDetail() },
                    isSpinning = !isAnyPopupActive,
                    isScrolled = isScrolled,
                    onPhotoThumbnailClick = { scrollToTopActions[pagerState.currentPage]?.invoke() },
                    hasSnippets = hasSnippetsCurrent,
                    onAdd = { showAddModal = true },
                    onDownload = { 
                        viewModel.downloadPhotoCard(context, photo, true, android.graphics.Color.BLACK) 
                    },
                    onEdit = { showCurrentSnippetsModal = true },
                    onShare = { viewModel.sharePhotoCard(context, photo, true, android.graphics.Color.BLACK) },
                    onDelete = { showDeleteModal = true },
                    isFavorite = photo.isFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite(photo.id) },
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .nestedScroll(nestedScrollConnection)
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
                            val sharedKey = if (viewModel.detailReturnScreen == Screen.Memory) "memory_${pagePhoto.id}" else "photo_${pagePhoto.id}"
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
                                onScrollChanged = { scrollStates[page] = it },
                                onRegisterScrollToTop = { scrollToTopActions[page] = it },
                                pageOffset = pageOffset
                            )
                        } else {
                            val sharedKey = if (viewModel.detailReturnScreen == Screen.Memory) "memory_${pagePhoto.id}" else "photo_${pagePhoto.id}"
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
                                pageOffset = pageOffset
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
    val motionScheme = MaterialTheme.motionScheme

    Card(
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier,
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
                    .placeholderMemoryCacheKey(photo.uriString)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
                    .then(
                        // Disabled shared element transition as per user request to slide from left instead
                        Modifier
                    )
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
    pageOffset: Float = 0f
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
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
    pageOffset: Float = 0f
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

        // Snippets on the bottom of the screen
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
                .padding(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            val pureSnippets = photo.snippets
            val total = pureSnippets.size

            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                pureSnippets.forEachIndexed { index, snippet ->
                    CloudSnippetItem(
                        text = snippet,
                        index = index,
                        totalCount = total,
                        photoColors = emptyList(),
                        forcedColor = viewModel.getSnippetColor(snippet),
                        forcedStyle = viewModel.getSnippetStyle(snippet)
                    )
                }
            }
        }
    }
}
