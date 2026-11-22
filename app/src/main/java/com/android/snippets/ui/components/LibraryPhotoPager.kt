package com.android.snippets.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import com.android.snippets.logic.PhotoSearchHelper
import com.android.snippets.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.snippets.model.Photo
import com.android.snippets.viewmodel.PhotoSortType
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.SnippetsUiState
import com.android.snippets.viewmodel.SettingsViewModel
import com.android.snippets.viewmodel.MemoryViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun LibraryPhotoPager(
    pagerState: PagerState,
    pageTabs: List<String>,
    flatPhotosRaw: List<Photo>,
    uiState: SnippetsUiState,
    viewModel: SnippetsViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    memoryViewModel: MemoryViewModel = hiltViewModel(),
    listStates: MutableMap<String, LazyStaggeredGridState>,
    topGridPadding: Dp = 0.dp,
    statusBarTop: Dp = 0.dp,
    hazeState: HazeState,
    showAddModal: Boolean,
    modalPhotoId: String?,
    onPhotoClick: (Photo) -> Unit,
    onPhotoLongClick: (Photo) -> Unit,
    onAddSnippet: (Photo) -> Unit,
    onNamePhoto: (Photo) -> Unit,
    onEditLink: (Photo) -> Unit,
    onRatePhoto: (Photo) -> Unit,
    onViewSnippets: (Photo) -> Unit,
    onDeletePhoto: (Photo) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    HorizontalPager(
        state = pagerState,
        key = { page -> pageTabs.getOrNull(page) ?: page },
        beyondViewportPageCount = 1,
        modifier = modifier
            .fillMaxSize()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Modifier
                        .hazeSource(state = hazeState, zIndex = 0f)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                } else Modifier
            ),
        pageSpacing = 16.dp
    ) { page ->
        val tabForPage = pageTabs.getOrNull(page) ?: "Box"
        val isBoxTab = tabForPage == "Box" || tabForPage == "Library"
        val tabSortType = if (isBoxTab) uiState.photoSortType else uiState.boxPhotoSortTypes[tabForPage] ?: PhotoSortType.DateNewest
        val pageFilteredPhotos = remember(flatPhotosRaw, tabForPage, tabSortType, uiState.boxIcons) {
            val filtered = if (isBoxTab) {
                flatPhotosRaw
            } else {
                flatPhotosRaw.filter { photo ->
                    PhotoSearchHelper.matchesTab(photo, tabForPage, uiState.boxIcons)
                }
            }
            val effectiveRawSort = uiState.currentSearchSortType ?: uiState.photoSortType
            if (isBoxTab && tabSortType == effectiveRawSort) {
                filtered
            } else {
                viewModel.sortPhotos(filtered, tabSortType)
            }
        }

        val pageHasMemories = remember(memoryViewModel.historyMemories, tabForPage, uiState.searchQuery) {
            isBoxTab && memoryViewModel.historyMemories.isNotEmpty() && uiState.searchQuery.isEmpty()
        }
        val effectiveTopPadding = if (statusBarTop > 0.dp) {
            if (pageHasMemories) {
                statusBarTop + 176.dp + 8.dp
            } else {
                statusBarTop + 72.dp + 8.dp
            }
        } else {
            topGridPadding
        }

        if (pageFilteredPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = effectiveTopPadding),
                contentAlignment = Alignment.Center
            ) {
                when (tabForPage) {
                    "Box 1", "Favorites" -> EmptyBox1State()
                    "Box", "Library" -> EmptyLibraryState()
                    else -> {
                        if (!uiState.isSearching) {
                            EmptyBoxState()
                        }
                    }
                }
            }
        } else {
            val pageListState = listStates[tabForPage] ?: listStates.getOrPut(tabForPage) { LazyStaggeredGridState() }

            LaunchedEffect(viewModel.selectedFilterSnippets, uiState.searchQuery) {
                pageListState.scrollToItem(0)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(1),
                    state = pageListState,
                    cacheWindow = LazyLayoutCacheWindow(ahead = 320.dp, behind = 160.dp, isNonScrollCachingEnabled = true),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 0.dp,
                        end = 0.dp,
                        top = effectiveTopPadding,
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
                        val isMost = tabSortType == PhotoSortType.MostSnippets && photo.snippets.isNotEmpty()
                        val isLeast = tabSortType == PhotoSortType.LeastSnippets && photo.snippets.isNotEmpty()
                        val isDark = isSystemInDarkTheme()

                        val isSelected = uiState.selectedPhotoIds.contains(photo.id)
                        val selectedCount = if (isSelected) uiState.selectedPhotoIds.indexOf(photo.id) + 1 else 0

                        PhotoCard(
                            photo = photo,
                            isSelected = isSelected,
                            selectionMode = uiState.isSelectionMode,
                            selectedCount = selectedCount,
                            userBoxes = uiState.userBoxes,
                            locationNameMap = uiState.locationNameMap,
                            boxIcons = uiState.boxIcons,
                            showFavoriteIcon = tabForPage != "Box 1" && tabForPage != "Favorites",
                            tab = tabForPage,
                            isMostSnippets = isMost,
                            isLeastSnippets = isLeast,
                            expandLocationIcon = settingsViewModel.expandLocationIcon,
                            expandLabelIcon = settingsViewModel.expandLabelIcon,
                            expandCalendarIcon = settingsViewModel.expandCalendarIcon,
                            isExpanded = uiState.activePhotoId == photo.id,
                            isAddSnippetOpen = showAddModal && modalPhotoId == photo.id,
                            onClick = { onPhotoClick(photo) },
                            onLongClick = { onPhotoLongClick(photo) },
                            onAddSnippet = { onAddSnippet(photo) },
                            onDownload = {
                                viewModel.downloadPhotoCard(
                                    context,
                                    photo,
                                    isDark,
                                    if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                                )
                            },
                            onShare = {
                                viewModel.sharePhotoCard(
                                    context,
                                    photo,
                                    isDark,
                                    if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                                )
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(photo.id)
                            },
                            onNamePhoto = { onNamePhoto(photo) },
                            onEditLink = { onEditLink(photo) },
                            onRatePhoto = { onRatePhoto(photo) },
                            onViewSnippets = { onViewSnippets(photo) },
                            onDeletePhoto = { onDeletePhoto(photo) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Scrollbar(
                    state = pageListState,
                    totalItemsCount = pageFilteredPhotos.size,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = effectiveTopPadding + 4.dp,
                            bottom = 144.dp,
                            end = 2.dp
                        )
                        .fillMaxHeight()
                )
            }
        }
    }
}
