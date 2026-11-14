package com.android.snippets.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import com.android.snippets.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
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
                        .hazeSource(state = hazeState)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                } else Modifier
            ),
        pageSpacing = 16.dp
    ) { page ->
        val tabForPage = pageTabs.getOrNull(page) ?: "Library"
        val tabSortType = if (tabForPage == "Library") uiState.photoSortType else uiState.collectionPhotoSortTypes[tabForPage] ?: PhotoSortType.DateNewest
        val pageFilteredPhotos = remember(flatPhotosRaw, tabForPage, tabSortType) {
            val filtered = if (tabForPage == "Library") {
                flatPhotosRaw
            } else {
                flatPhotosRaw.filter { photo ->
                    when (tabForPage) {
                        "Favorites" -> photo.isFavorite
                        else -> photo.collections.contains(tabForPage)
                    }
                }
            }
            val effectiveRawSort = uiState.currentSearchSortType ?: uiState.photoSortType
            if (tabForPage == "Library" && tabSortType == effectiveRawSort) {
                filtered
            } else {
                viewModel.sortPhotos(filtered, tabSortType)
            }
        }

        val pageHasMemories = remember(memoryViewModel.historyMemories, tabForPage) {
            tabForPage == "Library" && memoryViewModel.historyMemories.isNotEmpty()
        }
        val effectiveTopPadding = if (statusBarTop > 0.dp) {
            if (pageHasMemories) statusBarTop + 176.dp else statusBarTop + 72.dp
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
                    "Favorites" -> EmptyFavoritesState()
                    "Library" -> EmptyLibraryState()
                    else -> {
                        if (!uiState.isSearching) {
                            EmptyCollectionState(icon = Icons.Outlined.FolderOpen)
                        }
                    }
                }
            }
        } else {
            val pageListState = listStates[tabForPage] ?: listStates.getOrPut(tabForPage) { LazyStaggeredGridState() }

            LaunchedEffect(viewModel.selectedFilterSnippets) {
                pageListState.scrollToItem(0)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(1),
                    state = pageListState,
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
                            userCollections = uiState.userCollections,
                            locationNameMap = uiState.locationNameMap,
                            collectionIcons = uiState.collectionIcons,
                            showFavoriteIcon = tabForPage != "Favorites",
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
