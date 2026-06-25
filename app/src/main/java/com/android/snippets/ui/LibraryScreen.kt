package com.android.snippets.ui
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow

import com.android.snippets.ui.components.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection


import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import com.android.snippets.viewmodel.Screen
import com.android.snippets.model.Photo
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import com.android.snippets.ui.components.LoadingIndicator
import com.android.snippets.ui.components.HistoryBottomSheet

import androidx.compose.foundation.pager.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.platform.LocalContext

import com.ln.android.snippets.R
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.android.snippets.viewmodel.SnippetsViewModel
import android.view.HapticFeedbackConstants
import kotlinx.coroutines.launch
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.Remove

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryScreen(
    viewModel: SnippetsViewModel,
    windowSizeClass: WindowSizeClass? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onAddPhotos: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = LocalView.current

    var showMenuPopup by remember { mutableStateOf(false) }
    var showCollectionsPopup by remember { mutableStateOf(false) }
    var filterOffset by remember { mutableStateOf(0f) }



    var isSearchOpen by remember { mutableStateOf(false) }
    var showHistoryBottomSheet by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    BackHandler {
        if (isSearchOpen) {
            isSearchOpen = false
            viewModel.searchQuery = ""
        } else if (viewModel.isSelectionMode) {
            viewModel.clearSelection()
        } else if (showHistoryBottomSheet) {
            showHistoryBottomSheet = false
        } else if (showMenuPopup) {
            showMenuPopup = false
        } else if (showCollectionsPopup) {
            showCollectionsPopup = false
        }
    }
    
    val isAnyPopupActive = viewModel.showBulkAddToCollectionDialog || viewModel.showBulkDeleteModal || viewModel.showCreateDialog || showMenuPopup || showCollectionsPopup || isSearchOpen || showHistoryBottomSheet
    val allowMemorySpin = !isAnyPopupActive
    val curated = viewModel.curatedMemories
    
    var longPressedCollection by remember { mutableStateOf<String?>(null) }
    var renamingCollection by remember { mutableStateOf<String?>(null) }
    var deletingCollection by remember { mutableStateOf<String?>(null) }
    
    val pageTabs = remember(viewModel.userCollections) {
        listOf("Library", "Favorites") + viewModel.userCollections
    }
    
    val initialPage = remember(pageTabs) { pageTabs.indexOf(viewModel.libraryCurrentTab).coerceAtLeast(0) }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageTabs.size })
    val listStates = viewModel.libraryListStates
    
    val currentTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Library"
    
    var isFirstTabLoad by remember { mutableStateOf(true) }
    LaunchedEffect(currentTab) {
        viewModel.libraryCurrentTab = currentTab
        if (isFirstTabLoad) {
            isFirstTabLoad = false
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }
    
    val flatPhotosRaw = viewModel.filteredPhotos
    val currentTabSort = viewModel.getPhotoSortTypeFor(currentTab)
    
    val filteredFlatPhotos = remember(flatPhotosRaw, currentTab, currentTabSort) {
        val filtered = flatPhotosRaw.filter { photo ->
            when (currentTab) {
                "Library" -> true
                "Favorites" -> photo.isFavorite
                else -> photo.collections.contains(currentTab)
            }
        }
        viewModel.sortPhotos(filtered, currentTabSort)
    }
    
    val flatPhotos = filteredFlatPhotos
    
    LaunchedEffect(viewModel.activePhotoId, viewModel.currentScreen) {
        if (
            viewModel.currentScreen != Screen.Detail &&
            viewModel.activePhotoId != null
        ) {
            val targetIndex = flatPhotos.indexOfFirst { it.id == viewModel.activePhotoId }
            if (targetIndex != -1) {
                val gridState = listStates.getOrPut(currentTab) { LazyStaggeredGridState() }
                kotlinx.coroutines.delay(250)
                val isVisible = gridState.layoutInfo.visibleItemsInfo.any { it.index == targetIndex }
                if (!isVisible) {
                    gridState.scrollToItem(targetIndex)
                }
            }
        }
    }
    val startIndex = 0

    var isFabVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -5) {
                    isFabVisible = false
                } else if (available.y > 5) {
                    isFabVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val topBarOffset by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "topbar_offset"
    )

    val toolbarOffset by animateDpAsState(
        targetValue = if (viewModel.isSelectionMode || isFabVisible) 0.dp else 200.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "toolbar_offset"
    )

    val isScrolled by remember {
        derivedStateOf {
            val state = listStates[currentTab]
            (state?.firstVisibleItemIndex ?: 0) > 0 || (state?.firstVisibleItemScrollOffset ?: 0) > 0
        }
    }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
        ) {
            @OptIn(ExperimentalMaterial3Api::class)
            if (viewModel.isInitialLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    val maxTopPadding = 12.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    val minTopPadding = 4.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    val currentTopPadding by animateDpAsState(
                        targetValue = if (isScrolled) minTopPadding else maxTopPadding,
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                        label = "top_padding_anim"
                    )

                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(currentTopPadding))
                        
Surface(
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                val allTabs = listOf("Library", "Favorites") + viewModel.userCollections + listOf("New Collection")
                                val selectedTabIndex = allTabs.indexOf(currentTab).coerceAtLeast(0)
                                PrimaryScrollableTabRow(
                                    selectedTabIndex = selectedTabIndex,
                                    edgePadding = 16.dp,
                                    divider = {},
                                    indicator = {
                                        TabRowDefaults.PrimaryIndicator(
                                            modifier = Modifier.tabIndicatorOffset(
                                                selectedTabIndex = selectedTabIndex,
                                                matchContentSize = true
                                            ),
                                            width = androidx.compose.ui.unit.Dp.Unspecified,
                                            shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                        )
                                    }
                                ) {
                                    allTabs.forEach { tabName ->
                                        val isSelected = tabName == currentTab
                                        Tab(
                                            selected = isSelected,
                                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            onClick = {
                                                if (tabName == "New Collection") {
                                                    viewModel.navigateCollections(focusCreate = true)
                                                } else if (isSelected) {
                                                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                                    longPressedCollection = tabName
                                                } else {
                                                    val pageIndex = pageTabs.indexOf(tabName)
                                                    if (pageIndex != -1) {
                                                        scope.launch { pagerState.animateScrollToPage(pageIndex) }
                                                    }
                                                }
                                            },
                                            modifier = Modifier.then(
                                                if (tabName != "New Collection") {
                                                    @OptIn(ExperimentalFoundationApi::class)
                                                    Modifier.combinedClickable(
                                                        onClick = { 
                                                            val pageIndex = pageTabs.indexOf(tabName)
                                                            if (pageIndex != -1) {
                                                                if (pagerState.currentPage == pageIndex && tabName != "New Collection") {
                                                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                    longPressedCollection = tabName
                                                                } else {
                                                                    scope.launch { pagerState.animateScrollToPage(pageIndex) }
                                                                }
                                                            }
                                                        },
                                                        onLongClick = {
                                                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                            longPressedCollection = tabName
                                                        }
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            ),
                                            text = {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(if (isSelected) 2.dp else 4.dp),
                                                    modifier = Modifier.padding(top = 2.dp, bottom = 0.dp)
                                                ) {
                                                    val iconOrEmoji = when (tabName) {
                                                        "New Collection" -> Icons.Default.Add
                                                        "Library" -> Icons.Default.PhotoLibrary
                                                        else -> viewModel.getCollectionIcon(tabName)
                                                    }
                                                    val rotation = remember { Animatable(0f) }
                                                    LaunchedEffect(isSelected) {
                                                        if (isSelected) {
                                                            rotation.animateTo(
                                                                targetValue = rotation.value + 360f,
                                                                animationSpec = tween(
                                                                    durationMillis = 600,
                                                                    easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
                                                                )
                                                            )
                                                        }
                                                    }
                                                    Surface(
                                                        shape = CookieShape,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .graphicsLayer { rotationZ = rotation.value }
                                                    ) {
                                                        Box(
                                                            contentAlignment = Alignment.Center,
                                                            modifier = Modifier.graphicsLayer { rotationZ = -rotation.value }
                                                        ) {
                                                            if (iconOrEmoji is androidx.compose.ui.graphics.vector.ImageVector) {
                                                                Icon(iconOrEmoji, contentDescription = null, modifier = Modifier.size(20.dp))
                                                            } else if (iconOrEmoji is String) {
                                                                Text(text = iconOrEmoji, fontSize = 18.sp)
                                                            }
                                                        }
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        if (isSelected && tabName != "New Collection") {
                                                            Spacer(modifier = Modifier.size(16.dp))
                                                        }
                                                        Text(
                                                            text = tabName,
                                                            style = MaterialTheme.typography.titleMedium.copy(
                                                                fontFamily = com.android.snippets.ui.theme.GoogleSans
                                                            ),
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                        if (isSelected && tabName != "New Collection") {
                                                            val isBottomSheetOpen = longPressedCollection == tabName
                                                            Icon(
                                                                painter = painterResource(
                                                                    id = if (isBottomSheetOpen) R.drawable.ic_arrow_dropdown_filled else R.drawable.ic_arrow_dropdown
                                                                ),
                                                                contentDescription = null,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                                

                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    pageSpacing = 16.dp
                                ) { page ->
                                    val tabForPage = pageTabs.getOrNull(page) ?: "Library"
                                    val tabSortType = viewModel.getPhotoSortTypeFor(tabForPage)
                                    val pageFilteredPhotos = remember(flatPhotosRaw, tabForPage, tabSortType) {
                                        val filtered = flatPhotosRaw.filter { photo ->
                                            when (tabForPage) {
                                                "Library" -> true
                                                "Favorites" -> photo.isFavorite
                                                else -> photo.collections.contains(tabForPage)
                                            }
                                        }
                                        viewModel.sortPhotos(filtered, tabSortType)
                                    }

                                    val pageListState = listStates.getOrPut(tabForPage) { LazyStaggeredGridState() }
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        if (pageFilteredPhotos.isEmpty()) {
                                            when (tabForPage) {
                                                "Library" -> EmptyLibraryState()
                                                "Favorites" -> EmptyFavoritesState()
                                                else -> EmptyCollectionState()
                                            }
                                        } else {
                                            val gridColumns = when (windowSizeClass?.widthSizeClass) {
                                                WindowWidthSizeClass.Expanded -> 4
                                                WindowWidthSizeClass.Medium -> 3
                                                else -> 2
                                            }
                                            LazyVerticalStaggeredGrid(
                                                columns = StaggeredGridCells.Fixed(gridColumns),
                                                state = pageListState,
                                                modifier = Modifier.fillMaxSize(),
                                                contentPadding = PaddingValues(
                                                    start = 0.dp,
                                                    end = 0.dp,
                                                    top = 0.dp,
                                                    bottom = 100.dp
                                                ),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalItemSpacing = 4.dp
                                            ) {
                                                items(pageFilteredPhotos, key = { it.id }) { photo ->
                                                    PhotoMasonryItem(
                                                        photo = photo,
                                                        isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                                                        selectionMode = viewModel.isSelectionMode,
                                                        showFavoriteIcon = tabForPage != "Favorites",
                                                        matchingSnippetsCount = getMatchingSnippetsCount(photo, viewModel),
                                                        sharedTransitionScope = sharedTransitionScope,
                                                        animatedVisibilityScope = animatedVisibilityScope,
                                                        onClick = {
                                                            if (viewModel.isSelectionMode) {
                                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                viewModel.toggleSelection(photo.id)
                                                            } else {
                                                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                                                if (windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded) {
                                                                    viewModel.activePhotoId = photo.id
                                                                } else {
                                                                    viewModel.openDetail(photo.id, Screen.Library)
                                                                }
                                                            }
                                                        },
                                                        onLongClick = {
                                                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                            if (!viewModel.isSelectionMode) viewModel.toggleSelection(photo.id)
                                                        },
                                                        fillCard = false,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    } // end of Box
                                } // end of HorizontalPager
                            } // end of Column inside Surface
                        } // end of Surface
                    } // end of Column
                // Bottom Pill â€” morphs between normal controls and inline search bar
                val isToolbarVisibleState = !viewModel.isSelectionMode && isFabVisible

                val motionScheme = MaterialTheme.motionScheme
                AnimatedContent(
                    targetState = when {
                        viewModel.isSelectionMode -> 2
                        isSearchOpen -> 1
                        else -> 0
                    },
                    transitionSpec = {
                        (fadeIn(motionScheme.defaultEffectsSpec()) + scaleIn(initialScale = 0.92f, animationSpec = motionScheme.defaultSpatialSpec()))
                            .togetherWith(fadeOut(motionScheme.fastEffectsSpec()) + scaleOut(targetScale = 0.92f, animationSpec = motionScheme.fastSpatialSpec()))
                    },
                    label = "pill_mode",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .safeDrawingPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                        .offset(y = toolbarOffset)
                ) { searchMode ->
                    if (searchMode == 2) {
                        SelectionToolbar(
                            viewModel = viewModel,
                            onDeleteClick = { viewModel.showBulkDeleteModal = true },
                            isSpinning = !isAnyPopupActive,
                            isScrolled = isScrolled
                        )
                    } else if (searchMode == 1) {
                        // â”€â”€ SEARCH MODE â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shadowElevation = 8.dp,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Close / back
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                        isSearchOpen = false
                                        viewModel.searchQuery = ""
                                    },
                                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Close Search",
                                    tooltip = "Close",
                                    isSpinning = true,
                                    size = 48.dp,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                // Text field
                                BasicTextField(
                                    value = viewModel.searchQuery,
                                    onValueChange = { viewModel.searchQuery = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(searchFocusRequester),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = {
                                        viewModel.addRecentSearch(viewModel.searchQuery)
                                        isSearchOpen = false
                                        focusManager.clearFocus()
                                    }),
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (viewModel.searchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search...",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                // Clear button
                                if (viewModel.searchQuery.isNotEmpty()) {
                                    AnimatedCookieButton(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            viewModel.searchQuery = ""
                                        },
                                        icon = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tooltip = "Clear",
                                        isSpinning = true,
                                        size = 48.dp,
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    } else {
                        // â”€â”€ NORMAL MODE â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shadowElevation = 8.dp,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(CircleShape)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Menu
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        showMenuPopup = !showMenuPopup
                                        showCollectionsPopup = false
                                    },
                                    icon = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tooltip = "Menu",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = if (showMenuPopup) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (showMenuPopup) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                // Search
                                val isSearchActive = viewModel.searchQuery.isNotEmpty()
                                val searchIcon = if (isSearchActive && flatPhotos.isNotEmpty()) SearchSuccessIcon() else Icons.Default.Search
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        isSearchOpen = true
                                    },
                                    icon = searchIcon,
                                    contentDescription = "Search",
                                    tooltip = "Search",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (isSearchActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                // History
                                Box {
                                    AnimatedCookieButton(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            showHistoryBottomSheet = true
                                        },
                                        icon = Icons.Default.History,
                                        contentDescription = "History",
                                        tooltip = "History",
                                        isSpinning = !isAnyPopupActive,
                                        size = 48.dp,
                                        containerColor = if (showHistoryBottomSheet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = if (showHistoryBottomSheet) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    // Unviewed-memories indicator dot
                                    if (viewModel.hasUnviewedMemories) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = (-4).dp, y = 4.dp)
                                                .size(8.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }



                                // Filter
                                val isFilterActive = viewModel.selectedFilterSnippets.isNotEmpty() || viewModel.showFilterSheet
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        viewModel.filteringCategory = currentTab
                                        viewModel.navigateFilter()
                                    },
                                    icon = Icons.Default.FilterList,
                                    contentDescription = "Filters",
                                    tooltip = "Filters",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (isFilterActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        MenuBottomSheet(
                            show = showMenuPopup,
                            onDismissRequest = { showMenuPopup = false },
                            viewModel = viewModel,
                            view = view
                        )

                        HistoryBottomSheet(
                            show = showHistoryBottomSheet,
                            onDismissRequest = { showHistoryBottomSheet = false },
                            viewModel = viewModel,
                            view = view
                        )
                    }
                }
                }
            if (longPressedCollection != null) {
                ModalBottomSheet(
                    onDismissRequest = { longPressedCollection = null },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp)
                    ) {
                        val isSystemCollection = longPressedCollection == "Library" || longPressedCollection == "Favorites"
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "VIEW",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp)
                                .padding(bottom = 8.dp)
                        )

                            androidx.compose.material3.ButtonGroup(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(androidx.compose.material3.ButtonGroupDefaults.ConnectedSpaceBetween),
                                overflowIndicator = {}
                            ) {
                                val dateOptions = listOf(com.android.snippets.viewmodel.PhotoSortType.DateNewest, com.android.snippets.viewmodel.PhotoSortType.DateOldest)
                                val dateLabels = listOf("Newest first", "Oldest first")
                                val dateIcons = listOf(Icons.Default.ArrowDownward, Icons.Default.ArrowUpward)

                                dateOptions.forEachIndexed { index, option ->
                                    val isSelected = viewModel.getPhotoSortTypeFor(longPressedCollection ?: "Library") == option
                                    surfaceContainerHighestToggleableItem(
                                        weight = 1f,
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (!isSelected) {
                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                viewModel.setPhotoSortTypeFor(longPressedCollection ?: "Library", option)
                                                listStates.values.forEach { state ->
                                                    scope.launch { state.scrollToItem(0) }
                                                }
                                            }
                                        },
                                        icon = { Icon(dateIcons[index], null, modifier = Modifier.size(18.dp)) },
                                        label = dateLabels[index]
                                    )
                                }
                            }

                            androidx.compose.material3.ButtonGroup(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(androidx.compose.material3.ButtonGroupDefaults.ConnectedSpaceBetween),
                                overflowIndicator = {}
                            ) {
                                val snippetOptions = listOf(com.android.snippets.viewmodel.PhotoSortType.MostSnippets, com.android.snippets.viewmodel.PhotoSortType.LeastSnippets)
                                val snippetLabels = listOf("Most snippets", "Least snippets")
                                val snippetIcons = listOf(androidx.compose.material.icons.Icons.Default.TextSnippet, androidx.compose.material.icons.Icons.Default.TextSnippet)

                                snippetOptions.forEachIndexed { index, option ->
                                    val isSelected = viewModel.getPhotoSortTypeFor(longPressedCollection ?: "Library") == option
                                    surfaceContainerHighestToggleableItem(
                                        weight = 1f,
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (!isSelected) {
                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                viewModel.setPhotoSortTypeFor(longPressedCollection ?: "Library", option)
                                                listStates.values.forEach { state ->
                                                    scope.launch { state.scrollToItem(0) }
                                                }
                                            }
                                        },
                                        icon = { Icon(snippetIcons[index], null, modifier = Modifier.size(18.dp)) },
                                        label = snippetLabels[index]
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "ACTIONS",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 28.dp)
                                    .padding(bottom = 8.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onAddPhotos(longPressedCollection!!)
                                        longPressedCollection = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add photos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }

                                if (longPressedCollection != "Library") {
                                    Button(
                                        onClick = {
                                            viewModel.startCollectionRemoval(longPressedCollection!!)
                                            longPressedCollection = null
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        contentPadding = PaddingValues(16.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Remove photos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            if (!isSystemCollection) {
                                class Option(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val isDestructive: Boolean = false, val action: () -> Unit)
                                val options = buildList {
                                    add(Option("Edit name", Icons.Default.Edit) {
                                        renamingCollection = longPressedCollection
                                        longPressedCollection = null
                                    })
                                    add(Option("Pick an emoji", Icons.Default.AddReaction) {
                                        viewModel.navigateSelectIcon(longPressedCollection!!)
                                        longPressedCollection = null
                                    })
                                    val collectionIndex = viewModel.userCollections.indexOf(longPressedCollection)
                                    if (collectionIndex > 0) {
                                        add(Option("Move left", Icons.AutoMirrored.Filled.ArrowBack) {
                                            val col = longPressedCollection!!
                                            viewModel.moveCollectionLeft(col)
                                            longPressedCollection = null
                                            val currentIndex = pageTabs.indexOf(col)
                                            if (currentIndex > 2) {
                                                scope.launch { pagerState.scrollToPage(currentIndex - 1) }
                                            }
                                        })
                                    }
                                    if (collectionIndex != -1 && collectionIndex < viewModel.userCollections.size - 1) {
                                        add(Option("Move right", Icons.AutoMirrored.Filled.ArrowForward) {
                                            val col = longPressedCollection!!
                                            viewModel.moveCollectionRight(col)
                                            longPressedCollection = null
                                            val currentIndex = pageTabs.indexOf(col)
                                            if (currentIndex != -1 && currentIndex < pageTabs.size - 1) {
                                                scope.launch { pagerState.scrollToPage(currentIndex + 1) }
                                            }
                                        })
                                    }
                                    add(Option("Delete", Icons.Default.Delete, isDestructive = true) {
                                        deletingCollection = longPressedCollection
                                        longPressedCollection = null
                                    })
                                }

                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    options.forEachIndexed { index, option ->
                                        val shape = when {
                                            options.size == 1 -> RoundedCornerShape(24.dp)
                                            index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                                            index == options.size - 1 -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                                            else -> RoundedCornerShape(2.dp)
                                        }
                                        Surface(
                                            shape = shape,
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(shape)
                                                .clickable { option.action() }
                                        ) {
                                            ListItem(
                                                headlineContent = { Text(option.name, fontWeight = FontWeight.Bold) },
                                                leadingContent = { Icon(option.icon, contentDescription = null) },
                                                colors = ListItemDefaults.colors(
                                                    containerColor = Color.Transparent,
                                                    headlineColor = Color.Unspecified,
                                                    leadingIconColor = Color.Unspecified
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

                renamingCollection?.let { oldName ->
                    var newName by remember { mutableStateOf(oldName) }
                    AlertDialog(
                        onDismissRequest = { renamingCollection = null },
                        title = { Text("Rename collection") },
                        text = {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (newName.isNotBlank() && newName != oldName) {
                                    viewModel.renameCollection(oldName, newName.trim())
                                }
                                renamingCollection = null
                            }) {
                                Text("Rename")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { renamingCollection = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                deletingCollection?.let { collectionName ->
                    AlertDialog(
                        onDismissRequest = { deletingCollection = null },
                        title = { Text("Delete collection") },
                        text = { Text("Are you sure you want to delete '$collectionName'? Your photos will be saved.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteCollection(collectionName)
                                    if (currentTab == collectionName) {
                                        scope.launch { pagerState.animateScrollToPage(0) }
                                    }
                                    deletingCollection = null
                                }
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.secondary)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { deletingCollection = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }





@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
private fun ButtonGroupScope.surfaceContainerHighestToggleableItem(
    weight: Float,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit,
    label: String
) {
    val itemModifier = Modifier.weight(weight)
    customItem(
        buttonGroupContent = {
            ToggleButton(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = itemModifier,
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                icon()
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(label)
            }
        },
        menuContent = { state ->
            DropdownMenuItem(
                leadingIcon = icon,
                text = { Text(label) },
                onClick = {
                    onCheckedChange(!checked)
                    state.dismiss()
                }
            )
        }
    )
}
