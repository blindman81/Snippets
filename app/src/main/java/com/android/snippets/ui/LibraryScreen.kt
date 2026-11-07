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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.abs
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
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showMenuPopup by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
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

    val isBackHandlerEnabled = showAddModal || showSnippetsModal || showDeleteModal ||
            showRateModal || showLinkModal || showNameModal || (uiState.activePhotoId != null) ||
            isSearchOpen || uiState.isSelectionMode || (deletingCollection != null) ||
            (uiState.editingCollectionEmoji != null)

    BackHandler(enabled = isBackHandlerEnabled) {
        if (uiState.editingCollectionEmoji != null) {
            keyboardController?.hide()
            viewModel.cancelEditingCollectionEmoji()
        } else if (showAddModal) {
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
        } else if (uiState.activePhotoId != null) {
            viewModel.activePhotoId = null
        } else if (isSearchOpen) {
            isSearchOpen = false
            scope.launch {
                kotlinx.coroutines.delay(200L)
                viewModel.searchQuery = ""
            }
        } else if (uiState.isSelectionMode) {
            if (uiState.searchQuery.isNotEmpty()) {
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
            uiState.showBulkAddToCollectionDialog || uiState.showBulkDeleteModal ||
            uiState.showCreateDialog || showMenuPopup || isSearchOpen ||
            (longPressedCollection != null) || (deletingCollection != null) || uiState.showFilterSheet

    val pageTabs = remember(uiState.userCollections) {
        listOf("Library", "Favorites") + uiState.userCollections
    }

    val initialPage = remember(pageTabs) { pageTabs.indexOf(uiState.libraryCurrentTab).coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageTabs.size })
    val listStates = remember { mutableStateMapOf<String, androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState>() }
    val carouselLazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

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

    var isFabVisible by remember { mutableStateOf(true) }
    var isFirstTabLoad by remember { mutableStateOf(true) }
    LaunchedEffect(currentTab) {
        viewModel.libraryCurrentTab = currentTab
        if (isFirstTabLoad) {
            isFirstTabLoad = false
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            carouselLazyListState.scrollToItem(0)
        }
        isFabVisible = true
    }

    LaunchedEffect(viewModel.libraryCurrentTab, pageTabs) {
        val targetIndex = pageTabs.indexOf(viewModel.libraryCurrentTab)
        if (targetIndex != -1 && targetIndex != pagerState.currentPage) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    LaunchedEffect(viewModel.editingCollectionEmoji) {
        val editingCol = viewModel.editingCollectionEmoji
        if (editingCol != null) {
            emojiTextFieldState.clearText()
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

    val flatPhotosRaw = uiState.filteredPhotos

    LaunchedEffect(uiState.activePhotoId, currentTab) {
        val activeId = uiState.activePhotoId
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
                delay(180)
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

    val isCookieToolbarVisible = uiState.isSelectionMode || isFabVisible || isAnyPopupActive

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

    val density = LocalDensity.current
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val currentGridState = listStates[currentTab] ?: listStates.getOrPut(currentTab) { LazyStaggeredGridState() }

    val isCurrentTabScrolled by remember(currentTab, currentGridState) {
        derivedStateOf {
            currentGridState.firstVisibleItemIndex > 0 || currentGridState.firstVisibleItemScrollOffset > 10
        }
    }

    val getCollapseDistanceForTab = remember(density, statusBarTop, memoryViewModel.historyMemories) {
        { tabName: String? ->
            if (tabName == null) {
                with(density) { (statusBarTop + 72.dp).toPx() }
            } else {
                val hasMemories = when (tabName) {
                    "Library" -> memoryViewModel.historyMemories.isNotEmpty()
                    "Favorites" -> memoryViewModel.historyMemories.any { it.isFavorite }
                    else -> memoryViewModel.historyMemories.any { it.collections.contains(tabName) }
                }
                val paddingDp = if (hasMemories) statusBarTop + 176.dp else statusBarTop + 72.dp
                with(density) { paddingDp.toPx() }
            }
        }
    }

    val getScrollOffsetForTab = remember(listStates, getCollapseDistanceForTab) {
        { tabName: String? ->
            if (tabName == null) {
                0f
            } else {
                val state = listStates[tabName]
                if (state == null) {
                    0f
                } else {
                    val maxCollapse = getCollapseDistanceForTab(tabName)
                    if (state.firstVisibleItemIndex > 0) {
                        maxCollapse
                    } else {
                        state.firstVisibleItemScrollOffset.toFloat().coerceIn(0f, maxCollapse)
                    }
                }
            }
        }
    }

    val currentHeaderScrollOffset by remember(pagerState, pageTabs, getScrollOffsetForTab) {
        derivedStateOf {
            val currentPage = pagerState.currentPage
            val currentTabName = pageTabs.getOrNull(currentPage)
            val currentOffset = getScrollOffsetForTab(currentTabName)
            if (pagerState.isScrollInProgress) {
                val targetPage = pagerState.targetPage
                if (targetPage != currentPage && targetPage in pageTabs.indices) {
                    val targetTabName = pageTabs.getOrNull(targetPage)
                    val targetOffset = getScrollOffsetForTab(targetTabName)
                    val fraction = abs(pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
                    currentOffset + (targetOffset - currentOffset) * fraction
                } else {
                    currentOffset
                }
            } else {
                currentOffset
            }
        }
    }

    val currentMaxCollapseDistance by remember(pagerState, pageTabs, getCollapseDistanceForTab) {
        derivedStateOf {
            val currentPage = pagerState.currentPage
            val currentTabName = pageTabs.getOrNull(currentPage)
            val currentMax = getCollapseDistanceForTab(currentTabName)
            if (pagerState.isScrollInProgress) {
                val targetPage = pagerState.targetPage
                if (targetPage != currentPage && targetPage in pageTabs.indices) {
                    val targetTabName = pageTabs.getOrNull(targetPage)
                    val targetMax = getCollapseDistanceForTab(targetTabName)
                    val fraction = abs(pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)
                    currentMax + (targetMax - currentMax) * fraction
                } else {
                    currentMax
                }
            } else {
                currentMax
            }
        }
    }

    val headerCollapseProgress by remember {
        derivedStateOf {
            val maxCollapse = currentMaxCollapseDistance
            if (maxCollapse > 0f) {
                (currentHeaderScrollOffset / maxCollapse).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }

    val isHeaderScrolled by remember {
        derivedStateOf {
            headerCollapseProgress > 0.05f
        }
    }

    LaunchedEffect(isHeaderScrolled) {
        if (!isHeaderScrolled) {
            isFabVisible = true
        }
    }

    val statusBarBlurAlpha by remember {
        derivedStateOf {
            (headerCollapseProgress * 1.5f).coerceIn(0f, 1f)
        }
    }

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
                            val currentTabMemories = remember(memoryViewModel.historyMemories, currentTab) {
                                when (currentTab) {
                                    "Library" -> memoryViewModel.historyMemories
                                    "Favorites" -> memoryViewModel.historyMemories.filter { it.isFavorite }
                                    else -> memoryViewModel.historyMemories.filter { it.collections.contains(currentTab) }
                                }
                            }
                            val hasTabMemories = currentTabMemories.isNotEmpty()
                            val topGridPadding = if (hasTabMemories) statusBarTop + 176.dp else statusBarTop + 72.dp

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

                            LaunchedEffect(activeTabTarget) {
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
                                        translationY = -currentHeaderScrollOffset
                                        alpha = (1f - (headerCollapseProgress - 0.4f) / 0.6f).coerceIn(0f, 1f)
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
                                    onScrollCarouselToTop = {
                                        scope.launch {
                                            carouselLazyListState.animateScrollToItem(0)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                MemoriesCarousel(
                                    currentTab = currentTab,
                                    viewModel = memoryViewModel,
                                    lazyListState = carouselLazyListState,
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
            val type = Character.getType(char).toInt()
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
