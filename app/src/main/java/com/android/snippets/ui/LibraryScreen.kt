package com.android.snippets.ui
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.boundsInParent

import com.android.snippets.ui.components.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection


import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.geometry.Offset

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import com.android.snippets.ui.shapes.LocalAppShape
import com.android.snippets.ui.shapes.LocalAppShapeType
import com.android.snippets.ui.shapes.AppShape
import com.android.snippets.viewmodel.Screen
import com.android.snippets.model.Photo
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
    windowSizeClass: WindowSizeClass?,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    isDetailTransitioning: Boolean = false,
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

    val isBackHandlerEnabled = isSearchOpen || viewModel.isSelectionMode || showHistoryBottomSheet || showMenuPopup || showCollectionsPopup
    AppPredictiveBackHandler(enabled = isBackHandlerEnabled) {
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
    
    var showFoodRandomizerBottomSheet by remember { mutableStateOf(false) }
    val isAnyPopupActive = viewModel.showBulkAddToCollectionDialog || viewModel.showBulkDeleteModal || viewModel.showCreateDialog || showMenuPopup || showCollectionsPopup || isSearchOpen || showHistoryBottomSheet || showFoodRandomizerBottomSheet
    val allowMemorySpin = !isAnyPopupActive
    val curated = viewModel.curatedMemories
    
    var longPressedCollection by remember { mutableStateOf<String?>(null) }
    var renamingCollection by remember { mutableStateOf<String?>(null) }
    var deletingCollection by remember { mutableStateOf<String?>(null) }
    var draggedCollection by remember { mutableStateOf<String?>(null) }
    var photoToDeleteFromEatlist by remember { mutableStateOf<Photo?>(null) }
    val dragOffsetAnim = remember { androidx.compose.animation.core.Animatable(0f) }
    
    val pageTabs = remember(viewModel.userCollections, viewModel.showEatlist) {
        if (viewModel.showEatlist) {
            listOf("Eatlist", "Library", "Favorites") + viewModel.userCollections
        } else {
            listOf("Library", "Favorites") + viewModel.userCollections
        }
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
                                "Library" -> !photo.collections.contains("Eatlist")
                "Favorites" -> photo.isFavorite
                "Eatlist" -> photo.collections.contains("Eatlist")
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
        targetValue = if (isFabVisible) 0.dp else (-160).dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "topbar_offset"
    )

    val toolbarOffset by animateDpAsState(
        targetValue = if (viewModel.isSelectionMode || isFabVisible) 0.dp else 200.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "toolbar_offset"
    )

    val currentGridState = remember(currentTab) {
        listStates.getOrPut(currentTab) { LazyStaggeredGridState() }
    }

    val isScrolled by remember(currentGridState) {
        derivedStateOf {
            currentGridState.firstVisibleItemIndex > 0 || currentGridState.firstVisibleItemScrollOffset > 0
        }
    }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

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
                    val topGridPadding = statusBarTop + 88.dp

                    val statusBarBrushAlpha by animateFloatAsState(
                        targetValue = if (isScrolled) 1f else 0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "status_bar_brush_alpha"
                    )

                    val allTabs = pageTabs
                    val tabRowScrollState = rememberScrollState()
                    val tabPositions = remember { mutableStateMapOf<String, Float>() }
                    val tabWidths = remember { mutableStateMapOf<String, Float>() }
                    LaunchedEffect(currentTab) {
                        val tabX = tabPositions[currentTab] ?: return@LaunchedEffect
                        val tabW = tabWidths[currentTab] ?: return@LaunchedEffect
                        val rowW = tabRowScrollState.maxValue + 0f
                        val target = (tabX - (tabRowScrollState.viewportSize / 2f) + (tabW / 2f))
                            .coerceIn(0f, rowW)
                        tabRowScrollState.animateScrollTo(target.toInt())
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 16.dp
                    ) { page ->
                        val tabForPage = pageTabs.getOrNull(page) ?: "Library"
                        val tabSortType = viewModel.getPhotoSortTypeFor(tabForPage)
                        val pageFilteredPhotos = remember(flatPhotosRaw, tabForPage, tabSortType) {
                            val filtered = flatPhotosRaw.filter { photo ->
                                when (tabForPage) {
                                    "Library" -> !photo.collections.contains("Eatlist")
                                    "Favorites" -> photo.isFavorite
                                    "Eatlist" -> photo.collections.contains("Eatlist")
                                    else -> photo.collections.contains(tabForPage)
                                }
                            }
                            viewModel.sortPhotos(filtered, tabSortType)
                        }

                        if (pageFilteredPhotos.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = topGridPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                when (tabForPage) {
                                    "Favorites" -> EmptyFavoritesState()
                                    "Eatlist"   -> EmptyEatlistState()
                                    "Library"   -> EmptyLibraryState()
                                    else        -> EmptyCollectionState()
                                }
                            }
                        } else {
                            val pageListState = listStates.getOrPut(tabForPage) { LazyStaggeredGridState() }
                            val currentGridColumns = viewModel.customGridColumns?.coerceIn(-1, 3) ?: when (windowSizeClass?.widthSizeClass) {
                                WindowWidthSizeClass.Expanded -> 3
                                WindowWidthSizeClass.Medium -> 3
                                else -> -1
                            }
                            val displayColumns = if (currentGridColumns <= 0) 1 else currentGridColumns
                            val minColumns = -1
                            val maxColumns = 3

                            // Calculate max and min snippets counts once for the whole page/list:
                            val nonZeroSnippetCounts = remember(pageFilteredPhotos) {
                                pageFilteredPhotos.map { it.snippets.size }.filter { it > 0 }
                            }
                            val maxSnippets = remember(nonZeroSnippetCounts) {
                                nonZeroSnippetCounts.maxOrNull() ?: 0
                            }
                            val minSnippets = remember(nonZeroSnippetCounts) {
                                nonZeroSnippetCounts.minOrNull() ?: 0
                            }

                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(displayColumns),
                                state = pageListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(currentGridColumns) {
                                        awaitEachGesture {
                                            awaitFirstDown(requireUnconsumed = false)
                                            var zoomAccumulator = 1f
                                            do {
                                                val event = awaitPointerEvent()
                                                val numPointers = event.changes.filter { it.pressed }.size
                                                if (numPointers >= 2) {
                                                    val zoomChange = event.calculateZoom()
                                                    if (zoomChange != 1f) {
                                                        zoomAccumulator *= zoomChange
                                                        event.changes.forEach { it.consume() }
                                                        if (zoomAccumulator > 1.25f) {
                                                            // Pinch out (expanding fingers) -> fewer columns -> larger items
                                                            if (currentGridColumns > minColumns) {
                                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                viewModel.updateCustomGridColumns(currentGridColumns - 1)
                                                            }
                                                            zoomAccumulator = 1f
                                                        } else if (zoomAccumulator < 0.8f) {
                                                            // Pinch in (contracting fingers) -> more columns -> smaller items
                                                            if (currentGridColumns < maxColumns) {
                                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                viewModel.updateCustomGridColumns(currentGridColumns + 1)
                                                            }
                                                            zoomAccumulator = 1f
                                                        }
                                                    }
                                                }
                                            } while (event.changes.any { it.pressed })
                                        }
                                    },
                                contentPadding = PaddingValues(
                                    start = 0.dp,
                                    end = 0.dp,
                                    top = topGridPadding,
                                    bottom = 100.dp
                                ),
                                              horizontalArrangement = Arrangement.spacedBy(4.dp),
                                              verticalItemSpacing = 4.dp
                                          ) {
                                              itemsIndexed(pageFilteredPhotos, key = { _, photo -> photo.id }) { index, photo ->
                                                   val isMost = tabSortType == com.android.snippets.viewmodel.PhotoSortType.MostSnippets && photo.snippets.isNotEmpty()
                                                   val isLeast = tabSortType == com.android.snippets.viewmodel.PhotoSortType.LeastSnippets && photo.snippets.isNotEmpty()
                                                  
                                                  when (currentGridColumns) {
                                                      -1 -> {
                                                          // Calculate card position: First, Middle, Last, or Single
                                                          val totalCount = pageFilteredPhotos.size
                                                          val cardPos = when {
                                                              totalCount == 1 -> CardPosition.Single
                                                              index == 0 -> CardPosition.First
                                                              index == totalCount - 1 -> CardPosition.Last
                                                              else -> CardPosition.Middle
                                                          }

                                                          PhotoCardListItem(
                                                              photo = photo,
                                                              position = cardPos,
                                                              isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                                                              selectionMode = viewModel.isSelectionMode,
                                                              showFavoriteIcon = tabForPage != "Favorites",
                                                              tab = tabForPage,
                                                              isMostSnippets = isMost,
                                                              isLeastSnippets = isLeast,
                                                              viewModel = viewModel,
                                                              onClick = {
                                                                  if (viewModel.isSelectionMode) {
                                                                      view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                      viewModel.toggleSelection(photo.id)
                                                                  } else {
                                                                      view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                                                      if (windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded) {
                                                                          viewModel.activePhotoId = photo.id
                                                                      } else {
                                                                          viewModel.openDetail(photo.id, overrideReturnScreen = Screen.Library)
                                                                      }
                                                                  }
                                                              },
                                                              onLongClick = {
                                                                  view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                                  if (!viewModel.isSelectionMode) viewModel.toggleSelection(photo.id)
                                                              },
                                                              onEatlistCheckClick = {
                                                                  photoToDeleteFromEatlist = photo
                                                              },
                                                              sharedTransitionScope = sharedTransitionScope,
                                                              animatedVisibilityScope = animatedVisibilityScope,
                                                              modifier = Modifier.fillMaxWidth().alpha(if ((viewModel.currentScreen == Screen.Detail || isDetailTransitioning) && viewModel.activePhotoId == photo.id) 0f else 1f)
                                                          )
                                                      }
                                                      0 -> {
                                                          PhotoListItem(
                                                              photo = photo,
                                                              isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                                                              selectionMode = viewModel.isSelectionMode,
                                                              showFavoriteIcon = tabForPage != "Favorites",
                                                              tab = tabForPage,
                                                              isMostSnippets = isMost,
                                                              isLeastSnippets = isLeast,
                                                              viewModel = viewModel,
                                                              onClick = {
                                                                  if (viewModel.isSelectionMode) {
                                                                      view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                      viewModel.toggleSelection(photo.id)
                                                                  } else {
                                                                      view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                                                      if (windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded) {
                                                                          viewModel.activePhotoId = photo.id
                                                                      } else {
                                                                          viewModel.openDetail(photo.id, overrideReturnScreen = Screen.Library)
                                                                      }
                                                                  }
                                                              },
                                                              onLongClick = {
                                                                  view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                                  if (!viewModel.isSelectionMode) viewModel.toggleSelection(photo.id)
                                                              },
                                                              onEatlistCheckClick = {
                                                                  photoToDeleteFromEatlist = photo
                                                              },
                                                              shape = RoundedCornerShape(12.dp),
                                                              sharedTransitionScope = sharedTransitionScope,
                                                              animatedVisibilityScope = animatedVisibilityScope,
                                                              modifier = Modifier.fillMaxWidth().alpha(if ((viewModel.currentScreen == Screen.Detail || isDetailTransitioning) && viewModel.activePhotoId == photo.id) 0f else 1f)
                                                          )
                                                      }
                                                      else -> {
                                                          PhotoMasonryItem(
                                                              photo = photo,
                                                              isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                                                              selectionMode = viewModel.isSelectionMode,
                                                              showFavoriteIcon = tabForPage != "Favorites",
                                                              matchingSnippetsCount = getMatchingSnippetsCount(photo, viewModel),
                                                              isMostSnippets = isMost,
                                                              isLeastSnippets = isLeast,
                                                              sharedTransitionScope = sharedTransitionScope,
                                                              animatedVisibilityScope = animatedVisibilityScope,
                                                              shape = if (viewModel.makePhotosFollowShape) LocalAppShape.current else RoundedCornerShape(0.dp),
                                                              tab = tabForPage,
                                                              onClick = {
                                                                  if (viewModel.isSelectionMode) {
                                                                      view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                      viewModel.toggleSelection(photo.id)
                                                                  } else {
                                                                      view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                                                      if (windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded) {
                                                                          viewModel.activePhotoId = photo.id
                                                                      } else {
                                                                          viewModel.openDetail(photo.id, overrideReturnScreen = Screen.Library)
                                                                      }
                                                                  }
                                                              },
                                                              onLongClick = {
                                                                  view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                                  if (!viewModel.isSelectionMode) viewModel.toggleSelection(photo.id)
                                                              },
                                                              onEatlistCheckClick = {
                                                                  photoToDeleteFromEatlist = photo
                                                              },
                                                              fillCard = false,
                                                              modifier = Modifier.fillMaxWidth().alpha(if ((viewModel.currentScreen == Screen.Detail || isDetailTransitioning) && viewModel.activePhotoId == photo.id) 0f else 1f)
                                                          )
                                                      }
                                                  }
                                              }
                                          }
                                     }
                                 } // end of HorizontalPager

                                  // Status Bar Brush Overlay when scrolling
                                  Box(
                                      modifier = Modifier
                                          .fillMaxWidth()
                                          .height(statusBarTop + 80.dp)
                                          .align(Alignment.TopCenter)
                                          .graphicsLayer { alpha = statusBarBrushAlpha }
                                          .background(
                                              Brush.verticalGradient(
                                                  colors = listOf(
                                                      MaterialTheme.colorScheme.surfaceContainer,
                                                      MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                                                      MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f),
                                                      androidx.compose.ui.graphics.Color.Transparent
                                                  )
                                              )
                                          )
                                  )

                                 // Floating Tab Bar (Pill Shape)
                                 Surface(
                                     shape = CircleShape,
                                     color = MaterialTheme.colorScheme.surface,
                                     shadowElevation = 0.dp,
                                     tonalElevation = 0.dp,
                                     modifier = Modifier
                                         .align(Alignment.TopCenter)
                                         .padding(top = statusBarTop + 8.dp)
                                         .padding(start = 16.dp, end = 16.dp)
                                         .widthIn(max = 500.dp)
                                         .fillMaxWidth()
                                         .height(64.dp)
                                         .graphicsLayer { translationY = topBarOffset.toPx() }
                                 ) {
                                     Row(
                                         modifier = Modifier
                                             .fillMaxSize()
                                             .horizontalScroll(tabRowScrollState)
                                             .padding(horizontal = 8.dp, vertical = 6.dp),
                                         horizontalArrangement = Arrangement.Center,
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                          androidx.compose.material3.ButtonGroup(
                                              horizontalArrangement = Arrangement.spacedBy(androidx.compose.material3.ButtonGroupDefaults.ConnectedSpaceBetween),
                                              overflowIndicator = {}
                                          ) {
                                              allTabs.forEachIndexed { tabIndex, tabName ->
                                                  val isSelected = tabName == currentTab
                                                  val iconOrEmoji = when (tabName) {
                                                      "Library" -> Icons.Default.PhotoLibrary
                                                      "Favorites" -> Icons.Default.Favorite
                                                      "Eatlist" -> com.ln.android.snippets.R.drawable.ic_eatlist
                                                      else -> viewModel.getCollectionIcon(tabName)
                                                  }
                                                  val isSystem = tabName == "Library" || tabName == "Favorites" || tabName == "Eatlist"
                                                  val customIndex = viewModel.userCollections.indexOf(tabName)

                                                  customItem(
                                                      buttonGroupContent = {
                                                          key(tabName) {
                                                              val isDragged = draggedCollection == tabName
                                                              val density = androidx.compose.ui.platform.LocalDensity.current
                                                              val positionModifier = Modifier.onGloballyPositioned { coords ->
                                                                  tabPositions[tabName] = coords.boundsInParent().left
                                                                  tabWidths[tabName] = coords.size.width.toFloat()
                                                              }
                                                              val itemWidthPx = remember { with(density) { 120.dp.toPx() } }

                                                              val dragModifier = if (!isSystem) {
                                                                  Modifier
                                                                      .pointerInput(tabName) {
                                                                          detectDragGesturesAfterLongPress(
                                                                              onDragStart = {
                                                                                  view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                                                  draggedCollection = tabName
                                                                                  scope.launch { dragOffsetAnim.snapTo(0f) }
                                                                              },
                                                                              onDrag = { change, dragAmount ->
                                                                                  change.consume()
                                                                                  scope.launch { dragOffsetAnim.snapTo(dragOffsetAnim.value + dragAmount.x) }
                                                                                  
                                                                                  val index = viewModel.userCollections.indexOf(tabName)
                                                                                  if (index != -1) {
                                                                                      if (dragOffsetAnim.value > itemWidthPx && index < viewModel.userCollections.size - 1) {
                                                                                          view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                                          viewModel.moveCollectionRight(tabName)
                                                                                          scope.launch {
                                                                                              dragOffsetAnim.animateTo(
                                                                                                  dragOffsetAnim.value - itemWidthPx,
                                                                                                  androidx.compose.animation.core.spring(
                                                                                                      dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                                                                                      stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                                                                                                  )
                                                                                              )
                                                                                          }
                                                                                      } else if (dragOffsetAnim.value < -itemWidthPx && index > 0) {
                                                                                          view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                                          viewModel.moveCollectionLeft(tabName)
                                                                                          scope.launch {
                                                                                              dragOffsetAnim.animateTo(
                                                                                                  dragOffsetAnim.value + itemWidthPx,
                                                                                                  androidx.compose.animation.core.spring(
                                                                                                      dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                                                                                      stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                                                                                                  )
                                                                                              )
                                                                                          }
                                                                                      }
                                                                                  }
                                                                              },
                                                                              onDragEnd = {
                                                                                  scope.launch {
                                                                                      dragOffsetAnim.animateTo(
                                                                                          0f,
                                                                                          androidx.compose.animation.core.spring(
                                                                                              dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                                                                              stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                                                                          )
                                                                                      )
                                                                                      draggedCollection = null
                                                                                  }
                                                                              },
                                                                              onDragCancel = {
                                                                                  scope.launch {
                                                                                      dragOffsetAnim.snapTo(0f)
                                                                                      draggedCollection = null
                                                                                  }
                                                                              }
                                                                          )
                                                                      }
                                                                      .graphicsLayer {
                                                                          val currentlyDragged = draggedCollection == tabName
                                                                          translationX = if (currentlyDragged) dragOffsetAnim.value else 0f
                                                                          scaleX = if (currentlyDragged) 1.08f else 1f
                                                                          scaleY = if (currentlyDragged) 1.08f else 1f
                                                                          alpha = if (currentlyDragged) 0.85f else 1f
                                                                          shadowElevation = 0f
                                                                      }
                                                              } else {
                                                                  Modifier
                                                              }

                                                              val interactionSource = remember { MutableInteractionSource() }
                                                              val isPressed by interactionSource.collectIsPressedAsState()

                                                              // Calculate target width for spring animation & press compression
                                                              val targetWidth = when {
                                                                  isSelected && isPressed -> 94.dp
                                                                  isSelected -> 116.dp
                                                                  isPressed -> 64.dp
                                                                  else -> 78.dp
                                                              }

                                                              val animatedWidth by animateDpAsState(
                                                                  targetValue = targetWidth,
                                                                  animationSpec = androidx.compose.animation.core.spring(
                                                                      dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                                                                      stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                                                                  ),
                                                                  label = "tabWidth_$tabName"
                                                              )

                                                              val shapes = when {
                                                                  allTabs.size == 1 -> androidx.compose.material3.ButtonGroupDefaults.connectedLeadingButtonShapes()
                                                                  tabIndex == 0 -> androidx.compose.material3.ButtonGroupDefaults.connectedLeadingButtonShapes()
                                                                  tabIndex == allTabs.size - 1 -> androidx.compose.material3.ButtonGroupDefaults.connectedTrailingButtonShapes()
                                                                  else -> androidx.compose.material3.ButtonGroupDefaults.connectedMiddleButtonShapes()
                                                              }

                                                              ToggleButton(
                                                                  checked = isSelected,
                                                                  onCheckedChange = { checked ->
                                                                      val pageIndex = pageTabs.indexOf(tabName)
                                                                      if (pageIndex != -1) {
                                                                          if (isSelected) {
                                                                              view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                          } else {
                                                                              scope.launch { pagerState.animateScrollToPage(pageIndex) }
                                                                          }
                                                                      }
                                                                  },
                                                                  interactionSource = interactionSource,
                                                                  shapes = shapes,
                                                                  colors = ToggleButtonDefaults.toggleButtonColors(
                                                                      checkedContainerColor = MaterialTheme.colorScheme.primary,
                                                                      checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                                                                      containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                                      contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                                  ),
                                                                  modifier = dragModifier.then(positionModifier).height(48.dp).width(animatedWidth)
                                                              ) {
                                                                 Row(
                                                                     verticalAlignment = Alignment.CenterVertically,
                                                                     horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                 ) {
                                                                     Box(
                                                                         contentAlignment = Alignment.Center,
                                                                         modifier = Modifier.size(20.dp)
                                                                     ) {
                                                                         if (iconOrEmoji is androidx.compose.ui.graphics.vector.ImageVector) {
                                                                             Icon(iconOrEmoji, contentDescription = null, modifier = Modifier.size(16.dp))
                                                                         } else if (iconOrEmoji is String) {
                                                                             Text(text = iconOrEmoji, fontSize = 14.sp)
                                                                         } else if (iconOrEmoji is Int) {
                                                                             Icon(painterResource(id = iconOrEmoji), contentDescription = null, modifier = Modifier.size(16.dp))
                                                                         }
                                                                     }
                                                                     
                                                                     if (isSelected) {
                                                                         Text(
                                                                             text = tabName,
                                                                             style = com.android.snippets.ui.theme.titleMediumEmphasized.copy(
                                                                                 fontSize = 12.sp
                                                                             ),
                                                                             fontWeight = FontWeight.Bold,
                                                                             maxLines = 1
                                                                         )
                                                                     }
                                                                 }
                                                             }
                                                         }
                                                     },
                                                     menuContent = { state ->
                                                         DropdownMenuItem(
                                                             leadingIcon = {
                                                                 if (iconOrEmoji is androidx.compose.ui.graphics.vector.ImageVector) {
                                                                     Icon(iconOrEmoji, contentDescription = null, modifier = Modifier.size(20.dp))
                                                                 } else if (iconOrEmoji is String) {
                                                                     Text(text = iconOrEmoji, fontSize = 16.sp)
                                                                 } else if (iconOrEmoji is Int) {
                                                                     Icon(painterResource(id = iconOrEmoji), contentDescription = null, modifier = Modifier.size(20.dp))
                                                                 }
                                                             },
                                                             text = {
                                                                 Text(
                                                                     text = tabName,
                                                                     style = MaterialTheme.typography.labelLarge
                                                                 )
                                                             },
                                                             onClick = {
                                                                 val pageIndex = pageTabs.indexOf(tabName)
                                                                 if (pageIndex != -1) {
                                                                     scope.launch { pagerState.animateScrollToPage(pageIndex) }
                                                                 }
                                                                 state.dismiss()
                                                             }
                                                         )
                                                     }
                                                 )
                                             }
                                         }
                                     }
                                  }



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
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 8.dp,
                            tonalElevation = 0.dp,
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
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 8.dp,
                            tonalElevation = 0.dp,
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
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
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

                                 // More Options (Middle)
                                 AnimatedCookieButton(
                                     onClick = {
                                         view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                         val activeTab = pageTabs.getOrNull(pagerState.currentPage) ?: "Library"
                                         longPressedCollection = activeTab
                                     },
                                     icon = Icons.Default.MoreHoriz,
                                     contentDescription = "Options",
                                     tooltip = "Options",
                                     isSpinning = !isAnyPopupActive,
                                     size = 48.dp,
                                     containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                     contentColor = MaterialTheme.colorScheme.onSecondaryContainer
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
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
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
                                if (currentTab == "Eatlist") {
                                    MealDropdownChip(
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }

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
                                    tooltip = "Filters",
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
                            show = showHistoryBottomSheet,
                            onDismissRequest = { showHistoryBottomSheet = false },
                            viewModel = viewModel,
                            view = view
                        )



                AnimatedVisibility(
                    visible = isScrolled && !viewModel.isSelectionMode && !isSearchOpen,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .safeDrawingPadding()
                        .padding(bottom = 96.dp)
                        .offset(y = toolbarOffset)
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
                        isSpinning = false
                    )
                }

                AnimatedVisibility(
                    visible = currentTab == "Eatlist" && !viewModel.isSelectionMode && !isSearchOpen,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .safeDrawingPadding()
                        .padding(end = 20.dp, bottom = 92.dp)
                        .offset(y = toolbarOffset)
                ) {
                    FoodRandomizerFab(
                        onClick = {
                            showFoodRandomizerBottomSheet = true
                        }
                    )
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
                        val colName = longPressedCollection ?: ""
                        val iconOrEmoji = when (colName) {
                            "Library" -> Icons.Default.PhotoLibrary
                            "Favorites" -> Icons.Default.Favorite
                            else -> viewModel.getCollectionIcon(colName)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = LocalAppShape.current,
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (iconOrEmoji is androidx.compose.ui.graphics.vector.ImageVector) {
                                        Icon(iconOrEmoji, contentDescription = null, modifier = Modifier.size(24.dp))
                                    } else if (iconOrEmoji is String) {
                                        Text(text = iconOrEmoji, fontSize = 20.sp)
                                    } else if (iconOrEmoji is Int) {
                                        Icon(painterResource(id = iconOrEmoji), contentDescription = null, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            Text(
                                text = colName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val isSystemCollection = longPressedCollection == "Library" || longPressedCollection == "Favorites" || longPressedCollection == "Eatlist"
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp)
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ShapedSectionHeader(icon = Icons.Default.ViewAgenda)
                            Text(
                                text = "View",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentCols = viewModel.customGridColumns?.coerceIn(-1, 3) ?: when (windowSizeClass?.widthSizeClass) {
                                androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Expanded -> 3
                                else -> 2
                            }

                            ViewOptionItem(
                                columnCount = -1,
                                isSelected = currentCols == -1,
                                onClick = {
                                    viewModel.updateCustomGridColumns(-1)
                                    listStates.values.forEach { state ->
                                        scope.launch { state.scrollToItem(0) }
                                    }
                                },
                                icon = ViewAgendaCustomIcon()
                            )

                            ViewOptionItem(
                                columnCount = 0,
                                isSelected = currentCols == 0,
                                onClick = {
                                    viewModel.updateCustomGridColumns(0)
                                    listStates.values.forEach { state ->
                                        scope.launch { state.scrollToItem(0) }
                                    }
                                },
                                icon = FormatListBulletedIcon()
                            )

                            ViewOptionItem(
                                columnCount = 1,
                                isSelected = currentCols == 1,
                                onClick = {
                                    viewModel.updateCustomGridColumns(1)
                                    listStates.values.forEach { state ->
                                        scope.launch { state.scrollToItem(0) }
                                    }
                                },
                                icon = ViewListIcon()
                            )

                            ViewOptionItem(
                                columnCount = 2,
                                isSelected = currentCols == 2,
                                onClick = {
                                    viewModel.updateCustomGridColumns(2)
                                    listStates.values.forEach { state ->
                                        scope.launch { state.scrollToItem(0) }
                                    }
                                },
                                icon = ViewGrid2Icon()
                            )

                            ViewOptionItem(
                                columnCount = 3,
                                isSelected = currentCols == 3,
                                onClick = {
                                    viewModel.updateCustomGridColumns(3)
                                    listStates.values.forEach { state ->
                                        scope.launch { state.scrollToItem(0) }
                                    }
                                },
                                icon = ViewGrid3Icon()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp)
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ShapedSectionHeader(icon = SortIcon())
                            Text(
                                text = "Sort",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

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
                                        icon = { Icon(dateIcons[index], null, modifier = Modifier.size(if (isSelected) 24.dp else 18.dp)) },
                                        label = dateLabels[index]
                                    )
                                }
                            }

                            if (longPressedCollection != "Eatlist") {
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
                                            icon = { Icon(snippetIcons[index], null, modifier = Modifier.size(if (isSelected) 24.dp else 18.dp)) },
                                            label = snippetLabels[index]
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
                                    val starOptions = listOf(com.android.snippets.viewmodel.PhotoSortType.MostStarred, com.android.snippets.viewmodel.PhotoSortType.LeastStarred)
                                    val starLabels = listOf("Most starred", "Least starred")

                                    starOptions.forEachIndexed { index, option ->
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
                                            icon = { Icon(painterResource(id = R.drawable.ic_star_rating), null, modifier = Modifier.size(if (isSelected) 24.dp else 18.dp)) },
                                            label = starLabels[index]
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 28.dp)
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ShapedSectionHeader(icon = MoreSectionIcon())
                                Text(
                                    text = "Action",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }

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
                                    shapes = ButtonDefaults.shapes(
                                        shape = CircleShape,
                                        pressedShape = RoundedCornerShape(12.dp)
                                    ),
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
                                        shapes = ButtonDefaults.shapes(
                                            shape = CircleShape,
                                            pressedShape = RoundedCornerShape(12.dp)
                                        ),
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
                                class Option(val name: String, val icon: Any, val isDestructive: Boolean = false, val action: () -> Unit)
                                val options = buildList {
                                    add(Option("Edit name", Icons.Default.Edit) {
                                        renamingCollection = longPressedCollection
                                        longPressedCollection = null
                                    })
                                    add(Option("Pick an emoji", Icons.Default.AddReaction) {
                                        viewModel.navigateSelectIcon(longPressedCollection!!)
                                        longPressedCollection = null
                                    })
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
                                                leadingContent = { 
                                                     val icon = option.icon
                                                     if (icon is androidx.compose.ui.graphics.vector.ImageVector) {
                                                         Icon(icon, contentDescription = null)
                                                     } else if (icon is Int) {
                                                         Icon(painterResource(id = icon), contentDescription = null)
                                                     }
                                                 },
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

                photoToDeleteFromEatlist?.let { photo ->
                    AlertDialog(
                        onDismissRequest = { photoToDeleteFromEatlist = null },
                        title = { Text("Mark as eaten and delete") },
                        text = { Text("Are you sure you want to mark this item as eaten and delete it from Eatlist?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deletePhoto(photo.id)
                                    photoToDeleteFromEatlist = null
                                }
                            ) {
                                Text("Mark as eaten", color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { photoToDeleteFromEatlist = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                FoodRandomizerBottomSheet(
                    show = showFoodRandomizerBottomSheet,
                    onDismissRequest = { showFoodRandomizerBottomSheet = false },
                    eatlistPhotos = remember(flatPhotosRaw) { flatPhotosRaw.filter { it.collections.contains("Eatlist") } },
                    onSelectPhoto = { photo ->
                        if (windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded) {
                            viewModel.activePhotoId = photo.id
                        } else {
                            viewModel.openDetail(photo.id, overrideReturnScreen = Screen.Library)
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
                Text(
                    text = label,
                    style = if (checked) {
                        MaterialTheme.typography.labelLarge.copy(
                            fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide
                        )
                    } else {
                        MaterialTheme.typography.labelLarge
                    }
                )
            }
        },
        menuContent = { state ->
            DropdownMenuItem(
                leadingIcon = icon,
                text = {
                    Text(
                        text = label,
                        style = if (checked) {
                            MaterialTheme.typography.labelLarge.copy(
                                fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide
                            )
                        } else {
                            MaterialTheme.typography.labelLarge
                        }
                    )
                },
                onClick = {
                    onCheckedChange(!checked)
                    state.dismiss()
                }
            )
        }
    )
}

@Composable
private fun ViewOptionItem(
    columnCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector
) {
    val view = LocalView.current
    val shapeType = LocalAppShapeType.current
    val shape = LocalAppShape.current

    val rotation = remember { Animatable(0f) }
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            when (shapeType) {
                AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> {
                    rotation.animateTo(
                        targetValue = rotation.value + 360f,
                        animationSpec = tween(500, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f))
                    )
                }
                AppShape.COOKIE_4_SIDED -> {
                    launch {
                        scaleX.animateTo(0.75f, animationSpec = tween(70, easing = FastOutLinearInEasing))
                        scaleX.animateTo(1.15f, animationSpec = tween(90, easing = FastOutSlowInEasing))
                        scaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                    launch {
                        scaleY.animateTo(0.75f, animationSpec = tween(70, easing = FastOutLinearInEasing))
                        scaleY.animateTo(1.15f, animationSpec = tween(90, easing = FastOutSlowInEasing))
                        scaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                }
                AppShape.GEM, AppShape.SQUARE -> {
                    launch {
                        scaleX.animateTo(1.18f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                        scaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                    launch {
                        scaleY.animateTo(1.18f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                        scaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                }
                AppShape.PENTAGON -> {
                    rotation.animateTo(-15f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                    rotation.animateTo(15f, animationSpec = tween(120, easing = FastOutSlowInEasing))
                    rotation.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                }
                AppShape.CLOVER_4_LEAF -> {
                    launch {
                        scaleX.animateTo(1.25f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                        scaleX.animateTo(0.8f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                        scaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                    launch {
                        scaleY.animateTo(0.75f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                        scaleY.animateTo(1.2f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                        scaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                }
                AppShape.CLOVER_8_LEAF -> {
                    launch {
                        scaleX.animateTo(1.15f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                        scaleX.animateTo(1.03f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                        scaleX.animateTo(1.20f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                        scaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                    launch {
                        scaleY.animateTo(0.85f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                        scaleY.animateTo(1.08f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                        scaleY.animateTo(0.90f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                        scaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!isSelected) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onClick()
                }
            }
    ) {
        val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.rotationZ = rotation.value
                        this.scaleX = scaleX.value
                        this.scaleY = scaleY.value
                    }
                    .clip(shape)
                    .background(containerColor)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        RadioButton(
            selected = isSelected,
            onClick = {
                if (!isSelected) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onClick()
                }
            },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
    }
}

