package com.android.snippets.ui

import kotlinx.coroutines.launch

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.android.snippets.viewmodel.Screen
import com.android.snippets.viewmodel.SnippetsViewModel
import androidx.compose.foundation.layout.fillMaxHeight
import com.android.snippets.ui.components.BlurredAlertDialog
import com.android.snippets.ui.components.DeleteConfirmationModal
import com.android.snippets.ui.components.LocalHazeState
import com.android.snippets.ui.components.SnippetsSnackbarEffect
import com.android.snippets.ui.components.SnippetsSnackbarHost
import dev.chrisbanes.haze.HazeState

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
import androidx.compose.ui.graphics.TransformOrigin
import com.android.snippets.ui.util.Motion
import com.android.snippets.ui.util.LocalTiltActive
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination.Companion.hasRoute
import com.android.snippets.navigation.LibraryRoute
import com.android.snippets.navigation.MemoryRoute
import com.android.snippets.navigation.SettingsRoute
import com.android.snippets.navigation.AboutRoute
import com.android.snippets.navigation.EmojiPickerRoute
import com.android.snippets.navigation.StatsRoute
import com.android.snippets.navigation.MonthlyRecapRoute
import com.android.snippets.navigation.YearlyRecapRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetsApp(viewModel: SnippetsViewModel, windowSizeClass: WindowSizeClass) {
    val view = androidx.compose.ui.platform.LocalView.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    var pendingCollectionForPicker by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
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
    val isOverlayShowing = viewModel.showBulkDeleteModal || viewModel.showFilterSheet || viewModel.isAddingPhotos || viewModel.isBusy || viewModel.isCuratingMemories

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        currentBackStackEntry?.destination?.let { dest ->
            val screen = when {
                dest.hasRoute<LibraryRoute>() -> Screen.Library
                dest.hasRoute<MemoryRoute>() -> Screen.Memory
                dest.hasRoute<SettingsRoute>() -> Screen.Settings
                dest.hasRoute<AboutRoute>() -> Screen.About
                dest.hasRoute<EmojiPickerRoute>() -> Screen.EmojiPicker
                dest.hasRoute<StatsRoute>() -> Screen.Stats
                dest.hasRoute<MonthlyRecapRoute>() -> Screen.MonthlyRecap
                dest.hasRoute<YearlyRecapRoute>() -> Screen.YearlyRecap
                else -> null
            }
            if (screen != null && viewModel.currentScreen != screen) {
                viewModel.currentScreen = screen
            }
        }
    }

    LaunchedEffect(viewModel.currentScreen) {
        val dest = navController.currentBackStackEntry?.destination
        when (viewModel.currentScreen) {
            Screen.Library -> {
                if (dest?.hasRoute<LibraryRoute>() != true) {
                    if (!navController.popBackStack<LibraryRoute>(inclusive = false)) {
                        navController.navigate(LibraryRoute) {
                            popUpTo<LibraryRoute> { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            }
            Screen.Memory -> {
                if (dest?.hasRoute<MemoryRoute>() != true) {
                    navController.navigate(MemoryRoute()) {
                        launchSingleTop = true
                    }
                }
            }
            Screen.Settings -> {
                if (dest?.hasRoute<SettingsRoute>() != true) {
                    navController.navigate(SettingsRoute) {
                        launchSingleTop = true
                    }
                }
            }
            Screen.About -> {
                if (dest?.hasRoute<AboutRoute>() != true) {
                    navController.navigate(AboutRoute) {
                        launchSingleTop = true
                    }
                }
            }
            Screen.EmojiPicker -> {
                if (dest?.hasRoute<EmojiPickerRoute>() != true) {
                    navController.navigate(EmojiPickerRoute(viewModel.editingCollectionIcon)) {
                        launchSingleTop = true
                    }
                }
            }
            Screen.Stats -> {
                if (dest?.hasRoute<StatsRoute>() != true) {
                    navController.navigate(StatsRoute) {
                        launchSingleTop = true
                    }
                }
            }
            Screen.MonthlyRecap -> {
                if (dest?.hasRoute<MonthlyRecapRoute>() != true) {
                    navController.navigate(MonthlyRecapRoute()) {
                        launchSingleTop = true
                    }
                }
            }
            Screen.YearlyRecap -> {
                if (dest?.hasRoute<YearlyRecapRoute>() != true) {
                    navController.navigate(YearlyRecapRoute()) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    SnippetsSnackbarEffect(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState
    )

    LaunchedEffect(viewModel.pendingAddPhotoIntentToken) {
        if (viewModel.pendingAddPhotoIntentToken != 0L) {
            viewModel.pendingFavoriteIntent = false
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            viewModel.pendingAddPhotoIntentToken = 0L
        }
    }

    val rootHazeState = remember { HazeState() }
    CompositionLocalProvider(
        LocalHazeState provides rootHazeState
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
            val isOverlayShowing = viewModel.showBulkDeleteModal || viewModel.showFilterSheet || viewModel.isAddingPhotos || viewModel.isBusy || viewModel.isCuratingMemories

            @Composable
            fun RenderScreenContent(
                screen: Screen,
                animatedVisibilityScope: AnimatedVisibilityScope?
            ) {
                val isScreenActive = (viewModel.currentScreen == screen) && !isOverlayShowing
                CompositionLocalProvider(LocalTiltActive provides isScreenActive) {
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
                                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                            }
                                            "Favorites" -> viewModel.startCollectionAssignment("Favorites")
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
                            Screen.EmojiPicker -> EmojiPickerScreen(viewModel)
                            Screen.Stats -> StatsScreen(viewModel)
                            Screen.MonthlyRecap, Screen.YearlyRecap -> MonthlyRecapScreen(viewModel = viewModel)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = LibraryRoute,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = {
                        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.92f, animationSpec = tween(350, easing = FastOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 0.95f, animationSpec = tween(250, easing = FastOutSlowInEasing))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.92f, animationSpec = tween(350, easing = FastOutSlowInEasing))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                            scaleOut(
                                targetScale = 0.90f,
                                transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f),
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            )
                    }
                ) {
                    composable<LibraryRoute>(
                        popEnterTransition = {
                            if (initialState.destination.hasRoute<MemoryRoute>()) {
                                fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing))
                            } else {
                                fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                                    scaleIn(initialScale = 0.92f, animationSpec = tween(350, easing = FastOutSlowInEasing))
                            }
                        },
                        exitTransition = {
                            if (targetState.destination.hasRoute<MemoryRoute>()) {
                                fadeOut(animationSpec = tween(380, easing = FastOutSlowInEasing))
                            } else {
                                fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                                    scaleOut(targetScale = 0.95f, animationSpec = tween(250, easing = FastOutSlowInEasing))
                            }
                        }
                    ) {
                        RenderScreenContent(Screen.Library, this)
                    }
                    composable<MemoryRoute>(
                        enterTransition = { fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) },
                        exitTransition = { fadeOut(animationSpec = tween(380, easing = FastOutSlowInEasing)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) },
                        popExitTransition = { fadeOut(animationSpec = tween(380, easing = FastOutSlowInEasing)) }
                    ) {
                        RenderScreenContent(Screen.Memory, this)
                    }
                    composable<SettingsRoute> {
                        RenderScreenContent(Screen.Settings, this)
                    }
                    composable<AboutRoute> {
                        RenderScreenContent(Screen.About, this)
                    }
                    composable<EmojiPickerRoute> {
                        RenderScreenContent(Screen.EmojiPicker, this)
                    }
                    composable<StatsRoute> {
                        RenderScreenContent(Screen.Stats, this)
                    }
                    composable<MonthlyRecapRoute> {
                        RenderScreenContent(Screen.MonthlyRecap, this)
                    }
                    composable<YearlyRecapRoute> {
                        RenderScreenContent(Screen.YearlyRecap, this)
                    }
                }
            }
        }

        SnippetsSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )

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

        }
        }
    }
