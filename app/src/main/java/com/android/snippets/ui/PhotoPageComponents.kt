package com.android.snippets.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.snippets.model.Photo
import com.android.snippets.viewmodel.SnippetsViewModel

import androidx.compose.ui.layout.ContentScale


data class PhotoPage(
    val dateHeader: String?,
    val photos: List<Photo>
)

fun getMatchingSnippetsCount(photo: Photo, viewModel: SnippetsViewModel): Int {
    return when {
        viewModel.selectedFilterSnippets.isNotEmpty() -> {
            photo.snippets.count { s -> viewModel.selectedFilterSnippets.any { it.equals(s, ignoreCase = true) } }
        }
        viewModel.photoSortType == com.android.snippets.viewmodel.PhotoSortType.MostSnippets || 
        viewModel.photoSortType == com.android.snippets.viewmodel.PhotoSortType.LeastSnippets -> {
            photo.snippets.size
        }
        else -> 0
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PhotoPageGrid(
    photos: List<Photo>,
    viewModel: SnippetsViewModel,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    showFavoriteIcon: Boolean,
    onClick: (Photo) -> Unit,
    onLongClick: (Photo) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (photos.size) {
            1 -> {
                val photo = photos[0]
                PhotoMasonryItem(
                    photo = photo,
                    isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                    selectionMode = viewModel.isSelectionMode,

                    showFavoriteIcon = showFavoriteIcon,
                    matchingSnippetsCount = getMatchingSnippetsCount(photo, viewModel),
                    onClick = { onClick(photo) },
                    onLongClick = { onLongClick(photo) },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    fillCard = true,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }
            2 -> {
                Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    photos.forEach { photo ->
                        PhotoMasonryItem(
                            photo = photo,
                            isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                            selectionMode = viewModel.isSelectionMode,

                            showFavoriteIcon = showFavoriteIcon,
                            matchingSnippetsCount = getMatchingSnippetsCount(photo, viewModel),
                            onClick = { onClick(photo) },
                            onLongClick = { onLongClick(photo) },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            fillCard = true,
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(4.dp)
                        )
                    }
                }
            }
            3 -> {
                Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    PhotoMasonryItem(
                        photo = photos[0],
                        isSelected = viewModel.selectedPhotoIds.contains(photos[0].id),
                        selectionMode = viewModel.isSelectionMode,

                        showFavoriteIcon = showFavoriteIcon,
                        matchingSnippetsCount = getMatchingSnippetsCount(photos[0], viewModel),
                        onClick = { onClick(photos[0]) },
                        onLongClick = { onLongClick(photos[0]) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        fillCard = true,
                        modifier = Modifier.weight(1.2f).fillMaxHeight().padding(4.dp)
                    )
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        photos.drop(1).forEach { photo ->
                            PhotoMasonryItem(
                                photo = photo,
                                isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                                selectionMode = viewModel.isSelectionMode,

                                showFavoriteIcon = showFavoriteIcon,
                                matchingSnippetsCount = getMatchingSnippetsCount(photo, viewModel),
                                onClick = { onClick(photo) },
                                onLongClick = { onLongClick(photo) },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                fillCard = true,
                                modifier = Modifier.weight(1f).fillMaxWidth().padding(4.dp)
                            )
                        }
                    }
                }
            }
            4 -> {
                Column(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        photos.take(2).forEach { photo ->
                            PhotoMasonryItem(
                                photo = photo,
                                isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                                selectionMode = viewModel.isSelectionMode,

                                showFavoriteIcon = showFavoriteIcon,
                                matchingSnippetsCount = getMatchingSnippetsCount(photo, viewModel),
                                onClick = { onClick(photo) },
                                onLongClick = { onLongClick(photo) },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                fillCard = true,
                                modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
                            )
                        }
                    }
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        photos.drop(2).forEach { photo ->
                            PhotoMasonryItem(
                                photo = photo,
                                isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                                selectionMode = viewModel.isSelectionMode,

                                showFavoriteIcon = showFavoriteIcon,
                                matchingSnippetsCount = getMatchingSnippetsCount(photo, viewModel),
                                onClick = { onClick(photo) },
                                onLongClick = { onLongClick(photo) },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                fillCard = true,
                                modifier = Modifier.weight(1f).fillMaxHeight().padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
