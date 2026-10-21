package com.android.snippets.ui
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.boundsInParent

import com.android.snippets.ui.components.*
import com.android.snippets.ui.util.Motion
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection


import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.ui.geometry.Offset

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.android.snippets.ui.theme.GoogleSansFlexWide
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.viewmodel.Screen
import com.android.snippets.model.Photo
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import com.android.snippets.ui.components.LoadingIndicator
import com.android.snippets.ui.components.HistoryBottomSheet
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

import androidx.compose.foundation.pager.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.android.snippets.ui.components.BlurredSurface
import com.android.snippets.ui.components.BlurredModalBottomSheet
import com.android.snippets.ui.components.BlurredAlertDialog
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryScreen(
    viewModel: SnippetsViewModel,
    windowSizeClass: WindowSizeClass?,
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
    val searchFocusRequester = remember { FocusRequester() }

    var modalPhoto by remember { mutableStateOf<Photo?>(null) }
    var showAddModal by remember { mutableStateOf(false) }
    var showSnippetsModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }
    var showRateModal by remember { mutableStateOf(false) }
    var showLinkModal by remember { mutableStateOf(false) }
    var showNameModal by remember { mutableStateOf(false) }

    val isBackHandlerEnabled = showAddModal || showSnippetsModal || showDeleteModal || showRateModal || showLinkModal || showNameModal || (viewModel.activePhotoId != null) || isSearchOpen || viewModel.isSelectionMode || viewModel.showHistoryBottomSheet || showMenuPopup || showCollectionsPopup
    AppPredictiveBackHandler(enabled = isBackHandlerEnabled) {
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
            viewModel.clearSelection()
        } else if (viewModel.showHistoryBottomSheet) {
            viewModel.showHistoryBottomSheet = false
        } else if (showMenuPopup) {
            showMenuPopup = false
        } else if (showCollectionsPopup) {
            showCollectionsPopup = false
        }
    }
    
    var longPressedCollection by remember { mutableStateOf<String?>(null) }
    var deletingCollection by remember { mutableStateOf<String?>(null) }

    val isAnyPopupActive = showAddModal || showSnippetsModal || showDeleteModal || showRateModal || showLinkModal || showNameModal || viewModel.showBulkAddToCollectionDialog || viewModel.showBulkDeleteModal || viewModel.showCreateDialog || showMenuPopup || showCollectionsPopup || isSearchOpen || viewModel.showHistoryBottomSheet || (longPressedCollection != null) || (deletingCollection != null)
    val allowMemorySpin = !isAnyPopupActive
    val curated = viewModel.curatedMemories
    
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
                kotlinx.coroutines.delay(80)
                gridState.smoothScrollToItemSlow(targetIndex, targetOffsetPx = 24)
            }
        }
    }
    val startIndex = 0

    var isFabVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            private var scrollBuffer = 0f
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
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

    val isCookieToolbarVisible = viewModel.isSelectionMode || isFabVisible

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

    val isScrolled by remember(currentTab, currentGridState) {
        derivedStateOf {
            currentGridState.firstVisibleItemIndex > 0 || currentGridState.firstVisibleItemScrollOffset > 10
        }
    }

    LaunchedEffect(isScrolled) {
        if (!isScrolled) {
            isFabVisible = true
        }
    }

    val cookieNavBarOffset by animateDpAsState(
        targetValue = if (isScrolled) (-100).dp else 0.dp,
        animationSpec = if (!isScrolled) {
            Motion.NavBarEnterSpring
        } else {
            Motion.NavBarExitSpring
        },
        label = "cookie_nav_bar_offset"
    )

    val cookieNavBarAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 0f else 1f,
        animationSpec = if (!isScrolled) {
            spring(stiffness = Spring.StiffnessMediumLow)
        } else {
            MaterialTheme.motionScheme.defaultEffectsSpec()
        },
        label = "cookie_nav_bar_alpha"
    )

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val hazeState = com.android.snippets.ui.components.LocalHazeState.current ?: dev.chrisbanes.haze.rememberHazeState()

    androidx.compose.runtime.CompositionLocalProvider(
        com.android.snippets.ui.components.LocalHazeState provides hazeState
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    val topGridPadding = statusBarTop + 72.dp

                    val statusBarBlurAlpha by animateFloatAsState(
                        targetValue = if (isScrolled) 1f else 0f,
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

                    val allTabs = pageTabs
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

                    HorizontalPager(
                        state = pagerState,
                        key = { page -> pageTabs.getOrNull(page) ?: page },
                        beyondViewportPageCount = 1,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    Modifier.hazeSource(state = hazeState)
                                } else Modifier
                            ),
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

                        var lastSortType by remember(tabForPage) { mutableStateOf(tabSortType) }
                        if (lastSortType != tabSortType) {
                            lastSortType = tabSortType
                            listStates[tabForPage] = LazyStaggeredGridState()
                        }

                        if (pageFilteredPhotos.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .padding(top = topGridPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                when (tabForPage) {
                                    "Favorites" -> EmptyFavoritesState()
                                    "Library"   -> EmptyLibraryState()
                                    else        -> EmptyCollectionState()
                                }
                            }
                        } else {
                            val pageListState = listStates[tabForPage] ?: listStates.getOrPut(tabForPage) { LazyStaggeredGridState() }

                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(1),
                                state = pageListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 0.dp,
                                    end = 0.dp,
                                    top = topGridPadding,
                                    bottom = 140.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalItemSpacing = 4.dp
                            ) {
                                itemsIndexed(
                                    items = pageFilteredPhotos,
                                    key = { _, photo -> "${tabSortType.name}_${photo.id}" },
                                    contentType = { _, _ -> "card" }
                                ) { index, photo ->
                                    val isMost = tabSortType == com.android.snippets.viewmodel.PhotoSortType.MostSnippets && photo.snippets.isNotEmpty()
                                    val isLeast = tabSortType == com.android.snippets.viewmodel.PhotoSortType.LeastSnippets && photo.snippets.isNotEmpty()
                                    val totalCount = pageFilteredPhotos.size
                                    val cardPos = when {
                                        totalCount == 1 -> CardPosition.Single
                                        index == 0 -> CardPosition.First
                                        index == totalCount - 1 -> CardPosition.Last
                                        else -> CardPosition.Middle
                                    }
                                    val isDark = isSystemInDarkTheme()

                                    PhotoCard(
                                        photo = photo,
                                        position = cardPos,
                                        isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                                        selectionMode = viewModel.isSelectionMode,
                                        showFavoriteIcon = tabForPage != "Favorites",
                                        tab = tabForPage,
                                        isMostSnippets = isMost,
                                        isLeastSnippets = isLeast,
                                        viewModel = viewModel,
                                        isExpanded = viewModel.activePhotoId == photo.id,
                                        isAddSnippetOpen = showAddModal && modalPhoto?.id == photo.id,
                                        onClick = {
                                            if (viewModel.isSelectionMode) {
                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                viewModel.toggleSelection(photo.id)
                                            } else {
                                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                                val isExpanding = viewModel.activePhotoId != photo.id
                                                viewModel.activePhotoId = if (isExpanding) photo.id else null
                                            }
                                        },
                                        onLongClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                            if (!viewModel.isSelectionMode) viewModel.toggleSelection(photo.id)
                                        },
                                        onAddSnippet = {
                                            modalPhoto = photo
                                            showAddModal = true
                                        },
                                        onDownload = {
                                            viewModel.downloadPhotoCard(context, photo, isDark, if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                                        },
                                        onShare = {
                                            viewModel.sharePhotoCard(context, photo, isDark, if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                                        },
                                        onToggleFavorite = {
                                            viewModel.toggleFavorite(photo.id)
                                        },
                                        onNamePhoto = {
                                            modalPhoto = photo
                                            showNameModal = true
                                        },
                                        onEditLink = {
                                            modalPhoto = photo
                                            showLinkModal = true
                                        },
                                        onRatePhoto = {
                                            modalPhoto = photo
                                            showRateModal = true
                                        },
                                        onViewSnippets = {
                                            modalPhoto = photo
                                            showSnippetsModal = true
                                        },
                                        onDeletePhoto = {
                                            modalPhoto = photo
                                            showDeleteModal = true
                                        },
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } // end of HorizontalPager

                                  // Status Bar Progressive Blur Overlay when scrolling
                                  Box(
                                      modifier = Modifier
                                          .fillMaxWidth()
                                          .height(statusBarTop)
                                          .align(Alignment.TopCenter)
                                          .graphicsLayer { alpha = statusBarBlurAlpha }
                                          .then(
                                              if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                  Modifier.hazeEffect(
                                                      state = hazeState,
                                                      style = statusBarHazeStyle
                                                  ) {
                                                      progressive = HazeProgressive.verticalGradient(
                                                          startIntensity = 1f,
                                                          endIntensity = 0f
                                                      )
                                                  }
                                              } else {
                                                  Modifier.background(
                                                      Brush.verticalGradient(
                                                          colors = listOf(
                                                              MaterialTheme.colorScheme.surfaceContainer,
                                                              MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
                                                              MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f),
                                                              androidx.compose.ui.graphics.Color.Transparent
                                                          )
                                                      )
                                                  )
                                              }
                                          )
                                  )

                                 // Cookie Nav Bar stuck at the top of the surface background
                                 Box(
                                     modifier = Modifier
                                         .align(Alignment.TopStart)
                                         .fillMaxWidth()
                                         .padding(top = statusBarTop + 8.dp)
                                         .graphicsLayer {
                                             translationY = cookieNavBarOffset.toPx()
                                             alpha = cookieNavBarAlpha
                                         },
                                     contentAlignment = Alignment.CenterStart
                                 ) {
                                     Row(
                                         modifier = Modifier
                                             .wrapContentWidth()
                                             .horizontalScroll(tabRowScrollState)
                                             .padding(horizontal = 16.dp),
                                         horizontalArrangement = Arrangement.Start,
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                          val cookieNavBarItems = remember(allTabs, activeTabTarget) {
                                              allTabs.mapIndexed { tabIndex, tabName ->
                                                  val iconOrEmoji = when (tabName) {
                                                      "Library" -> CollectionsBookmarkIcon(filled = tabName == activeTabTarget)
                                                      "Favorites" -> Icons.Default.Favorite
                                                      else -> viewModel.getCollectionIcon(tabName)
                                                  }
                                                  val isSystem = tabName == "Library" || tabName == "Favorites"
                                                  val positionModifier = Modifier.onGloballyPositioned { coords ->
                                                      tabPositions[tabName] = coords.boundsInParent().left
                                                      tabWidths[tabName] = coords.size.width.toFloat()
                                                  }

                                                  cookieButtonItem(
                                                      contentDescription = tabName,
                                                      tooltip = if (isSystem) tabName else null,
                                                      modifier = positionModifier,
                                                      icon = { iconModifier, tint ->
                                                          when (iconOrEmoji) {
                                                              is androidx.compose.ui.graphics.vector.ImageVector -> Icon(iconOrEmoji, contentDescription = tabName, modifier = iconModifier.size(20.dp), tint = tint)
                                                              is Int -> Icon(painterResource(id = iconOrEmoji), contentDescription = tabName, modifier = iconModifier.size(20.dp), tint = tint)
                                                              is String -> Text(text = iconOrEmoji, fontSize = 16.sp, modifier = iconModifier)
                                                              else -> Icon(Icons.Default.Folder, contentDescription = tabName, modifier = iconModifier.size(20.dp), tint = tint)
                                                          }
                                                      }
                                                  )
                                              }
                                          }

                                          val selectedTabIndex = allTabs.indexOf(activeTabTarget).coerceAtLeast(0)

                                          CookieButtonGroup(
                                              selectedIndex = selectedTabIndex,
                                              onSelect = selectTab@ { index ->
                                                  val tabName = allTabs.getOrNull(index) ?: return@selectTab
                                                  val pageIndex = pageTabs.indexOf(tabName)
                                                  if (pageIndex != -1) {
                                                      if (tabName == currentTab) {
                                                          view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                      } else {
                                                          scope.launch {
                                                           val current = pagerState.currentPage
                                                           if (kotlin.math.abs(pageIndex - current) > 1) {
                                                               pagerState.scrollToPage(if (pageIndex > current) pageIndex - 1 else pageIndex + 1)
                                                           }
                                                           pagerState.animateScrollToPage(pageIndex)
                                                       }
                                                      }
                                                  }
                                              },
                                              items = cookieNavBarItems,
                                              cookieSize = 48.dp,
                                              overlap = 0.dp,
                                              selectedContainerColor = MaterialTheme.colorScheme.primary,
                                              selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                              unselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                              unselectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                              enableWiggle = true
                                          )
                                     }
                                 }



                // Cookie Toolbar � morphs between normal controls and inline search bar
                val isCookieToolbarVisibleState = !viewModel.isSelectionMode && isFabVisible

                BlurredSurface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 8.dp,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .safeDrawingPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                        .height(84.dp)
                        .graphicsLayer {
                            translationY = cookieToolbarOffset.toPx()
                            scaleX = cookieToolbarScale
                            scaleY = cookieToolbarScale
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                        }
                ) {
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
                        contentAlignment = Alignment.Center,
                        label = "cookie_toolbar_mode",
                        modifier = Modifier.fillMaxSize()
                    ) { searchMode ->
                        if (searchMode == 2) {
                            SelectionToolbarContent(
                                viewModel = viewModel,
                                onDeleteClick = { viewModel.showBulkDeleteModal = true },
                                isSpinning = !isAnyPopupActive,
                                isScrolled = isScrolled
                            )
                        } else if (searchMode == 1) {
                            // Search Mode
                            Row(
                                modifier = Modifier.fillMaxSize()
                                    .padding(horizontal = 16.dp),
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

                                val searchTextFieldState = rememberTextFieldState(viewModel.searchQuery)
                                LaunchedEffect(searchTextFieldState) {
                                    snapshotFlow { searchTextFieldState.text.toString() }.collect { newText ->
                                        if (viewModel.searchQuery != newText) {
                                            viewModel.searchQuery = newText
                                        }
                                    }
                                }
                                LaunchedEffect(viewModel.searchQuery) {
                                    if (searchTextFieldState.text.toString() != viewModel.searchQuery) {
                                        searchTextFieldState.setTextAndPlaceCursorAtEnd(viewModel.searchQuery)
                                    }
                                }

                                // Text field
                                BasicTextField(
                                    state = searchTextFieldState,
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(searchFocusRequester),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 17.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    onKeyboardAction = {
                                        viewModel.addRecentSearch(viewModel.searchQuery)
                                        isSearchOpen = false
                                        focusManager.clearFocus()
                                    },
                                    decorator = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (viewModel.searchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search...",
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        } else {
                            // Normal Mode
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
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                // Search
                                val isSearchActive = viewModel.searchQuery.isNotEmpty()
                                val searchIcon = if (isSearchActive && flatPhotosRaw.any { photo ->
                                    when (currentTab) {
                                        "Library" -> true
                                        "Favorites" -> photo.isFavorite
                                        else -> photo.collections.contains(currentTab)
                                    }
                                }) SearchSuccessIcon() else Icons.Default.Search
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        isSearchOpen = true
                                    },
                                    icon = searchIcon,
                                    contentDescription = "Search",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (isSearchActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                // Manage (Middle)
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        val activeTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Library"
                                        longPressedCollection = activeTab
                                    },
                                    icon = ImageVector.vectorResource(id = R.drawable.ic_folder_managed),
                                    contentDescription = "Manage",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )

                                // History
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        viewModel.showHistoryBottomSheet = true
                                    },
                                    icon = Icons.Default.History,
                                    contentDescription = "History",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    badge = {
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
                                )

                                val isFilterActive = viewModel.selectedFilterSnippets.isNotEmpty() || viewModel.showFilterSheet
                                AnimatedCookieButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        val activeTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Library"
                                        viewModel.filteringCategory = activeTab
                                        viewModel.navigateFilter()
                                    },
                                    icon = Icons.Default.FilterList,
                                    contentDescription = "Filters",
                                    isSpinning = !isAnyPopupActive,
                                    size = 48.dp,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
                        MenuBottomSheet(
                            show = showMenuPopup,
                            onDismissRequest = { showMenuPopup = false },
                            viewModel = viewModel,
                            view = view
                        )

                        HistoryBottomSheet(
                            show = viewModel.showHistoryBottomSheet,
                            onDismissRequest = { viewModel.showHistoryBottomSheet = false },
                            viewModel = viewModel,
                            view = view
                        )



                AnimatedVisibility(
                    visible = isScrolled && !viewModel.isSelectionMode && !isSearchOpen,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                           scaleOut(targetScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)),
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
                        icon = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top",
                        tooltip = "Scroll to top",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        size = 48.dp,
                        isSpinning = !isAnyPopupActive
                    )
                }


            if (longPressedCollection != null) {
                BlurredModalBottomSheet(
                    onDismissRequest = { longPressedCollection = null },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        com.android.snippets.ui.util.LocalTiltActive provides (com.android.snippets.ui.util.LocalTiltActive.current && (deletingCollection == null))
                    ) {
                        Motion.ExpressiveSheetContent {
                            val managePagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

                            var activePillInfo by remember { mutableStateOf<SortPillInfo?>(null) }
                            var displayedPillInfo by remember { mutableStateOf<SortPillInfo?>(null) }
                            var pillInfoJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

                            fun showPillInfo(name: String, icon: @Composable () -> Unit) {
                                pillInfoJob?.cancel()
                                val info = SortPillInfo(name, icon, System.currentTimeMillis())
                                displayedPillInfo = info
                                activePillInfo = info
                                pillInfoJob = scope.launch {
                                    kotlinx.coroutines.delay(3500)
                                    activePillInfo = null
                                }
                            }

                            LaunchedEffect(managePagerState.currentPage) {
                                pillInfoJob?.cancel()
                                activePillInfo = null
                            }

                            val collection = longPressedCollection ?: ""
                            val iconOrEmoji = when (collection) {
                                "Library" -> CollectionsBookmarkIcon(filled = true)
                                "Favorites" -> Icons.Default.Favorite
                                else -> viewModel.getCollectionIcon(collection)
                            }

                            val actions = remember(longPressedCollection) {
                                when (longPressedCollection) {
                                    "Library" -> listOf("ADD")
                                    "Favorites" -> listOf("ADD", "REMOVE")
                                    else -> listOf("ADD", "REMOVE", "EDIT_EMOJI", "DELETE")
                                }
                            }

                            val actionItems = remember(actions) {
                                actions.map { action ->
                                    when (action) {
                                        "ADD" -> cookieButtonItem(
                                            icon = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Add photos",
                                            tooltip = "Add photos",
                                            iconSize = 28.dp
                                        )
                                        "REMOVE" -> cookieButtonItem(
                                            icon = Icons.Default.Remove,
                                            contentDescription = "Remove photos",
                                            tooltip = "Remove photos",
                                            iconSize = 28.dp
                                        )
                                        "EDIT_EMOJI" -> cookieButtonItem(
                                            icon = Icons.Default.AddReaction,
                                            contentDescription = "Edit emoji",
                                            tooltip = "Edit emoji",
                                            iconSize = 28.dp
                                        )
                                        "DELETE" -> cookieButtonItem(
                                            icon = R.drawable.ic_delete,
                                            contentDescription = "Delete",
                                            tooltip = "Delete",
                                            iconSize = 28.dp
                                        )
                                        else -> cookieButtonItem(Icons.Default.MoreHoriz)
                                    }
                                }
                            }

                            var selectedActionIndex by remember { mutableIntStateOf(-1) }
                            var actionJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

                            val currentSortType = viewModel.getPhotoSortTypeFor(longPressedCollection ?: currentTab)

                            val sortOptions = remember {
                                listOf(
                                    com.android.snippets.viewmodel.PhotoSortType.DateNewest,
                                    com.android.snippets.viewmodel.PhotoSortType.DateOldest,
                                    com.android.snippets.viewmodel.PhotoSortType.MostSnippets,
                                    com.android.snippets.viewmodel.PhotoSortType.MostStarred,
                                    com.android.snippets.viewmodel.PhotoSortType.LeastSnippets,
                                    com.android.snippets.viewmodel.PhotoSortType.LeastStarred
                                )
                            }
                            val sortItems = remember(sortOptions) {
                                listOf(
                                    cookieButtonItem(Icons.Default.ArrowDownward, contentDescription = "Newest first", tooltip = "Newest first", iconSize = 22.dp),
                                    cookieButtonItem(Icons.Default.ArrowUpward, contentDescription = "Oldest first", tooltip = "Oldest first", iconSize = 22.dp),
                                    cookieButtonItem(Icons.AutoMirrored.Filled.TextSnippet, contentDescription = "Most snippets", tooltip = "Most snippets", iconSize = 22.dp),
                                    cookieButtonItem(R.drawable.ic_star_rating, contentDescription = "Most starred", tooltip = "Most starred", iconSize = 22.dp),
                                    cookieButtonItem(Icons.AutoMirrored.Filled.TextSnippet, contentDescription = "Least snippets", tooltip = "Least snippets", iconSize = 22.dp),
                                    cookieButtonItem(R.drawable.ic_star_rating, contentDescription = "Least starred", tooltip = "Least starred", iconSize = 22.dp)
                                )
                            }
                            val selectedSortIndex = sortOptions.indexOf(currentSortType)


                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 154.dp)
                                    .padding(top = 8.dp, bottom = 20.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Collection icon with pagination dots below it
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Top,
                                        modifier = Modifier.width(48.dp)
                                    ) {
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Surface(
                                            shape = CookieShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                when (iconOrEmoji) {
                                                    is androidx.compose.ui.graphics.vector.ImageVector -> {
                                                        Icon(iconOrEmoji, contentDescription = null, modifier = Modifier.size(24.dp))
                                                    }
                                                    is String -> {
                                                        Text(text = iconOrEmoji, fontSize = 20.sp)
                                                    }
                                                    else -> {
                                                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(24.dp))
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(26.dp))

                                        // Spring physics M3 pagination dots
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            repeat(2) { index ->
                                                val isSelected = (if (managePagerState.isScrollInProgress) managePagerState.targetPage else managePagerState.currentPage) == index
                                                val animatedWidth by animateDpAsState(
                                                    targetValue = if (isSelected) 18.dp else 6.dp,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    ),
                                                    label = "manage_dot_width_$index"
                                                )
                                                val primaryAlpha by animateFloatAsState(
                                                    targetValue = if (isSelected) 1f else 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    ),
                                                    label = "manage_dot_primary_alpha_$index"
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .height(6.dp)
                                                        .width(animatedWidth)
                                                        .clip(CircleShape)
                                                        .clickable {
                                                            if (!isSelected) {
                                                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                                scope.launch {
                                                                    managePagerState.animateScrollToPage(
                                                                        page = index,
                                                                        animationSpec = spring(
                                                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                                                            stiffness = Spring.StiffnessLow
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                        }
                                                ) {
                                                    // Outline variant (bottom layer):
                                                    // Retains secondary container color with M3 outline variant border.
                                                    // Kept at full opacity so it never fades out when turning primary.
                                                    Box(
                                                        modifier = Modifier
                                                            .matchParentSize()
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                                            .border(
                                                                width = 1.dp,
                                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                                shape = CircleShape
                                                            )
                                                    )
                                                    // Primary layer: drawn OVER outline variant so outline is not visible when primary
                                                    if (primaryAlpha > 0.001f) {
                                                        val primaryColor = MaterialTheme.colorScheme.primary
                                                        Box(
                                                            modifier = Modifier
                                                                .matchParentSize()
                                                                .graphicsLayer { alpha = primaryAlpha }
                                                                .border(1.dp, primaryColor, CircleShape)
                                                                .clip(CircleShape)
                                                                .background(primaryColor)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Action pill composable rendered on each page
                                    val actionPillContent: @Composable () -> Unit = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(36.dp)
                                                .padding(top = 4.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = activePillInfo != null,
                                                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                                                    initialOffsetY = { -it / 2 },
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                ),
                                                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
                                                    targetOffsetY = { -it / 2 },
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                )
                                            ) {
                                                displayedPillInfo?.let { currentInfo ->
                                                    Surface(
                                                        modifier = Modifier.height(32.dp),
                                                        shape = CircleShape,
                                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                        border = null
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Start
                                                        ) {
                                                            currentInfo.icon()
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = currentInfo.text,
                                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                                    fontWeight = FontWeight.Medium,
                                                                    fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide
                                                                ),
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                fontSize = 12.sp,
                                                                maxLines = 1,
                                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Horizontal pager next to collection icon spanning button row and action pill row
                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(126.dp)
                                    ) {
                                        val availableWidth = maxWidth
                                        val actionCookieSize = remember(availableWidth, actionItems.size) {
                                            availableWidth / actionItems.size.coerceAtLeast(4)
                                        }
                                        val sortCookieSize = remember(availableWidth, sortItems.size) {
                                            availableWidth / sortItems.size.coerceAtLeast(1)
                                        }

                                        HorizontalPager(
                                            state = managePagerState,
                                            beyondViewportPageCount = 2,
                                            pageSpacing = 16.dp,
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.Top
                                        ) { page ->
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.Top
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(86.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                when (page) {
                                                    0 -> {
                                                        // Page 1: Actions
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Start
                                                        ) {
                                                            CookieButtonGroup(
                                                                selectedIndex = selectedActionIndex,
                                                                onSelect = selectAction@ { index ->
                                                                    val action = actions.getOrNull(index) ?: return@selectAction
                                                                    selectedActionIndex = index
                                                                    when (action) {
                                                                        "ADD" -> showPillInfo("Add photos") {
                                                                            Icon(
                                                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        "REMOVE" -> showPillInfo("Remove photos") {
                                                                            Icon(
                                                                                imageVector = Icons.Default.Remove,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        "EDIT_EMOJI" -> showPillInfo("Edit emoji") {
                                                                            Icon(
                                                                                imageVector = Icons.Default.AddReaction,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        "DELETE" -> showPillInfo("Delete") {
                                                                            Icon(
                                                                                painter = painterResource(id = R.drawable.ic_delete),
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                    }
                                                                    actionJob?.cancel()
                                                                    actionJob = scope.launch {
                                                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                        kotlinx.coroutines.delay(350)
                                                                        when (action) {
                                                                            "ADD" -> {
                                                                                onAddPhotos(longPressedCollection!!)
                                                                                longPressedCollection = null
                                                                            }
                                                                            "REMOVE" -> {
                                                                                if (longPressedCollection == "Library") {
                                                                                    viewModel.clearSelection()
                                                                                    viewModel.forceSelectionMode = true
                                                                                } else {
                                                                                    viewModel.startCollectionRemoval(longPressedCollection!!)
                                                                                }
                                                                                longPressedCollection = null
                                                                            }
                                                                            "EDIT_EMOJI" -> {
                                                                                viewModel.navigateEmojiPicker(longPressedCollection!!)
                                                                                longPressedCollection = null
                                                                            }
                                                                            "DELETE" -> {
                                                                                if (longPressedCollection != "Library") {
                                                                                    deletingCollection = longPressedCollection
                                                                                } else {
                                                                                    viewModel.showSnackbar("Cannot delete Library collection")
                                                                                }
                                                                                longPressedCollection = null
                                                                            }
                                                                        }
                                                                        selectedActionIndex = -1
                                                                    }
                                                                },
                                                                onItemClick = clickAction@ { index ->
                                                                    val action = actions.getOrNull(index) ?: return@clickAction
                                                                    when (action) {
                                                                        "ADD" -> showPillInfo("Add photos") {
                                                                            Icon(
                                                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        "REMOVE" -> showPillInfo("Remove photos") {
                                                                            Icon(
                                                                                imageVector = Icons.Default.Remove,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        "EDIT_EMOJI" -> showPillInfo("Edit emoji") {
                                                                            Icon(
                                                                                imageVector = Icons.Default.AddReaction,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        "DELETE" -> showPillInfo("Delete") {
                                                                            Icon(
                                                                                painter = painterResource(id = R.drawable.ic_delete),
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                    }
                                                                },
                                                                items = actionItems,
                                                                columns = 1,
                                                                cookieSize = actionCookieSize,
                                                                overlap = 0.dp,
                                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                                                unselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                                unselectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                enableWiggle = false
                                                            )
                                                        }
                                                    }
                                                    1 -> {
                                                        // Page 2: Sort items
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.Start,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            CookieButtonGroup(
                                                                selectedIndex = selectedSortIndex,
                                                                onSelect = selectSort@ { index ->
                                                                    val option = sortOptions.getOrNull(index) ?: return@selectSort
                                                                    val col = longPressedCollection ?: currentTab
                                                                    viewModel.activePhotoId = null
                                                                    if (currentSortType != option) {
                                                                        viewModel.setPhotoSortTypeFor(col, option)
                                                                    }
                                                                    listStates[col]?.let { state ->
                                                                        scope.launch { state.scrollToItem(0) }
                                                                    }
                                                                },
                                                                onItemClick = { index ->
                                                                    when (index) {
                                                                        0 -> showPillInfo("Newest first") {
                                                                            Icon(
                                                                                imageVector = Icons.Default.ArrowDownward,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        1 -> showPillInfo("Oldest first") {
                                                                            Icon(
                                                                                imageVector = Icons.Default.ArrowUpward,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        2 -> showPillInfo("Most snippets") {
                                                                            Icon(
                                                                                imageVector = Icons.AutoMirrored.Filled.TextSnippet,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        3 -> showPillInfo("Most starred") {
                                                                            Icon(
                                                                                painter = painterResource(id = R.drawable.ic_star_rating),
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        4 -> showPillInfo("Least snippets") {
                                                                            Icon(
                                                                                imageVector = Icons.AutoMirrored.Filled.TextSnippet,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                        5 -> showPillInfo("Least starred") {
                                                                            Icon(
                                                                                painter = painterResource(id = R.drawable.ic_star_rating),
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(16.dp),
                                                                                tint = MaterialTheme.colorScheme.primary
                                                                            )
                                                                        }
                                                                    }
                                                                },
                                                                items = sortItems,
                                                                columns = 1,
                                                                cookieSize = sortCookieSize,
                                                                overlap = 0.dp,
                                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                                                unselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                                unselectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                enableWiggle = true
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            actionPillContent()
                                        }
                                    }
                                }
                                }
                            }
                        }
                    }
                }
            }
                deletingCollection?.let { collection ->
                    BlurredAlertDialog(
                        onDismissRequest = { deletingCollection = null },
                        onConfirm = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.deleteCollection(collection)
                            if (currentTab == collection) {
                                scope.launch { pagerState.animateScrollToPage(0) }
                            }
                            deletingCollection = null
                        },
                        title = {
                            Text(
                                text = "Delete collection?",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.5).sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        text = {
                            Text(
                                text = "Are you sure you want to delete this collection? Your photos will be saved.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                val currentModalPhoto = modalPhoto?.let { p -> viewModel.photos.find { it.id == p.id } ?: p }
                if (currentModalPhoto != null) {
                    if (showAddModal) {
                        AddSnippetsModal(
                            photo = currentModalPhoto,
                            onAdd = { name -> viewModel.updateSnippets(currentModalPhoto.id, name) },
                            onClose = { showAddModal = false },
                            viewModel = viewModel
                        )
                    }
                    if (showSnippetsModal) {
                        SnippetsModal(
                            photo = currentModalPhoto,
                            onRemove = { snippet -> viewModel.removeSnippet(currentModalPhoto.id, snippet) },
                            onClose = { showSnippetsModal = false },
                            viewModel = viewModel
                        )
                    }
                    if (showDeleteModal) {
                        DeleteConfirmationModal(
                            count = 1,
                            hasPublicPhotos = currentModalPhoto.isPublic,
                            onDismiss = { showDeleteModal = false },
                            onConfirm = { unpublish ->
                                viewModel.deletePhoto(currentModalPhoto.id, unpublish = unpublish)
                                if (viewModel.activePhotoId == currentModalPhoto.id) {
                                    viewModel.activePhotoId = null
                                }
                                showDeleteModal = false
                            }
                        )
                    }
                    if (showRateModal) {
                        RateFoodDialog(
                            initialRating = currentModalPhoto.rating,
                            onDismiss = { showRateModal = false },
                            onConfirm = { rating ->
                                viewModel.setPhotoRating(currentModalPhoto.id, rating)
                                showRateModal = false
                            }
                        )
                    }
                    if (showLinkModal) {
                        LocationLinkModal(
                            initialLink = currentModalPhoto.locationLink,
                            suggestions = viewModel.allUniqueLocations,
                            onDismiss = { showLinkModal = false },
                            onSave = { link, name ->
                                viewModel.updateLocationLink(currentModalPhoto.id, link.takeIf { it.isNotBlank() }, name)
                                showLinkModal = false
                            }
                        )
                    }
                    if (showNameModal) {
                        NamePhotoModal(
                            initialName = currentModalPhoto.name,
                            onDismiss = { showNameModal = false },
                            onSave = { name ->
                                viewModel.updatePhotoName(currentModalPhoto.id, name)
                                showNameModal = false
                            }
                        )
                    }
                }
            }
        }
    }
}
}






}





private data class SortPillInfo(
    val text: String,
    val icon: @Composable () -> Unit,
    val timestamp: Long = System.currentTimeMillis()
)

private suspend fun LazyStaggeredGridState.smoothScrollToItemSlow(
    targetIndex: Int,
    targetOffsetPx: Int = 24,
    durationMillis: Int = 700
) {
    val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == targetIndex }
    if (visibleItem != null) {
        val delta = (visibleItem.offset.y - targetOffsetPx).toFloat()
        if (kotlin.math.abs(delta) > 4f) {
            animateScrollBy(
                value = delta,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = FastOutSlowInEasing
                )
            )
        }
    } else {
        animateScrollToItem(targetIndex, scrollOffset = -targetOffsetPx)
    }
}


