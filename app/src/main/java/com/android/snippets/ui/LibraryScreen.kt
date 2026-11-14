package com.android.snippets.ui

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.PredictiveBackHandler
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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showMenuPopup by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.searchQuery) {
        if (uiState.searchQuery.isNotEmpty()) {
            isSearchOpen = true
        }
    }
    val searchFocusRequester = remember { FocusRequester() }
    val emojiFocusRequester = remember { FocusRequester() }
    val emojiTextFieldState = rememberTextFieldState()

    var modalPhoto by remember { mutableStateOf<Photo?>(null) }
    var showAddModal by remember { mutableStateOf(false) }
    var showSnippetsModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }
    var showRateModal by remember { mutableStateOf(false) }
    var showLinkModal by remember { mutableStateOf(false) }
    var showNameModal by remember { mutableStateOf(false) }

    var longPressedCollection by remember { mutableStateOf<String?>(null) }
    var deletingCollection by remember { mutableStateOf<String?>(null) }


    val isAnyPopupActive = showAddModal || showSnippetsModal || showDeleteModal ||
            showRateModal || showLinkModal || showNameModal ||
            uiState.showBulkAddToCollectionDialog || uiState.showBulkDeleteModal ||
            uiState.showCreateDialog || showMenuPopup || isSearchOpen ||
            (longPressedCollection != null) || (deletingCollection != null) || uiState.showFilterSheet

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

    PredictiveBackHandler(enabled = isSearchOpen) { progress ->
        try {
            progress.collect { }
            isSearchOpen = false
            viewModel.searchQuery = ""
        } catch (_: CancellationException) {}
    }

    PredictiveBackHandler(enabled = uiState.activePhotoId != null) { progress ->
        try {
            progress.collect { }
            viewModel.closeDetail()
        } catch (_: CancellationException) {}
    }

    PredictiveBackHandler(enabled = deletingCollection != null) { progress ->
        try {
            progress.collect { }
            deletingCollection = null
        } catch (_: CancellationException) {}
    }

    PredictiveBackHandler(enabled = uiState.editingCollectionEmoji != null) { progress ->
        try {
            progress.collect { }
            keyboardController?.hide()
            viewModel.cancelEditingCollectionEmoji()
        } catch (_: CancellationException) {}
    }

    PredictiveBackHandler(enabled = showMenuPopup) { progress ->
        try {
            progress.collect { }
            showMenuPopup = false
        } catch (_: CancellationException) {}
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                showMenuPopup = false
            }
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP && viewModel.editingCollectionEmoji != null) {
                keyboardController?.hide()
                focusManager.clearFocus(force = true)
                viewModel.cancelEditingCollectionEmoji()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val pageTabs = remember(uiState.userCollections) {
        listOf("Library", "Favorites") + uiState.userCollections
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

    LaunchedEffect(uiState.pendingCollectionAssignment) {
        val currentAssignment = uiState.pendingCollectionAssignment
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

    val currentTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Library"

    var isFirstTabLoad by remember { mutableStateOf(true) }

    // Instant tactile haptic feedback on page boundary switch (without mutating ViewModel state)
    LaunchedEffect(currentTab) {
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
                    val settledTab = currentTabs.getOrNull(settled) ?: "Library"
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

    LaunchedEffect(viewModel.editingCollectionEmoji, uiState.emojiKeyboardRequestCount) {
        val editingCol = viewModel.editingCollectionEmoji
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
                val editingCol = viewModel.editingCollectionEmoji
                if (editingCol != null && emoji.isNotEmpty()) {
                    viewModel.updateEditingEmojiPreview(editingCol, emoji)
                    delay(150)
                    keyboardController?.hide()
                    viewModel.finalizeCollectionEmoji(editingCol, emoji)
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

    val hasLibraryMemories = remember(memoryViewModel.historyMemories) {
        memoryViewModel.historyMemories.isNotEmpty()
    }

    val currentTabMemories = remember(memoryViewModel.historyMemories, currentTab) {
        if (currentTab == "Library") memoryViewModel.historyMemories else emptyList()
    }
    val hasTabMemories = currentTabMemories.isNotEmpty()

    val isCurrentTabScrolled by remember(currentTab, currentGridState) {
        derivedStateOf {
            currentGridState.firstVisibleItemIndex > 0 || currentGridState.firstVisibleItemScrollOffset > 4
        }
    }

    val libraryGridState = listStates.getOrPut("Library") { LazyStaggeredGridState() }
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

                val libraryIndex = pageTabs.indexOf("Library").let { if (it >= 0) it.toFloat() else 0f }
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
                    .then(
                        if (isSearchOpen) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(onTap = { focusManager.clearFocus() })
                            }
                        } else Modifier
                    )
            ) {
                if (uiState.isInitialLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                } else {
                    CompositionLocalProvider(
                        com.android.snippets.ui.util.LocalTiltActive provides
                                (com.android.snippets.ui.util.LocalTiltActive.current && !isAnyPopupActive)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val topGridPadding = if (hasTabMemories) statusBarTop + 176.dp else statusBarTop + 72.dp

                            val targetPage = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
                            val activeTabTarget = pageTabs.getOrNull(targetPage) ?: "Library"

                            LaunchedEffect(activeTabTarget) {
                                if (uiState.pendingCollectionAssignment != null) return@LaunchedEffect
                                val tabX = tabPositions[activeTabTarget] ?: return@LaunchedEffect
                                val tabW = tabWidths[activeTabTarget] ?: return@LaunchedEffect
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
                                isMenuOpen = showMenuPopup,
                                isManageOpen = longPressedCollection != null,
                                isFilterOpen = uiState.showFilterSheet,
                                onMenuClick = {
                                    if (showMenuPopup) {
                                        showMenuPopup = false
                                    } else {
                                        longPressedCollection = null
                                        viewModel.closeFilter()
                                        showMenuPopup = true
                                    }
                                },
                                onManageClick = {
                                    if (longPressedCollection != null) {
                                        longPressedCollection = null
                                    } else {
                                        showMenuPopup = false
                                        viewModel.closeFilter()
                                        val activeTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Library"
                                        longPressedCollection = activeTab
                                    }
                                },
                                onFilterClick = {
                                    if (uiState.showFilterSheet) {
                                        viewModel.closeFilter()
                                    } else {
                                        showMenuPopup = false
                                        longPressedCollection = null
                                        val activeTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Library"
                                        viewModel.filteringCategory = activeTab
                                        viewModel.navigateFilter()
                                    }
                                },
                                onBulkDeleteClick = {
                                    viewModel.showBulkDeleteModal = true
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .zIndex(25f)
                            )

                            // 5. Menu Floating Sheet
                            MenuFloatingSheet(
                                show = showMenuPopup,
                                onDismissRequest = { showMenuPopup = false },
                                viewModel = viewModel,
                                view = view
                            )

                            // 6. Manage Collection Floating Sheet
                            ManageFloatingSheet(
                                collection = longPressedCollection,
                                currentTab = currentTab,
                                viewModel = viewModel,
                                isDeletingCollection = deletingCollection != null,
                                onDismissRequest = { longPressedCollection = null },
                                onAddPhotos = onAddPhotos,
                                onStartDeleteCollection = { col: String ->
                                    deletingCollection = col
                                    longPressedCollection = null
                                }
                            )

                            // 7. Filter Floating Sheet
                            FloatingSheet(
                                visible = uiState.showFilterSheet,
                                onDismissRequest = { viewModel.closeFilter() },
                                modifier = Modifier.padding(top = (topGridPadding - 65.dp).coerceAtLeast(0.dp)),
                                sheetModifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .widthIn(max = 428.dp)
                                    .fillMaxHeight(),
                                contentPadding = PaddingValues(top = 10.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                            ) {
                                FilterFloatingSheet(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // 8. Photo & Collection Modals
                            LibraryModalsHost(
                                viewModel = viewModel,
                                deletingCollection = deletingCollection,
                                onDismissDeleteCollection = { deletingCollection = null },
                                onConfirmDeleteCollection = { collection ->
                                    viewModel.deleteCollection(collection)
                                    if (currentTab == collection) {
                                        scope.launch { pagerState.animateScrollToPage(0) }
                                    }
                                    deletingCollection = null
                                },
                                modalPhoto = modalPhoto,
                                showAddModal = showAddModal,
                                onCloseAddModal = { showAddModal = false },
                                showSnippetsModal = showSnippetsModal,
                                onCloseSnippetsModal = { showSnippetsModal = false },
                                showDeleteModal = showDeleteModal,
                                onCloseDeleteModal = { showDeleteModal = false },
                                showRateModal = showRateModal,
                                onCloseRateModal = { showRateModal = false },
                                showLinkModal = showLinkModal,
                                onCloseLinkModal = { showLinkModal = false },
                                showNameModal = showNameModal,
                                onCloseNameModal = { showNameModal = false }
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
                                    val editingCol = viewModel.editingCollectionEmoji
                                    val emoji = emojiTextFieldState.text.toString().trim()
                                    if (editingCol != null && emoji.isNotEmpty()) {
                                        keyboardController?.hide()
                                        viewModel.finalizeCollectionEmoji(editingCol, emoji)
                                    } else {
                                        keyboardController?.hide()
                                        viewModel.cancelEditingCollectionEmoji()
                                    }
                                }
                            )
                        }
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
