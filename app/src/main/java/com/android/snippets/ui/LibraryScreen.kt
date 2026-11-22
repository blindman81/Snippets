package com.android.snippets.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.abs
import kotlin.coroutines.cancellation.CancellationException
import com.android.snippets.model.Photo
import com.android.snippets.ui.components.*
import com.android.snippets.ui.util.Motion
import com.android.snippets.ui.util.smoothScrollToItemSlow
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.MemoryViewModel
import com.android.snippets.viewmodel.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun LibraryScreen(
    viewModel: SnippetsViewModel,
    memoryViewModel: MemoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass?,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onAddPhotos: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val emojiFocusRequester = remember { FocusRequester() }
    val emojiTextFieldState = rememberTextFieldState()

    var isSearchOpen by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    var modalPhoto by remember { mutableStateOf<Photo?>(null) }
    var showAddModal by remember { mutableStateOf(false) }
    var showSnippetsModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }
    var showRateModal by remember { mutableStateOf(false) }
    var showLinkModal by remember { mutableStateOf(false) }
    var showNameModal by remember { mutableStateOf(false) }

    var deletingBox by remember { mutableStateOf<String?>(null) }

    val manageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val action = result.data?.getStringExtra(EditBoxActivity.EXTRA_ACTION)
            val box = result.data?.getStringExtra(EditBoxActivity.EXTRA_BOX)
                ?: result.data?.getStringExtra(EditBoxActivity.EXTRA_COLLECTION)
                ?: viewModel.libraryCurrentTab
            when (action) {
                EditBoxActivity.ACTION_ADD_PHOTOS -> onAddPhotos(box)
                EditBoxActivity.ACTION_REMOVE_PHOTOS -> viewModel.startBoxRemoval(box)
                EditBoxActivity.ACTION_EDIT_EMOJI -> viewModel.startEditingBoxEmoji(box)
                EditBoxActivity.ACTION_DELETE_BOX, EditBoxActivity.ACTION_DELETE_COLLECTION -> {
                    deletingBox = box
                }
            }
        }
    }

    val filterLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val filters = result.data?.getStringArrayListExtra(FilterActivity.EXTRA_SELECTED_FILTERS)
            if (filters != null) {
                viewModel.selectedFilterSnippets = filters
            }
        }
    }

    val moreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val action = result.data?.getStringExtra(MoreActivity.EXTRA_ACTION)
            if (action == MoreActivity.ACTION_CREATE_BOX || action == MoreActivity.ACTION_CREATE_COLLECTION) {
                viewModel.createNewBox()
            }
        }
    }

    val isAnyPopupActive = showAddModal || showSnippetsModal || showDeleteModal ||
            showRateModal || showLinkModal || showNameModal ||
            uiState.showBulkAddToBoxDialog || uiState.showBulkDeleteModal ||
            uiState.showCreateDialog || (deletingBox != null)

    PredictiveBackHandler(enabled = uiState.isSelectionMode) { progress ->
        try {
            progress.collect { }
            if (uiState.searchQuery.isNotEmpty()) {
                viewModel.searchQuery = ""
            } else {
                viewModel.clearSelection()
            }
        } catch (_: CancellationException) {}
    }

    PredictiveBackHandler(enabled = (isSearchOpen || uiState.searchQuery.isNotEmpty()) && !uiState.isSelectionMode) { progress ->
        try {
            progress.collect { }
            isSearchOpen = false
            viewModel.searchQuery = ""
            focusManager.clearFocus()
        } catch (_: CancellationException) {}
    }

    PredictiveBackHandler(enabled = uiState.activePhotoId != null) { progress ->
        try {
            progress.collect { }
            viewModel.closeDetail()
        } catch (_: CancellationException) {}
    }

    PredictiveBackHandler(enabled = deletingBox != null) { progress ->
        try {
            progress.collect { }
            deletingBox = null
        } catch (_: CancellationException) {}
    }

    PredictiveBackHandler(enabled = uiState.editingBoxEmoji != null) { progress ->
        try {
            progress.collect { }
            keyboardController?.hide()
            viewModel.cancelEditingBoxEmoji()
        } catch (_: CancellationException) {}
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP && viewModel.editingBoxEmoji != null) {
                keyboardController?.hide()
                focusManager.clearFocus(force = true)
                viewModel.cancelEditingBoxEmoji()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val pageTabs = remember(uiState.userBoxes) {
        val filteredUserBoxes = uiState.userBoxes
            .filter { it != "Box" && it != "Library" && it != "Box 1" && it != "Favorites" }
            .distinct()
        listOf("Box", "Box 1") + filteredUserBoxes
    }

    val initialPage = remember(pageTabs) { pageTabs.indexOf(uiState.libraryCurrentTab).coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageTabs.size })
    val listStates = remember { mutableStateMapOf<String, androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState>() }
    val carouselLazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val tabRowScrollState = rememberScrollState()
    val tabPositions = remember { mutableStateMapOf<String, Float>() }
    val tabWidths = remember { mutableStateMapOf<String, Float>() }

    var savedCollectionsScrollPosition by rememberSaveable { mutableIntStateOf(0) }
    var previousPendingAssignment by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.pendingBoxAssignment) {
        val currentAssignment = uiState.pendingBoxAssignment
        if (currentAssignment != null && previousPendingAssignment == null) {
            savedCollectionsScrollPosition = tabRowScrollState.value
        } else if (currentAssignment == null && previousPendingAssignment != null) {
            if (uiState.libraryCurrentTab == previousPendingAssignment) {
                tabRowScrollState.scrollTo(savedCollectionsScrollPosition)
            } else {
                val currentTabName = uiState.libraryCurrentTab
                val tabX = tabPositions[currentTabName]
                val tabW = tabWidths[currentTabName]
                if (tabX != null && tabW != null) {
                    val rowW = tabRowScrollState.maxValue + 0f
                    val target = (tabX - (tabRowScrollState.viewportSize / 2f) + (tabW / 2f))
                        .coerceIn(0f, rowW)
                    tabRowScrollState.animateScrollTo(target.toInt())
                } else {
                    tabRowScrollState.animateScrollTo(0)
                }
            }
        }
        previousPendingAssignment = currentAssignment
    }

    LaunchedEffect(viewModel.scrollToTopTrigger) {
        if (viewModel.scrollToTopTrigger != 0L) {
            listStates.values.forEach { state ->
                try {
                    state.scrollToItem(0)
                } catch (_: Exception) {}
            }
            try {
                carouselLazyListState.scrollToItem(0)
            } catch (_: Exception) {}
        }
    }

    val currentTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Box"

    var isFirstTabLoad by remember { mutableStateOf(true) }

    // Instant tactile haptic feedback on page boundary switch (without mutating ViewModel state)
    LaunchedEffect(currentTab) {
        if (viewModel.editingBoxEmoji != null && viewModel.editingBoxEmoji != currentTab) {
            viewModel.cancelEditingBoxEmoji()
        }
        if (isFirstTabLoad) {
            isFirstTabLoad = false
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    // Only commit active tab to ViewModel & reset carousel once the pager settles
    val currentTabs by rememberUpdatedState(pageTabs)
    LaunchedEffect(pagerState) {
        snapshotFlow { Pair(pagerState.isScrollInProgress, pagerState.settledPage) }
            .collect { (isScrolling, settled) ->
                if (!isScrolling) {
                    val settledTab = currentTabs.getOrNull(settled) ?: "Box"
                    if (viewModel.libraryCurrentTab != settledTab) {
                        viewModel.libraryCurrentTab = settledTab
                        try {
                            carouselLazyListState.scrollToItem(0)
                        } catch (_: Exception) {}
                    }
                }
            }
    }

    val displayedCarouselTab by remember(pageTabs, pagerState.settledPage, currentTab, pagerState.isScrollInProgress) {
        derivedStateOf {
            if (pagerState.isScrollInProgress) {
                pageTabs.getOrNull(pagerState.settledPage) ?: currentTab
            } else {
                currentTab
            }
        }
    }

    LaunchedEffect(viewModel.libraryCurrentTab, pageTabs) {
        val targetIndex = pageTabs.indexOf(viewModel.libraryCurrentTab)
        if (targetIndex != -1 && targetIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    LaunchedEffect(viewModel.editingBoxEmoji, uiState.emojiKeyboardRequestCount) {
        val editingCol = viewModel.editingBoxEmoji
        if (editingCol != null && uiState.emojiKeyboardRequestCount > 0L) {
            emojiTextFieldState.clearText()
            focusManager.clearFocus(force = true)
            delay(50)
            emojiFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(emojiTextFieldState) {
        snapshotFlow { emojiTextFieldState.text.toString() }
            .collect { text ->
                val emoji = text.trim()
                val editingCol = viewModel.editingBoxEmoji
                if (editingCol != null && emoji.isNotEmpty()) {
                    viewModel.updateEditingEmojiPreview(editingCol, emoji)
                    delay(150)
                    keyboardController?.hide()
                    viewModel.finalizeBoxEmoji(editingCol, emoji)
                }
            }
    }

    val flatPhotosRaw = remember(
        uiState.photos,
        uiState.searchQuery,
        uiState.selectedFilterSnippets,
        uiState.photoSortType
    ) {
        uiState.filteredPhotos
    }

    LaunchedEffect(uiState.activePhotoId, currentTab) {
        val activeId = uiState.activePhotoId
        if (activeId != null) {
            val tabPhotos = uiState.getFilteredPhotosForTab(currentTab)
            val targetIndex = tabPhotos.indexOfFirst { it.id == activeId }
            if (targetIndex != -1) {
                val gridState = listStates.getOrPut(currentTab) { LazyStaggeredGridState() }
                delay(180)
                gridState.smoothScrollToItemSlow(targetIndex, targetOffsetPx = 24)
            }
        }
    }

    val density = LocalDensity.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val currentGridState = listStates[currentTab] ?: listStates.getOrPut(currentTab) { LazyStaggeredGridState() }

    val hasLibraryMemories = remember(memoryViewModel.historyMemories, uiState.searchQuery) {
        memoryViewModel.historyMemories.isNotEmpty() && uiState.searchQuery.isEmpty()
    }

    val currentTabMemories = remember(memoryViewModel.historyMemories, currentTab, uiState.searchQuery) {
        if ((currentTab == "Box" || currentTab == "Library") && uiState.searchQuery.isEmpty()) memoryViewModel.historyMemories else emptyList()
    }
    val hasTabMemories = currentTabMemories.isNotEmpty()

    val isCurrentTabScrolled by remember(currentTab, currentGridState) {
        derivedStateOf {
            currentGridState.firstVisibleItemIndex > 0 || currentGridState.firstVisibleItemScrollOffset > 4
        }
    }

    val libraryGridState = listStates.getOrPut("Box") { listStates.getOrPut("Library") { LazyStaggeredGridState() } }
    val maxCarouselCollapsePx = with(density) { 104.dp.toPx() }

    val carouselCollapseProgress by remember(
        hasLibraryMemories,
        libraryGridState,
        maxCarouselCollapsePx,
        pageTabs,
        pagerState
    ) {
        derivedStateOf {
            if (!hasLibraryMemories) {
                1f
            } else {
                val libraryScrollProgress = if (libraryGridState.firstVisibleItemIndex > 0) {
                    1f
                } else {
                    (libraryGridState.firstVisibleItemScrollOffset.toFloat() / maxCarouselCollapsePx).coerceIn(0f, 1f)
                }

                val libraryIndex = pageTabs.indexOf("Box").let { if (it >= 0) it.toFloat() else pageTabs.indexOf("Library").let { l -> if (l >= 0) l.toFloat() else 0f } }
                val continuousPage = (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceAtLeast(0f)
                val distanceFromLibrary = kotlin.math.abs(continuousPage - libraryIndex).coerceIn(0f, 1f)

                // Smoothly interpolate between Library's scroll-based collapse progress and fully collapsed (1f)
                libraryScrollProgress + (1f - libraryScrollProgress) * distanceFromLibrary
            }
        }
    }

    val carouselHeightDp = 104.dp * (1f - carouselCollapseProgress)
    val carouselAlpha = (1f - carouselCollapseProgress * 1.3f).coerceIn(0f, 1f)

    val appBarBlurRadius by animateDpAsState(
        targetValue = if (isCurrentTabScrolled) 20.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "app_bar_blur_radius"
    )

    val appBarContainerColor by animateColorAsState(
        targetValue = if (isCurrentTabScrolled) {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "app_bar_container_color"
    )

    val appBarHazeStyle = remember(appBarContainerColor, appBarBlurRadius) {
        HazeStyle(
            tint = HazeTint(appBarContainerColor),
            blurRadius = appBarBlurRadius,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }

    val hazeState = LocalHazeState.current ?: dev.chrisbanes.haze.rememberHazeState()

    CompositionLocalProvider(
        LocalHazeState provides hazeState
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
            ) {
                CompositionLocalProvider(
                    com.android.snippets.ui.util.LocalTiltActive provides
                            (com.android.snippets.ui.util.LocalTiltActive.current && !isAnyPopupActive)
                ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val topGridPadding = if (hasTabMemories) {
                                statusBarTop + 176.dp + 8.dp
                            } else {
                                statusBarTop + 72.dp + 8.dp
                            }

                            val targetPage = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
                            val settledPage = pagerState.settledPage
                            val scrollProgress = if (pagerState.isScrollInProgress) {
                                val pageDistance = abs(targetPage - settledPage)
                                if (pageDistance == 0) {
                                    0f
                                } else {
                                    abs((pagerState.currentPage + pagerState.currentPageOffsetFraction) - settledPage) /
                                        pageDistance.toFloat()
                                }
                            } else {
                                1f
                            }
                            val selectionPage = if (
                                pagerState.isScrollInProgress && scrollProgress < 0.7f
                            ) settledPage else targetPage
                            val activeTabTarget = pageTabs.getOrNull(selectionPage) ?: "Box"
                            val targetTab = pageTabs.getOrNull(targetPage) ?: "Box"

                            LaunchedEffect(targetTab) {
                                if (uiState.pendingBoxAssignment != null) return@LaunchedEffect
                                val tabX = tabPositions[targetTab] ?: return@LaunchedEffect
                                val tabW = tabWidths[targetTab] ?: return@LaunchedEffect
                                val rowW = tabRowScrollState.maxValue + 0f
                                val target = (tabX - (tabRowScrollState.viewportSize / 2f) + (tabW / 2f))
                                    .coerceIn(0f, rowW)
                                tabRowScrollState.animateScrollTo(target.toInt())
                            }

                            // 1. Photo Feed Pager
                            LibraryPhotoPager(
                                pagerState = pagerState,
                                pageTabs = pageTabs,
                                flatPhotosRaw = flatPhotosRaw,
                                uiState = uiState,
                                viewModel = viewModel,
                                settingsViewModel = settingsViewModel,
                                memoryViewModel = memoryViewModel,
                                listStates = listStates,
                                topGridPadding = topGridPadding,
                                statusBarTop = statusBarTop,
                                hazeState = hazeState,
                                showAddModal = showAddModal,
                                modalPhotoId = modalPhoto?.id,
                                onPhotoClick = { photo ->
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleSelection(photo.id)
                                    } else {
                                        val isExpanding = uiState.activePhotoId != photo.id
                                        viewModel.activePhotoId = if (isExpanding) photo.id else null
                                    }
                                },
                                onPhotoLongClick = { photo ->
                                    if (!uiState.isSelectionMode) viewModel.toggleSelection(photo.id)
                                },
                                onAddSnippet = { photo ->
                                    viewModel.requestAddSnippet(
                                        photo = photo,
                                        onShowNameModal = {
                                            modalPhoto = it
                                            showNameModal = true
                                        },
                                        onShowAddModal = {
                                            modalPhoto = it
                                            showAddModal = true
                                        }
                                    )
                                },
                                onNamePhoto = { photo ->
                                    modalPhoto = photo
                                    showNameModal = true
                                },
                                onEditLink = { photo ->
                                    modalPhoto = photo
                                    showLinkModal = true
                                },
                                onRatePhoto = { photo ->
                                    modalPhoto = photo
                                    showRateModal = true
                                },
                                onViewSnippets = { photo ->
                                    modalPhoto = photo
                                    showSnippetsModal = true
                                },
                                onDeletePhoto = { photo ->
                                    modalPhoto = photo
                                    showDeleteModal = true
                                }
                            )

                            // 2. Full-width App Bar Box (takes full space behind memories carousel and cookienavbar)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .zIndex(10f)
                                    .then(
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            Modifier.hazeSource(state = hazeState, zIndex = 10f)
                                        } else Modifier
                                    )
                                    .then(
                                        if (appBarBlurRadius > 0.dp && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            Modifier.hazeEffect(state = hazeState, style = appBarHazeStyle)
                                        } else {
                                            Modifier.background(appBarContainerColor)
                                        }
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = statusBarTop)
                                ) {
                                    // Memories Carousel inside the App Bar (shrinks 1:1 with scroll distance)
                                    if (hasLibraryMemories && carouselHeightDp > 0.dp) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(carouselHeightDp)
                                                .graphicsLayer {
                                                    alpha = carouselAlpha
                                                    clip = true
                                                }
                                        ) {
                                            MemoriesCarousel(
                                                currentTab = currentTab,
                                                viewModel = memoryViewModel,
                                                lazyListState = carouselLazyListState,
                                                sharedTransitionScope = sharedTransitionScope,
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                isCollapsed = carouselCollapseProgress >= 1f,
                                                pageTabs = pageTabs,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    // Cookie Nav Bar (readjusts to the top as carousel minimizes)
                                    LibraryNavBar(
                                        allTabs = pageTabs,
                                        activeTabTarget = activeTabTarget,
                                        currentTab = currentTab,
                                        pageTabs = pageTabs,
                                        pagerState = pagerState,
                                        viewModel = viewModel,
                                        tabRowScrollState = tabRowScrollState,
                                        tabPositions = tabPositions,
                                        tabWidths = tabWidths,
                                        statusBarTop = 0.dp,
                                        cookieNavBarOffset = 0.dp,
                                        cookieNavBarAlpha = 1f,
                                        onScrollCarouselToTop = {
                                            scope.launch {
                                                try {
                                                    currentGridState.animateScrollToItem(0)
                                                } catch (_: Exception) {}
                                                try {
                                                    carouselLazyListState.animateScrollToItem(0)
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                }
                            }

                            // 4. Floating Cookie Toolbar
                            CookieToolbar(
                                viewModel = viewModel,
                                currentTab = currentTab,
                                flatPhotosRaw = flatPhotosRaw,
                                isSearchOpen = isSearchOpen,
                                onSearchOpenChange = { isOpen -> isSearchOpen = isOpen },
                                searchFocusRequester = searchFocusRequester,
                                isScrolled = isCurrentTabScrolled,
                                isAnyPopupActive = isAnyPopupActive,
                                onManageClick = {
                                    val activeTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Box"
                                    val intent = Intent(context, EditBoxActivity::class.java).apply {
                                        putExtra(EditBoxActivity.EXTRA_BOX, activeTab)
                                        putStringArrayListExtra(
                                            EditBoxActivity.EXTRA_USER_BOXES,
                                            ArrayList(viewModel.userBoxes)
                                        )
                                    }
                                    manageLauncher.launch(intent)
                                },
                                onFilterClick = {
                                    val activeTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Box"
                                    val intent = Intent(context, FilterActivity::class.java).apply {
                                        putExtra(FilterActivity.EXTRA_CATEGORY, activeTab)
                                        putStringArrayListExtra(
                                            FilterActivity.EXTRA_SELECTED_FILTERS,
                                            ArrayList(uiState.selectedFilterSnippets)
                                        )
                                    }
                                    filterLauncher.launch(intent)
                                },
                                onMoreClick = {
                                    val intent = Intent(context, MoreActivity::class.java).apply {
                                        putStringArrayListExtra(
                                            MoreActivity.EXTRA_USER_BOXES,
                                            ArrayList(uiState.userBoxes)
                                        )
                                    }
                                    moreLauncher.launch(intent)
                                },
                                onBulkDeleteClick = {
                                    viewModel.showBulkDeleteModal = true
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .zIndex(25f)
                                    .then(
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                             Modifier.hazeSource(state = hazeState, zIndex = 25f)
                                        } else Modifier
                                    )
                            )

                            // 8. Photo & Box Modals
                            LibraryModalsHost(
                                viewModel = viewModel,
                                deletingBox = deletingBox,
                                onDismissDeleteBox = { deletingBox = null },
                                onConfirmDeleteBox = { box ->
                                    viewModel.deleteBox(box)
                                    if (currentTab == box) {
                                        scope.launch { pagerState.animateScrollToPage(0) }
                                    }
                                    deletingBox = null
                                },
                                modalPhoto = modalPhoto,
                                showAddModal = showAddModal,
                                onCloseAddModal = {
                                    showAddModal = false
                                    modalPhoto = null
                                },
                                showSnippetsModal = showSnippetsModal,
                                onCloseSnippetsModal = {
                                    showSnippetsModal = false
                                    modalPhoto = null
                                },
                                showDeleteModal = showDeleteModal,
                                onCloseDeleteModal = {
                                    showDeleteModal = false
                                    modalPhoto = null
                                },
                                showRateModal = showRateModal,
                                onCloseRateModal = {
                                    showRateModal = false
                                    modalPhoto = null
                                },
                                showLinkModal = showLinkModal,
                                onCloseLinkModal = {
                                    showLinkModal = false
                                    modalPhoto = null
                                },
                                showNameModal = showNameModal,
                                onCloseNameModal = {
                                    showNameModal = false
                                    modalPhoto = null
                                },
                                modifier = Modifier.zIndex(999f),
                                hazeState = hazeState
                            )

                            // 9. Hidden text field to open keyboard for emoji selection
                            BasicTextField(
                                state = emojiTextFieldState,
                                modifier = Modifier
                                    .size(1.dp)
                                    .focusRequester(emojiFocusRequester),
                                inputTransformation = EmojiInputTransformation,
                                lineLimits = TextFieldLineLimits.SingleLine,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                onKeyboardAction = {
                                    val editingCol = viewModel.editingBoxEmoji
                                    val emoji = emojiTextFieldState.text.toString().trim()
                                    if (editingCol != null && emoji.isNotEmpty()) {
                                        keyboardController?.hide()
                                        viewModel.finalizeBoxEmoji(editingCol, emoji)
                                    } else {
                                        keyboardController?.hide()
                                        viewModel.cancelEditingBoxEmoji()
                                    }
                                }
                            )
                        }
                }
            }
        }
    }
}

private object EmojiInputTransformation : InputTransformation {
    override fun TextFieldBuffer.transformInput() {
        val emojiOnly = asCharSequence().filter { char ->
            val type = Character.getType(char)
            type == Character.SURROGATE.toInt() || 
            type == Character.OTHER_SYMBOL.toInt() ||
            char.code > 0x2000 // Catch many more complex emojis
        }
        if (emojiOnly.length > 8) {
            val latest = emojiOnly.takeLast(4).toString()
            replace(0, length, latest)
        } else if (emojiOnly != asCharSequence()) {
            replace(0, length, emojiOnly)
        }
    }
}
