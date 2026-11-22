package com.android.snippets.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.components.BusyOverlay
import com.android.snippets.ui.components.DeleteConfirmationModal
import com.android.snippets.ui.components.modals.RequestTileServiceModal
import com.android.snippets.ui.components.LocalHazeState
import com.android.snippets.ui.components.SnippetsSnackbarEffect
import com.android.snippets.ui.components.SnippetsSnackbarHost
import com.android.snippets.ui.util.LocalTiltActive
import com.android.snippets.viewmodel.SnippetsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun SnippetsApp(viewModel: SnippetsViewModel, windowSizeClass: WindowSizeClass) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingBoxForPicker by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val pendingBox = pendingBoxForPicker
            if (pendingBox != null && pendingBox != "Box" && pendingBox != "Library" && pendingBox != "Box 1" && pendingBox != "Favorites") {
                viewModel.addPhotosToBox(uris, pendingBox)
            } else {
                viewModel.addPhotos(uris, isFavorite = uiState.pendingFavoriteIntent || pendingBox == "Box 1" || pendingBox == "Favorites")
            }
            pendingBoxForPicker = null
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val showLoadingOverlay = uiState.isInitialLoading || uiState.isAddingPhotos || uiState.isBusy || uiState.isCuratingMemories
    val isOverlayShowing = uiState.showBulkDeleteModal || uiState.showRequestTileDialog || showLoadingOverlay

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
    val loadingHazeState = remember { HazeState() }
    val isScreenActive = !isOverlayShowing

    CompositionLocalProvider(
        LocalHazeState provides rootHazeState,
        LocalTiltActive provides isScreenActive
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = loadingHazeState)
            ) {
                LibraryScreen(
                    viewModel = viewModel,
                    windowSizeClass = windowSizeClass,
                    onAddPhotos = { tab ->
                        when (tab) {
                            "Box", "Library" -> {
                                viewModel.pendingFavoriteIntent = false
                                pendingBoxForPicker = null
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            "Box 1", "Favorites" -> viewModel.startBoxAssignment("Box 1")
                            else -> viewModel.startBoxAssignment(tab)
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

                if (uiState.showRequestTileDialog) {
                    val context = LocalContext.current
                    RequestTileServiceModal(
                        onDismiss = { viewModel.dismissRequestTileDialog() },
                        onConfirm = { viewModel.confirmRequestTileDialog(context) }
                    )
                }
            }

            AnimatedVisibility(
                visible = showLoadingOverlay,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(350)),
                modifier = Modifier.fillMaxSize()
            ) {
                BusyOverlay(hazeState = loadingHazeState)
            }
        }
    }
}
