package com.android.snippets.ui

import kotlinx.coroutines.launch

import androidx.activity.BackEventCompat
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.android.snippets.viewmodel.Screen
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.ui.components.BulkEditSnippetsDialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetsApp(viewModel: SnippetsViewModel, windowSizeClass: WindowSizeClass) {
    val view = androidx.compose.ui.platform.LocalView.current
    val scope = rememberCoroutineScope()

    var pendingCollectionForPicker by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val pendingColl = pendingCollectionForPicker
            if (pendingColl != null && pendingColl != "Library" && pendingColl != "Favorites") {
                viewModel.addPhotoToCollection(it, pendingColl)
            } else {
                viewModel.addPhoto(it, isFavorite = viewModel.pendingFavoriteIntent)
            }
            pendingCollectionForPicker = null
        }
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
    val predictiveBackMotionController = remember {
        PredictiveBackMotionController(
            progress = predictiveBackProgress,
            onEdgeChanged = { edge -> predictiveBackEdge = edge }
        )
    }
    val predictiveBackEnabled = viewModel.currentScreen != Screen.Library
    var isNavigatingBackFromGesture by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.currentScreen) {
        if (isNavigatingBackFromGesture) {
            isNavigatingBackFromGesture = false
        }
    }

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

    LaunchedEffect(viewModel.pendingAddPhotoIntentToken) {
        if (viewModel.pendingAddPhotoIntentToken != 0L) {
            viewModel.pendingFavoriteIntent = false
            photoPickerLauncher.launch("image/*")
            viewModel.pendingAddPhotoIntentToken = 0L
        }
    }

    CompositionLocalProvider(
        LocalPredictiveBackMotionController provides predictiveBackMotionController
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )

        @OptIn(ExperimentalSharedTransitionApi::class)
        SharedTransitionLayout {
            val showDetail = viewModel.currentScreen == Screen.Detail
            val isGestureActive = predictiveBackProgress.value > 0f

            val backScreen = if (isGestureActive) {
                when (viewModel.currentScreen) {
                    Screen.Memory, Screen.Settings, Screen.About, Screen.Stats, Screen.Templates -> Screen.Library
                    Screen.SelectIcon, Screen.ChooseShape -> viewModel.previousScreen
                    Screen.PhotosCarousel -> Screen.Settings
                    Screen.Detail -> viewModel.detailReturnScreen
                    else -> null
                }
            } else {
                null
            }

            @Composable
            fun RenderScreenContent(
                screen: Screen,
                animatedVisibilityScope: AnimatedVisibilityScope?
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (screen) {
                        Screen.Library -> {
                            LibraryScreen(
                                viewModel = viewModel,
                                windowSizeClass = windowSizeClass,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = animatedVisibilityScope,
                                onAddPhotos = { tab ->
                                    when (tab) {
                                        "Library" -> {
                                            viewModel.pendingFavoriteIntent = false
                                            pendingCollectionForPicker = null
                                            photoPickerLauncher.launch("image/*")
                                        }
                                        "Favorites" -> viewModel.startCollectionAssignment("Favorites")
                                        "Eatlist" -> {
                                            pendingCollectionForPicker = "Eatlist"
                                            photoPickerLauncher.launch("image/*")
                                        }
                                        else -> viewModel.startCollectionAssignment(tab)
                                    }
                                }
                            )
                        }
                        Screen.Memory -> MemoryScreen(
                            viewModel = viewModel,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        Screen.About -> AboutScreen(viewModel)
                        Screen.Settings -> SettingsScreen(viewModel)
                        Screen.SelectIcon -> SelectIconScreen(viewModel)
                        Screen.ChooseShape -> ChooseShapeScreen(viewModel)
                        Screen.PhotosCarousel -> PhotosCarouselScreen(viewModel)
                        Screen.Stats -> StatsScreen(viewModel)
                        Screen.Templates -> TemplatesScreen(viewModel)
                        else -> Box(Modifier.fillMaxSize())
                    }
                }
            }

            // 1. Underlay container (only rendered when backScreen is not null)
            if (backScreen != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val progress = predictiveBackProgress.value
                            val scale = 0.96f + (0.04f * progress)
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    RenderScreenContent(backScreen, null)
                }
            }

            // 2. Foreground container (scales/translates during the gesture)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (isGestureActive) {
                            val progress = predictiveBackProgress.value
                            val edgeDirection = when (predictiveBackEdge) {
                                BackEventCompat.EDGE_RIGHT -> -1f
                                else -> 1f
                            }
                            translationX = edgeDirection * size.width * 0.12f * progress
                            scaleX = 1f - (0.08f * progress)
                            scaleY = 1f - (0.08f * progress)
                            clip = true
                            shape = RoundedCornerShape(32.dp * progress)
                        }
                    }
            ) {
                // Unified AnimatedContent targeting viewModel.currentScreen directly
                AnimatedContent(
                    targetState = viewModel.currentScreen,
                    transitionSpec = {
                        if (isNavigatingBackFromGesture) {
                            fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                        } else {
                            Motion.screenTransition(initialState, targetState, motionScheme)
                        }
                    },
                    label = "screen_transition",
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    if (screen == Screen.Detail) {
                        DetailScreen(
                            viewModel = viewModel,
                            windowSizeClass = windowSizeClass,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@AnimatedContent
                        )
                    } else {
                        RenderScreenContent(screen, this@AnimatedContent)
                    }
                }
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

        AppPredictiveBackHandler(enabled = predictiveBackEnabled) {
            viewModel.navigateBack()
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

        if (viewModel.showBulkEditSnippetsDialog) {
            BulkEditSnippetsDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showBulkEditSnippetsDialog = false }
            )
        }
    }
