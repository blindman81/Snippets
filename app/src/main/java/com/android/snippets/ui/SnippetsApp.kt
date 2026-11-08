package com.android.snippets.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.components.BusyOverlay
import com.android.snippets.ui.components.DeleteConfirmationModal
import com.android.snippets.ui.components.LocalHazeState
import com.android.snippets.ui.components.SnippetsSnackbarEffect
import com.android.snippets.ui.components.SnippetsSnackbarHost
import com.android.snippets.ui.util.LocalTiltActive
import com.android.snippets.viewmodel.SnippetsViewModel
import dev.chrisbanes.haze.HazeState

@Composable
fun SnippetsApp(viewModel: SnippetsViewModel, windowSizeClass: WindowSizeClass) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingCollectionForPicker by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val pendingColl = pendingCollectionForPicker
            if (pendingColl != null && pendingColl != "Library" && pendingColl != "Favorites") {
                viewModel.addPhotosToCollection(uris, pendingColl)
            } else {
                viewModel.addPhotos(uris, isFavorite = uiState.pendingFavoriteIntent)
            }
            pendingCollectionForPicker = null
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val isOverlayShowing = uiState.showBulkDeleteModal || uiState.showFilterSheet || uiState.isAddingPhotos || uiState.isBusy || uiState.isCuratingMemories

    SnippetsSnackbarEffect(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState
    )

    LaunchedEffect(uiState.pendingAddPhotoIntentToken) {
        if (uiState.pendingAddPhotoIntentToken != 0L) {
            viewModel.pendingFavoriteIntent = false
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            viewModel.pendingAddPhotoIntentToken = 0L
        }
    }

    val rootHazeState = remember { HazeState() }
    val isScreenActive = !isOverlayShowing

    CompositionLocalProvider(
        LocalHazeState provides rootHazeState,
        LocalTiltActive provides isScreenActive
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LibraryScreen(
                viewModel = viewModel,
                windowSizeClass = windowSizeClass,
                onAddPhotos = { tab ->
                    when (tab) {
                        "Library" -> {
                            viewModel.pendingFavoriteIntent = false
                            pendingCollectionForPicker = null
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        "Favorites" -> viewModel.startCollectionAssignment("Favorites")
                        else -> viewModel.startCollectionAssignment(tab)
                    }
                }
            )

            SnippetsSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            )

            if (uiState.showBulkDeleteModal) {
                DeleteConfirmationModal(
                    count = uiState.selectedPhotoIds.size,
                    onDismiss = { viewModel.showBulkDeleteModal = false },
                    onConfirm = {
                        viewModel.showBulkDeleteModal = false
                        viewModel.deleteSelectedPhotos(unpublish = false)
                    }
                )
            }

            // Global Busy Overlays
            if (uiState.isAddingPhotos || uiState.isBusy || uiState.isCuratingMemories) {
                BusyOverlay()
            }
        }
    }
}
