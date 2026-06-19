package com.android.snippets.ui

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import androidx.activity.BackEventCompat
import androidx.activity.ExperimentalActivityApi
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.android.snippets.viewmodel.Screen
import com.android.snippets.viewmodel.SnippetsViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.android.snippets.ui.util.Motion

@OptIn(ExperimentalMaterial3Api::class, ExperimentalActivityApi::class)
@Composable
fun SnippetsApp(viewModel: SnippetsViewModel, windowSizeClass: WindowSizeClass) {
    val view = androidx.compose.ui.platform.LocalView.current
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.addPhoto(it, isFavorite = viewModel.pendingFavoriteIntent) }
    }

    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val activeColl = viewModel.activeCollection
                if (activeColl != null && activeColl != "Library" && activeColl != "Favorites") {
                    viewModel.addPhotoToCollection(uri, activeColl)
                } else {
                    viewModel.addPhoto(uri, isFavorite = viewModel.pendingFavoriteIntent)
                }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val motionScheme = MaterialTheme.motionScheme
    val predictiveBackProgress = remember { Animatable(0f) }
    var predictiveBackEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }
    val predictiveBackEnabled = viewModel.currentScreen != Screen.Library

    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage?.let { message ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = viewModel.snackbarActionLabel,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onSnackbarAction?.invoke()
                }
                viewModel.snackbarMessage = null
                viewModel.snackbarActionLabel = null
                viewModel.onSnackbarAction = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        @OptIn(ExperimentalSharedTransitionApi::class)
        SharedTransitionLayout {
            val showDetail = viewModel.currentScreen == Screen.Detail
            val baseScreen = if (showDetail) viewModel.detailReturnScreen else viewModel.currentScreen
            val showDetailOverlay = showDetail

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val progress = predictiveBackProgress.value
                        val edgeDirection = when (predictiveBackEdge) {
                            BackEventCompat.EDGE_RIGHT -> -1f
                            else -> 1f
                        }
                        translationX = edgeDirection * size.width * 0.08f * progress
                        scaleX = 1f - (0.04f * progress)
                        scaleY = 1f - (0.04f * progress)
                        alpha = 1f - (0.08f * progress)
                        clip = progress > 0f
                        if (clip) {
                            shape = RoundedCornerShape(28.dp * progress)
                        }
                    }
            ) {
                // Single‑pane (original behaviour)
                AnimatedContent(
                    targetState = baseScreen,
                    transitionSpec = { Motion.screenTransition(initialState, targetState, motionScheme) },
                    label = "screen_transition",
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (screen) {
                            Screen.Library -> {
                                LibraryScreen(
                                    viewModel,
                                    windowSizeClass = windowSizeClass,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@AnimatedContent,
                                    onAddPhotos = { tab ->
                                        if (tab == "Library") {
                                            viewModel.pendingFavoriteIntent = false
                                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        } else if (tab == "Favorites") {
                                            viewModel.startCollectionAssignment("Favorites")
                                        } else {
                                            viewModel.startCollectionAssignment(tab)
                                        }
                                    }
                                )
                            }
                            Screen.Memory -> MemoryScreen(viewModel, sharedTransitionScope = this@SharedTransitionLayout, animatedVisibilityScope = this@AnimatedContent)
                            Screen.About -> AboutScreen(viewModel)
                            Screen.Settings -> SettingsScreen(viewModel)
                            Screen.SelectIcon -> SelectIconScreen(viewModel)
                            Screen.PhotosCarousel -> PhotosCarouselScreen(viewModel)
                            else -> Box(Modifier.fillMaxSize())
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showDetailOverlay,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 250)),
                    exit = slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 250)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    DetailScreen(
                        viewModel = viewModel,
                        windowSizeClass = windowSizeClass,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                snackbar = { snackbarData ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value != SwipeToDismissBoxValue.Settled) {
                                snackbarData.dismiss()
                                  true
                            } else {
                                false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                        content = {
                            Snackbar(snackbarData = snackbarData)
                        }
                    )
                }
            )
        }
    }

    PredictiveBackHandler(enabled = predictiveBackEnabled) { backEvents ->
        var completed = false
        try {
            backEvents.collect { backEvent ->
                predictiveBackEdge = backEvent.swipeEdge
                predictiveBackProgress.snapTo(backEvent.progress)
            }
            completed = true
            predictiveBackProgress.snapTo(1f)
            viewModel.navigateBack()
        } catch (e: CancellationException) {
            predictiveBackProgress.animateTo(0f, motionScheme.fastEffectsSpec())
            throw e
        } finally {
            if (completed) {
                predictiveBackProgress.snapTo(0f)
            }
        }
    }

    if (viewModel.showBulkDeleteModal) {
        DeleteConfirmationModal(
            count = viewModel.selectedPhotoIds.size,
            onDismiss = { viewModel.showBulkDeleteModal = false },
            onConfirm = {
                viewModel.showBulkDeleteModal = false
                viewModel.deleteSelectedPhotos(unpublish = false)
            }
        )
    }

    // Global Busy Overlays
    if (viewModel.isAddingPhotos) {
        com.android.snippets.ui.components.BusyOverlay()
    }
    
    if (viewModel.isBusy) {
        com.android.snippets.ui.components.BusyOverlay()
    }

    if (viewModel.isCuratingMemories) {
        com.android.snippets.ui.components.BusyOverlay()
    }

    if (viewModel.showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeFilter() },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            scrimColor = BottomSheetDefaults.ScrimColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                FilterScreen(viewModel)
            }
        }
    }

    if (viewModel.showCreateDialog) {
        var newCollectionName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.showCreateDialog = false },
            title = { Text("Create collection") },
            text = {
                OutlinedTextField(
                    value = newCollectionName,
                    onValueChange = { newCollectionName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCollectionName.isNotBlank()) {
                        viewModel.createCollection(newCollectionName.trim(), openAfterCreate = false)
                    }
                    viewModel.showCreateDialog = false
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
