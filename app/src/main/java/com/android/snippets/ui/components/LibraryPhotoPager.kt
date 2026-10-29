package com.android.snippets.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import com.android.snippets.ui.*
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.isSystemInDarkTheme
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LibraryPhotoPager(
    pagerState: PagerState,
    pageTabs: List<String>,
    flatPhotosRaw: List<Photo>,
    viewModel: SnippetsViewModel,
    listStates: MutableMap<String, LazyStaggeredGridState>,
    topGridPadding: Dp,
    hazeState: HazeState,
    showAddModal: Boolean,
    modalPhotoId: String?,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topGridPadding),
                contentAlignment = Alignment.Center
            ) {
                when (tabForPage) {
                    "Favorites" -> EmptyFavoritesState()
                    "Library" -> EmptyLibraryState()
                    else -> EmptyCollectionState()
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
                    val isMost = tabSortType == PhotoSortType.MostSnippets && photo.snippets.isNotEmpty()
                    val isLeast = tabSortType == PhotoSortType.LeastSnippets && photo.snippets.isNotEmpty()
                    val isDark = isSystemInDarkTheme()

                    PhotoCard(
                        photo = photo,
                        isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                        selectionMode = viewModel.isSelectionMode,
                        showFavoriteIcon = tabForPage != "Favorites",
                        tab = tabForPage,
                        isMostSnippets = isMost,
                        isLeastSnippets = isLeast,
                        viewModel = viewModel,
                        isExpanded = viewModel.activePhotoId == photo.id,
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
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
