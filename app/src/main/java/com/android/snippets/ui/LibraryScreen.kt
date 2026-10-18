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
    
    var longPressedCollection by remember { mutableStateOf<String?>(null) }
    var deletingCollection by remember { mutableStateOf<String?>(null) }

    val isAnyPopupActive = viewModel.showBulkAddToCollectionDialog || viewModel.showBulkDeleteModal || viewModel.showCreateDialog || showMenuPopup || showCollectionsPopup || isSearchOpen || showHistoryBottomSheet || (longPressedCollection != null) || (deletingCollection != null)
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

    val tabBarOffset by animateDpAsState(
        targetValue = if (isScrolled) (-100).dp else 0.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "tab_bar_offset"
    )

    val tabBarAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 0f else 1f,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "tab_bar_alpha"
    )

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val hazeState = com.android.snippets.ui.components.LocalHazeState.current ?: dev.chrisbanes.haze.rememberHazeState()

    var hazeInvalidationTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(viewModel.customGridColumns, currentTab) {
        repeat(12) {
            hazeInvalidationTrigger++
            kotlinx.coroutines.delay(30)
        }
    }

    LaunchedEffect(currentGridState.isScrollInProgress) {
        if (currentGridState.isScrollInProgress) {
            while (currentGridState.isScrollInProgress) {
                hazeInvalidationTrigger++
                kotlinx.coroutines.delay(16)
            }
            hazeInvalidationTrigger++
        }
    }

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
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                hazeInvalidationTrigger
                                viewModel.customGridColumns
                                drawContent()
                            }
                            .then(
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    Modifier.hazeSource(state = hazeState, key = viewModel.customGridColumns)
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

                        if (pageFilteredPhotos.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
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
                            val pageListState = listStates.getOrPut(tabForPage) { LazyStaggeredGridState() }
                            val rawColumns = viewModel.customGridColumns?.coerceIn(-1, 3)
                            val currentGridColumns = if (rawColumns == 1) -1 else (rawColumns ?: when (windowSizeClass?.widthSizeClass) {
                                WindowWidthSizeClass.Expanded -> 3
                                WindowWidthSizeClass.Medium -> 3
                                else -> -1
                            })
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
                                                                 val nextCol = if (currentGridColumns == 2) 0 else currentGridColumns - 1
                                                                 viewModel.updateCustomGridColumns(nextCol)
                                                             }
                                                             zoomAccumulator = 1f
                                                         } else if (zoomAccumulator < 0.8f) {
                                                             // Pinch in (contracting fingers) -> more columns -> smaller items
                                                             if (currentGridColumns < maxColumns) {
                                                                 view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                                 val nextCol = if (currentGridColumns == 0) 2 else currentGridColumns + 1
                                                                 viewModel.updateCustomGridColumns(nextCol)
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
                                    bottom = 140.dp
                                ),
                                              horizontalArrangement = Arrangement.spacedBy(4.dp),
                                              verticalItemSpacing = if (currentGridColumns == 0) 2.dp else 4.dp
                                          ) {
                                              itemsIndexed(pageFilteredPhotos, key = { _, photo -> photo.id }) { index, photo ->
                                                   val isMost = tabSortType == com.android.snippets.viewmodel.PhotoSortType.MostSnippets && photo.snippets.isNotEmpty()
                                                   val isLeast = tabSortType == com.android.snippets.viewmodel.PhotoSortType.LeastSnippets && photo.snippets.isNotEmpty()
                                                  
                                                   when (currentGridColumns) {
                                                       -1, 1 -> {
                                                           // Calculate card position: First, Middle, Last, or Single
                                                           val totalCount = pageFilteredPhotos.size
                                                           val cardPos = when {
                                                               totalCount == 1 -> CardPosition.Single
                                                               index == 0 -> CardPosition.First
                                                               index == totalCount - 1 -> CardPosition.Last
                                                               else -> CardPosition.Middle
                                                           }

                                                           PhotoCard(
                                                               photo = photo,
                                                               position = CardPosition.Single,
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
                                                               sharedTransitionScope = sharedTransitionScope,
                                                               animatedVisibilityScope = animatedVisibilityScope,
                                                               modifier = Modifier.fillMaxWidth()
                                                           )
                                                       }
                                                       0 -> {
                                                           val totalCount = pageFilteredPhotos.size
                                                           val cardPos = when {
                                                               totalCount == 1 -> CardPosition.Single
                                                               index == 0 -> CardPosition.First
                                                               index == totalCount - 1 -> CardPosition.Last
                                                               else -> CardPosition.Middle
                                                           }
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
                                                               sharedTransitionScope = sharedTransitionScope,
                                                               animatedVisibilityScope = animatedVisibilityScope,
                                                               modifier = Modifier.fillMaxWidth()
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
                                                               shape = RoundedCornerShape(12.dp),
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
                                                               fillCard = false,
                                                               viewModel = viewModel,
                                                               modifier = Modifier.fillMaxWidth()
                                                           )
                                                       }
                                                   }
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

                                 // Tab Bar stuck at the top of the surface background
                                 Box(
                                     modifier = Modifier
                                         .align(Alignment.TopStart)
                                         .fillMaxWidth()
                                         .padding(top = statusBarTop + 8.dp)
                                         .graphicsLayer {
                                             translationY = tabBarOffset.toPx()
                                             alpha = tabBarAlpha
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
                                          val navTabItems = remember(allTabs, currentTab) {
                                              allTabs.mapIndexed { tabIndex, tabName ->
                                                  val isSelected = tabName == currentTab
                                                  val iconOrEmoji = when (tabName) {
                                                      "Library" -> Icons.Default.PhotoLibrary
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

                                          val selectedTabIndex = allTabs.indexOf(currentTab).coerceAtLeast(0)

                                          CookieButtonGroup(
                                              selectedIndex = selectedTabIndex,
                                              onSelect = { index ->
                                                  val tabName = allTabs.getOrNull(index) ?: return@CookieButtonGroup
                                                  val pageIndex = pageTabs.indexOf(tabName)
                                                  if (pageIndex != -1) {
                                                      if (tabName == currentTab) {
                                                          view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                                      } else {
                                                          scope.launch { pagerState.animateScrollToPage(pageIndex) }
                                                      }
                                                  }
                                              },
                                              items = navTabItems,
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

                        BlurredSurface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shadowElevation = 8.dp,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
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
                        }
                    } else {
                        // â”€â”€ NORMAL MODE â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                        BlurredSurface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shadowElevation = 8.dp,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
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
                                Box {
                                    AnimatedCookieButton(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            showHistoryBottomSheet = true
                                        },
                                        icon = Icons.Default.History,
                                        contentDescription = "History",
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
                        .padding(bottom = 132.dp)
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


            if (longPressedCollection != null) {
                BlurredModalBottomSheet(
                    onDismissRequest = { longPressedCollection = null },
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        com.android.snippets.ui.util.LocalTiltActive provides (com.android.snippets.ui.util.LocalTiltActive.current && (deletingCollection == null))
                    ) {
                        Motion.ExpressiveSheetContent {
                            val managePagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

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
                                "Library" -> Icons.Default.PhotoLibrary
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
                                            icon = Icons.Default.Delete,
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

                            val currentSortType = viewModel.getPhotoSortTypeFor(longPressedCollection ?: "Library")

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
                                    cookieButtonItem(Icons.Default.TextSnippet, contentDescription = "Most snippets", tooltip = "Most snippets", iconSize = 22.dp),
                                    cookieButtonItem(R.drawable.ic_star_rating, contentDescription = "Most starred", tooltip = "Most starred", iconSize = 22.dp),
                                    cookieButtonItem(Icons.Default.TextSnippet, contentDescription = "Least snippets", tooltip = "Least snippets", iconSize = 22.dp),
                                    cookieButtonItem(R.drawable.ic_star_rating, contentDescription = "Least starred", tooltip = "Least starred", iconSize = 22.dp)
                                )
                            }
                            val selectedSortIndex = sortOptions.indexOf(currentSortType)

                            val currentCols = when (val raw = viewModel.customGridColumns) {
                                null -> when (windowSizeClass?.widthSizeClass) {
                                    androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Expanded,
                                    androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Medium -> 3
                                    else -> 1
                                }
                                0 -> 1
                                else -> raw.coerceIn(1, 3)
                            }

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
                                        Surface(
                                            shape = CookieShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(44.dp)
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

                                        Spacer(modifier = Modifier.height(38.dp))

                                        // Spring physics M3 pagination dots
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            repeat(3) { index ->
                                                val isSelected = (if (managePagerState.isScrollInProgress) managePagerState.targetPage else managePagerState.currentPage) == index
                                                val animatedWidth by animateDpAsState(
                                                    targetValue = if (isSelected) 18.dp else 6.dp,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    ),
                                                    label = "manage_dot_width_$index"
                                                )
                                                val animatedColor by animateColorAsState(
                                                    targetValue = if (isSelected) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.secondaryContainer
                                                    },
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    ),
                                                    label = "manage_dot_color_$index"
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .height(6.dp)
                                                        .width(animatedWidth)
                                                        .clip(CircleShape)
                                                        .background(animatedColor)
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
                                                )
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
                                                                onSelect = { index ->
                                                                    val action = actions.getOrNull(index) ?: return@CookieButtonGroup
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
                                                                                imageVector = Icons.Default.Delete,
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
                                                                onItemClick = { index ->
                                                                    val action = actions.getOrNull(index) ?: return@CookieButtonGroup
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
                                                                                imageVector = Icons.Default.Delete,
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
                                                                unselectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                                                onSelect = { index ->
                                                                    val option = sortOptions.getOrNull(index) ?: return@CookieButtonGroup
                                                                    if (currentSortType != option) {
                                                                        val col = longPressedCollection ?: "Library"
                                                                        viewModel.setPhotoSortTypeFor(col, option)
                                                                        listStates[col]?.let { state ->
                                                                            scope.launch { state.scrollToItem(0) }
                                                                        }
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
                                                                                imageVector = Icons.Default.TextSnippet,
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
                                                                                imageVector = Icons.Default.TextSnippet,
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
                                                                unselectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                enableWiggle = true
                                                            )
                                                        }
                                                    }
                                                    2 -> {
                                                        // Page 3: View items (no horizontalScroll, easily fits 3 items and swipe works smoothly)
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = if (availableWidth >= 86.dp * 3) {
                                                                Arrangement.SpaceBetween
                                                            } else {
                                                                Arrangement.spacedBy(10.dp, Alignment.Start)
                                                            },
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            ViewOptionItem(
                                                                columnCount = 1,
                                                                isSelected = currentCols == 1,
                                                                onClick = {
                                                                    viewModel.updateCustomGridColumns(1)
                                                                    listStates.values.forEach { state ->
                                                                        scope.launch { state.scrollToItem(0) }
                                                                    }
                                                                    showPillInfo("Single column") {
                                                                        Icon(ViewAgendaCustomIcon(), contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                                    }
                                                                },
                                                                icon = ViewAgendaCustomIcon(),
                                                                size = 86.dp
                                                            )

                                                            ViewOptionItem(
                                                                columnCount = 2,
                                                                isSelected = currentCols == 2,
                                                                onClick = {
                                                                    viewModel.updateCustomGridColumns(2)
                                                                    listStates.values.forEach { state ->
                                                                        scope.launch { state.scrollToItem(0) }
                                                                    }
                                                                    showPillInfo("Two columns") {
                                                                        Icon(ViewGrid2Icon(), contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                                    }
                                                                },
                                                                icon = ViewGrid2Icon(),
                                                                size = 86.dp
                                                            )

                                                            ViewOptionItem(
                                                                columnCount = 3,
                                                                isSelected = currentCols == 3,
                                                                onClick = {
                                                                    viewModel.updateCustomGridColumns(3)
                                                                    listStates.values.forEach { state ->
                                                                        scope.launch { state.scrollToItem(0) }
                                                                    }
                                                                    showPillInfo("Three columns") {
                                                                        Icon(ViewGrid3Icon(), contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                                    }
                                                                },
                                                                icon = ViewGrid3Icon(),
                                                                size = 86.dp
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
            }
        }
    }
}
}






}



@Composable
private fun ViewOptionItem(
    columnCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    size: androidx.compose.ui.unit.Dp = 86.dp
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val shape = CookieShape

    val rotation = remember { Animatable(0f) }
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }
    var spinJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val tiltAngle by com.android.snippets.ui.util.rememberCookieTilt(enabled = isSelected)

    fun triggerSpin() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        onClick()
        spinJob?.cancel()
        spinJob = scope.launch {
            val scaleJobX = launch {
                scaleX.animateTo(0.84f, Motion.PressSpring)
                scaleX.animateTo(1f, Motion.BouncySpring)
            }
            val scaleJobY = launch {
                scaleY.animateTo(0.84f, Motion.PressSpring)
                scaleY.animateTo(1f, Motion.BouncySpring)
            }
            val rotJob = launch {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = Motion.BouncySpring
                )
            }
            rotJob.join()
            scaleJobX.join()
            scaleJobY.join()
        }
    }

    var lastIsSelected by remember { mutableStateOf(isSelected) }
    LaunchedEffect(isSelected) {
        if (isSelected && !lastIsSelected && spinJob?.isActive != true) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = Motion.BouncySpring
            )
        }
        lastIsSelected = isSelected
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(durationMillis = 200),
        label = "view_option_container_color"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "view_option_content_color"
    )

    Box(
        modifier = Modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                triggerSpin()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.rotationZ = rotation.value + (if (isSelected) tiltAngle else 0f)
                    this.scaleX = scaleX.value
                    this.scaleY = scaleY.value
                }
                .clip(shape)
                .background(animatedContainerColor)
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = animatedContentColor,
            modifier = Modifier.size(if (size.value >= 86f) 32.dp else 24.dp)
        )
    }
}

private data class SortPillInfo(
    val text: String,
    val icon: @Composable () -> Unit,
    val timestamp: Long = System.currentTimeMillis()
)
