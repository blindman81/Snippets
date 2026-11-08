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
import com.android.snippets.model.Photo
import com.android.snippets.ui.components.*
import com.android.snippets.viewmodel.SnippetsUiState
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyRecapScreen(
    viewModel: SnippetsViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = isSystemInDarkTheme()

    val photos = uiState.recapPhotos
    val title = uiState.recapTitle
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

    val handleBack = {
        val act = context as? androidx.activity.ComponentActivity
        if (act != null) {
            act.finish()
        } else {
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
            contentAlignment = if (photos.isNotEmpty()) Alignment.TopCenter else Alignment.Center
        ) {
            if (photos.isNotEmpty()) {
                RecapCardList(
                    photos = photos,
                    uiState = uiState,
                    viewModel = viewModel,
                    listState = listState,
                    modalPhoto = modalPhoto,
                    showAddModal = showAddModal,
                    onPhotoClick = { photo ->
                        if (uiState.isSelectionMode) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.toggleSelection(photo.id)
                        } else {
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            val isExpanding = uiState.activePhotoId != photo.id
                            viewModel.activePhotoId = if (isExpanding) photo.id else null
                        }
                    },
                    onPhotoLongClick = { photo ->
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
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
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "No photos found for $title",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = handleBack) {
                        Text("Go Back")
                    }
                }
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
    uiState: SnippetsUiState,
    viewModel: SnippetsViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
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
            val isSelected = uiState.selectedPhotoIds.contains(photo.id)
            val selectedCount = if (isSelected) uiState.selectedPhotoIds.indexOf(photo.id) + 1 else 0
            PhotoCard(
                photo = photo,
                isSelected = isSelected,
                selectionMode = uiState.isSelectionMode,
                selectedCount = selectedCount,
                userCollections = uiState.userCollections,
                locationNameMap = uiState.locationNameMap,
                showFavoriteIcon = true,
                viewModel = viewModel,
                expandLocationIcon = settingsViewModel.expandLocationIcon,
                expandLabelIcon = settingsViewModel.expandLabelIcon,
                expandCalendarIcon = settingsViewModel.expandCalendarIcon,
                isExpanded = uiState.activePhotoId == photo.id,
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

