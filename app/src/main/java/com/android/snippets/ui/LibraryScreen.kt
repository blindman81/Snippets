package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.snippets.model.Photo
import com.android.snippets.ui.components.*
import com.android.snippets.ui.util.Motion
import com.android.snippets.ui.util.smoothScrollToItemSlow
import com.android.snippets.viewmodel.SnippetsViewModel
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
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
    windowSizeClass: WindowSizeClass?,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onAddPhotos: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val focusManager = LocalFocusManager.current

    var showMenuPopup by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    var modalPhoto by remember { mutableStateOf<Photo?>(null) }
    var showAddModal by remember { mutableStateOf(false) }
    var showSnippetsModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }
    var showRateModal by remember { mutableStateOf(false) }
    var showLinkModal by remember { mutableStateOf(false) }
    var showNameModal by remember { mutableStateOf(false) }

    var longPressedCollection by remember { mutableStateOf<String?>(null) }
    var deletingCollection by remember { mutableStateOf<String?>(null) }

    val isBackHandlerEnabled = showAddModal || showSnippetsModal || showDeleteModal ||
            showRateModal || showLinkModal || showNameModal || (viewModel.activePhotoId != null) ||
            isSearchOpen || viewModel.isSelectionMode || (deletingCollection != null)

    BackHandler(enabled = isBackHandlerEnabled) {
        if (showAddModal) {
            showAddModal = false
        } else if (showSnippetsModal) {
            showSnippetsModal = false
        } else if (showDeleteModal) {
            showDeleteModal = false
        } else if (showRateModal) {
            showRateModal = false
        } else if (showLinkModal) {
            showLinkModal = false
        } else if (showNameModal) {
            showNameModal = false
        } else if (viewModel.activePhotoId != null) {
            viewModel.activePhotoId = null
        } else if (isSearchOpen) {
            isSearchOpen = false
            viewModel.searchQuery = ""
        } else if (viewModel.isSelectionMode) {
            if (viewModel.searchQuery.isNotEmpty()) {
                viewModel.searchQuery = ""
            } else {
                viewModel.clearSelection()
            }
        } else if (deletingCollection != null) {
            deletingCollection = null
        }
    }

    val isAnyPopupActive = showAddModal || showSnippetsModal || showDeleteModal ||
            showRateModal || showLinkModal || showNameModal ||
            viewModel.showBulkAddToCollectionDialog || viewModel.showBulkDeleteModal ||
            viewModel.showCreateDialog || showMenuPopup || isSearchOpen ||
            (longPressedCollection != null) || (deletingCollection != null) || viewModel.showFilterSheet

    val pageTabs = remember(viewModel.userCollections) {
        listOf("Library", "Favorites") + viewModel.userCollections
    }

    val initialPage = remember(pageTabs) { pageTabs.indexOf(viewModel.libraryCurrentTab).coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageTabs.size })
    val listStates = viewModel.libraryListStates

    val currentTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Library"

    var isFabVisible by remember { mutableStateOf(true) }
    var isFirstTabLoad by remember { mutableStateOf(true) }
    LaunchedEffect(currentTab) {
        viewModel.libraryCurrentTab = currentTab
        if (isFirstTabLoad) {
            isFirstTabLoad = false
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            viewModel.carouselLazyListState.scrollToItem(0)
        }
        isFabVisible = true
    }

    val flatPhotosRaw = viewModel.filteredPhotos

    LaunchedEffect(viewModel.activePhotoId, currentTab) {
        val activeId = viewModel.activePhotoId
        if (activeId != null) {
            val tabSort = viewModel.getPhotoSortTypeFor(currentTab)
            val tabPhotos = viewModel.sortPhotos(
                flatPhotosRaw.filter { photo ->
                    when (currentTab) {
                        "Library" -> true
                        "Favorites" -> photo.isFavorite
                        else -> photo.collections.contains(currentTab)
                    }
                },
                tabSort
            )
            val targetIndex = tabPhotos.indexOfFirst { it.id == activeId }
            if (targetIndex != -1) {
                val gridState = listStates.getOrPut(currentTab) { LazyStaggeredGridState() }
                delay(80)
                gridState.smoothScrollToItemSlow(targetIndex, targetOffsetPx = 24)
            }
        }
    }

    val nestedScrollConnection = remember(isAnyPopupActive) {
        object : NestedScrollConnection {
            private var scrollBuffer = 0f
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isAnyPopupActive) return Offset.Zero
                scrollBuffer += available.y
                if (scrollBuffer < -20f) {
                    if (isFabVisible) isFabVisible = false
                    scrollBuffer = 0f
                } else if (scrollBuffer > 20f) {
                    if (!isFabVisible) isFabVisible = true
                    scrollBuffer = 0f
                }
                return Offset.Zero
            }
        }
    }

    val isCookieToolbarVisible = viewModel.isSelectionMode || isFabVisible || isAnyPopupActive

    val cookieToolbarOffset by animateDpAsState(
        targetValue = if (isCookieToolbarVisible) 0.dp else 200.dp,
        animationSpec = if (isCookieToolbarVisible) {
            Motion.ToolbarEnterSpring
        } else {
            Motion.ToolbarExitSpring
        },
        label = "cookie_toolbar_offset"
    )

    val cookieToolbarScale by animateFloatAsState(
        targetValue = if (isCookieToolbarVisible) 1f else 0.88f,
        animationSpec = if (isCookieToolbarVisible) {
            Motion.ToolbarScaleEnterSpring
        } else {
            Motion.ToolbarScaleExitSpring
        },
        label = "cookie_toolbar_scale"
    )

    val currentGridState = listStates[currentTab] ?: listStates.getOrPut(currentTab) { LazyStaggeredGridState() }

    val isCurrentTabScrolled by remember(currentTab, currentGridState) {
        derivedStateOf {
            currentGridState.firstVisibleItemIndex > 0 || currentGridState.firstVisibleItemScrollOffset > 10
        }
    }

    val isHeaderScrolled by remember(pagerState, pageTabs, listStates) {
        derivedStateOf {
            val target = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
            val tabName = pageTabs.getOrNull(target) ?: return@derivedStateOf false
            val state = listStates[tabName] ?: return@derivedStateOf false
            state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 10
        }
    }

    LaunchedEffect(isHeaderScrolled) {
        if (!isHeaderScrolled) {
            isFabVisible = true
        }
    }

    val cookieNavBarOffset by animateDpAsState(
        targetValue = if (isHeaderScrolled) (-280).dp else 0.dp,
        animationSpec = if (!isHeaderScrolled) {
            Motion.NavBarEnterSpring
        } else {
            Motion.NavBarExitSpring
        },
        label = "cookie_nav_bar_offset"
    )

    val cookieNavBarAlpha by animateFloatAsState(
        targetValue = if (isHeaderScrolled) 0f else 1f,
        animationSpec = if (!isHeaderScrolled) {
            spring(stiffness = Spring.StiffnessMediumLow)
        } else {
            MaterialTheme.motionScheme.defaultEffectsSpec()
        },
        label = "cookie_nav_bar_alpha"
    )

    val hazeState = LocalHazeState.current ?: dev.chrisbanes.haze.rememberHazeState()

    CompositionLocalProvider(
        LocalHazeState provides hazeState
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
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
                if (viewModel.isInitialLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                } else {
                    CompositionLocalProvider(
                        com.android.snippets.ui.util.LocalTiltActive provides
                                (com.android.snippets.ui.util.LocalTiltActive.current && !isAnyPopupActive)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                            val currentTabMemories = remember(viewModel.historyMemories, currentTab) {
                                when (currentTab) {
                                    "Library" -> viewModel.historyMemories
                                    "Favorites" -> viewModel.historyMemories.filter { it.isFavorite }
                                    else -> viewModel.historyMemories.filter { it.collections.contains(currentTab) }
                                }
                            }
                            val hasTabMemories = currentTabMemories.isNotEmpty()
                            val topGridPadding = if (hasTabMemories) statusBarTop + 176.dp else statusBarTop + 72.dp

                            val statusBarBlurAlpha by animateFloatAsState(
                                targetValue = if (isHeaderScrolled) 1f else 0f,
                                animationSpec = tween(durationMillis = 300),
                                label = "status_bar_blur_alpha"
                            )

                            val statusBarBaseColor = MaterialTheme.colorScheme.surfaceContainer
                            val statusBarHazeStyle = remember(statusBarBaseColor) {
                                HazeStyle(
                                    tint = HazeTint(statusBarBaseColor.copy(alpha = HazeDefaults.tintAlpha)),
                                    blurRadius = HazeDefaults.blurRadius,
                                    noiseFactor = HazeDefaults.noiseFactor
                                )
                            }

                            val tabRowScrollState = rememberScrollState()
                            val tabPositions = remember { mutableStateMapOf<String, Float>() }
                            val tabWidths = remember { mutableStateMapOf<String, Float>() }
                            val targetPage = if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
                            val activeTabTarget = pageTabs.getOrNull(targetPage) ?: "Library"

                            LaunchedEffect(pagerState.settledPage) {
                                val settledTab = pageTabs.getOrNull(pagerState.settledPage) ?: return@LaunchedEffect
                                val tabX = tabPositions[settledTab] ?: return@LaunchedEffect
                                val tabW = tabWidths[settledTab] ?: return@LaunchedEffect
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
                                viewModel = viewModel,
                                listStates = listStates,
                                topGridPadding = topGridPadding,
                                statusBarTop = statusBarTop,
                                hazeState = hazeState,
                                showAddModal = showAddModal,
                                modalPhotoId = modalPhoto?.id,
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                onPhotoClick = { photo ->
                                    if (viewModel.isSelectionMode) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        viewModel.toggleSelection(photo.id)
                                    } else {
                                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                        val isExpanding = viewModel.activePhotoId != photo.id
                                        viewModel.activePhotoId = if (isExpanding) photo.id else null
                                    }
                                },
                                onPhotoLongClick = { photo ->
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    if (!viewModel.isSelectionMode) viewModel.toggleSelection(photo.id)
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

                            // 2. Status Bar Progressive Blur
                            LibraryStatusBarBlur(
                                statusBarTop = statusBarTop,
                                alpha = { statusBarBlurAlpha },
                                hazeState = hazeState,
                                style = statusBarHazeStyle,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )

                            // 3. Cookie Nav Bar & Memories Carousel
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        translationY = cookieNavBarOffset.toPx()
                                        alpha = cookieNavBarAlpha
                                        clip = false
                                    }
                            ) {
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
                                    statusBarTop = statusBarTop,
                                    cookieNavBarOffset = 0.dp,
                                    cookieNavBarAlpha = 1f,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                MemoriesCarousel(
                                    currentTab = currentTab,
                                    viewModel = viewModel,
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    modifier = Modifier.fillMaxWidth()
                                )
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
                                cookieToolbarOffset = { cookieToolbarOffset },
                                cookieToolbarScale = { cookieToolbarScale },
                                isMenuOpen = showMenuPopup,
                                isManageOpen = longPressedCollection != null,
                                isFilterOpen = viewModel.showFilterSheet,
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
                                    if (viewModel.showFilterSheet) {
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
                                visible = viewModel.showFilterSheet,
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

                            // 8. Scroll to Top Floating Button
                            AnimatedVisibility(
                                visible = isCurrentTabScrolled && !pagerState.isScrollInProgress && !viewModel.isSelectionMode && !isSearchOpen && !isAnyPopupActive,
                                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                        scaleIn(
                                            initialScale = 0.8f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ),
                                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                        scaleOut(
                                            targetScale = 0.8f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        ),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .safeDrawingPadding()
                                    .padding(bottom = 132.dp)
                                    .graphicsLayer {
                                        translationY = cookieToolbarOffset.toPx()
                                    }
                            ) {
                                AnimatedCookieButton(
                                    onClick = {
                                        val state = listStates[currentTab]
                                        if (state != null) {
                                            scope.launch {
                                                state.animateScrollToItem(0)
                                            }
                                        }
                                    },
                                    icon = Icons.Rounded.KeyboardArrowUp,
                                    contentDescription = "Scroll to top",
                                    tooltip = "Scroll to top",
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    size = 48.dp,
                                    isSpinning = !isAnyPopupActive
                                )
                            }

                            // 9. Photo & Collection Modals
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
                        }
                    }
                }
            }
        }
    }
}
