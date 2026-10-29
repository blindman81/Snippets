package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.snippets.model.Photo
import com.android.snippets.ui.components.*
import androidx.activity.compose.BackHandler
import com.android.snippets.viewmodel.SnippetsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyRecapScreen(
    viewModel: SnippetsViewModel,
    onBack: () -> Unit = { viewModel.navigateBack() }
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = isSystemInDarkTheme()

    val photos = viewModel.recapPhotos
    val title = viewModel.recapTitle
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    var modalPhoto by remember { mutableStateOf<Photo?>(null) }
    var showAddModal by remember { mutableStateOf(false) }
    var showSnippetsModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }
    var showRateModal by remember { mutableStateOf(false) }
    var showLinkModal by remember { mutableStateOf(false) }
    var showNameModal by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.activePhotoId = null
        }
    }

    val isBackHandlerEnabled = showAddModal ||
        showSnippetsModal ||
        showDeleteModal ||
        showRateModal ||
        showLinkModal ||
        showNameModal ||
        viewModel.activePhotoId != null ||
        viewModel.isSelectionMode

    val handleBack: () -> Unit = {
        when {
            showAddModal -> showAddModal = false
            showSnippetsModal -> showSnippetsModal = false
            showDeleteModal -> showDeleteModal = false
            showRateModal -> showRateModal = false
            showLinkModal -> showLinkModal = false
            showNameModal -> showNameModal = false
            viewModel.activePhotoId != null -> viewModel.activePhotoId = null
            viewModel.isSelectionMode -> viewModel.clearSelection()
            else -> onBack()
        }
    }

    BackHandler(enabled = isBackHandlerEnabled) {
        handleBack()
    }

    LaunchedEffect(photos.isEmpty()) {
        if (photos.isEmpty()) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                viewModel = viewModel,
                title = title,
                onNavigationClick = handleBack,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                leftAlignTitle = true,
                isScrolled = isScrolled
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            if (photos.isNotEmpty()) {
                RecapCardList(
                    photos = photos,
                    viewModel = viewModel,
                    listState = listState,
                    modalPhoto = modalPhoto,
                    showAddModal = showAddModal,
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
                    onDownload = { photo ->
                        viewModel.downloadPhotoCard(
                            context,
                            photo,
                            isDark,
                            if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                        )
                    },
                    onShare = { photo ->
                        viewModel.sharePhotoCard(
                            context,
                            photo,
                            isDark,
                            if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                        )
                    },
                    onToggleFavorite = { photo ->
                        viewModel.toggleFavorite(photo.id)
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
            }
        }
    }

    LibraryModalsHost(
        viewModel = viewModel,
        deletingCollection = null,
        onDismissDeleteCollection = {},
        onConfirmDeleteCollection = {},
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

@Composable
private fun RecapCardList(
    photos: List<Photo>,
    viewModel: SnippetsViewModel,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modalPhoto: Photo?,
    showAddModal: Boolean,
    onPhotoClick: (Photo) -> Unit,
    onPhotoLongClick: (Photo) -> Unit,
    onAddSnippet: (Photo) -> Unit,
    onDownload: (Photo) -> Unit,
    onShare: (Photo) -> Unit,
    onToggleFavorite: (Photo) -> Unit,
    onNamePhoto: (Photo) -> Unit,
    onEditLink: (Photo) -> Unit,
    onRatePhoto: (Photo) -> Unit,
    onViewSnippets: (Photo) -> Unit,
    onDeletePhoto: (Photo) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(photos, key = { _, photo -> photo.id }) { index, photo ->
            PhotoCard(
                photo = photo,
                isSelected = viewModel.selectedPhotoIds.contains(photo.id),
                selectionMode = viewModel.isSelectionMode,
                showFavoriteIcon = true,
                viewModel = viewModel,
                isExpanded = viewModel.activePhotoId == photo.id,
                isAddSnippetOpen = showAddModal && modalPhoto?.id == photo.id,
                onClick = { onPhotoClick(photo) },
                onLongClick = { onPhotoLongClick(photo) },
                onAddSnippet = { onAddSnippet(photo) },
                onDownload = { onDownload(photo) },
                onShare = { onShare(photo) },
                onToggleFavorite = { onToggleFavorite(photo) },
                onNamePhoto = { onNamePhoto(photo) },
                onEditLink = { onEditLink(photo) },
                onRatePhoto = { onRatePhoto(photo) },
                onViewSnippets = { onViewSnippets(photo) },
                onDeletePhoto = { onDeletePhoto(photo) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

