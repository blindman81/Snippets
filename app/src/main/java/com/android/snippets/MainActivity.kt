package com.android.snippets

import androidx.activity.result.contract.ActivityResultContracts

import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import com.android.snippets.ui.SnippetsApp
import com.android.snippets.ui.theme.SnippetsTheme
import com.android.snippets.ui.theme.configurePredictiveBackTransition
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.SettingsViewModel
import com.android.snippets.viewmodel.MemoryViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.android.snippets.ui.components.LoadingIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay



@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: SnippetsViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val memoryViewModel: MemoryViewModel by viewModels()
    private var pendingNotificationPhotoId by mutableStateOf<String?>(null)
    private var pendingNotificationToken by mutableStateOf(0L)
    private var pendingSharedImageUri by mutableStateOf<android.net.Uri?>(null)
    private var pendingSharedImageUris by mutableStateOf<List<android.net.Uri>>(emptyList())
    private var pendingSharedText by mutableStateOf<String?>(null)
    private var pendingSharedLocationName by mutableStateOf<String?>(null)
    private var pendingShareToken by mutableStateOf(0L)
    private var pendingHistoryIntentToken by mutableStateOf(0L)

    private fun extractUrlOnly(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val urlRegex = """(https?://[^\s]+)""".toRegex()
        return urlRegex.find(text)?.value
    }

    private fun handleNotificationIntent(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra("open_memory", false) == true) {
            pendingNotificationPhotoId = intent.getStringExtra("photo_id")
            pendingNotificationToken = System.currentTimeMillis()
            com.android.snippets.logic.MemoryWorker.cancelSummaryNotification(this)
            intent.removeExtra("open_memory")
            intent.removeExtra("photo_id")
        }
    }

    private fun handleTileIntent(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra("open_add_photo", false) == true || intent?.action == "com.android.snippets.action.ADD_PHOTO") {
            viewModel.pendingAddPhotoIntentToken = System.currentTimeMillis()
            intent.removeExtra("open_add_photo")
        }
    }

    private fun handleShareIntent(intent: android.content.Intent?) {
        val action = intent?.action
        val isImageShare = intent?.type?.startsWith("image/") == true
        if ((action == android.content.Intent.ACTION_SEND || action == android.content.Intent.ACTION_SEND_MULTIPLE) && isImageShare) {
            val uris = when (action) {
                android.content.Intent.ACTION_SEND_MULTIPLE -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableArrayListExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
                    } ?: emptyList()
                }
                else -> {
                    val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM) as? android.net.Uri
                    }
                    if (uri != null) listOf(uri) else emptyList()
                }
            }
            val rawText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
                ?: intent.getStringExtra(android.content.Intent.EXTRA_SUBJECT)
                ?: intent.getCharSequenceExtra(android.content.Intent.EXTRA_TEXT)?.toString()
            val extractedUrl = extractUrlOnly(rawText)
            val extractedPlace = com.android.snippets.ui.util.LocationUtils.extractPlaceNameFromLink(rawText, this)
            if (uris.isNotEmpty()) {
                pendingSharedImageUris = uris
                pendingSharedImageUri = uris.firstOrNull()
                pendingSharedText = extractedUrl ?: rawText
                pendingSharedLocationName = extractedPlace
                pendingShareToken = System.currentTimeMillis()
            }
            intent.action = null
        }
    }

    private fun handleHistoryIntent(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra("open_history", false) == true) {
            if (intent.getBooleanExtra("history_intent_processed", false)) return
            pendingHistoryIntentToken = System.currentTimeMillis()
            intent.putExtra("history_intent_processed", true)
            intent.removeExtra("open_history")
        }
    }

    private fun handleDeepLinkIntent(intent: android.content.Intent?) {
        if (intent?.action == android.content.Intent.ACTION_VIEW) {
            val data = intent.data ?: return
            val host = data.host
            val pathSegments = data.pathSegments

            when {
                (host == "photo" && pathSegments.isNotEmpty()) -> {
                    viewModel.openDetail(pathSegments[0])
                }
                (pathSegments.getOrNull(0) == "photo" && pathSegments.size > 1) -> {
                    viewModel.openDetail(pathSegments[1])
                }
                host == "memory" || pathSegments.getOrNull(0) == "memory" -> {
                    val memoryIntent = android.content.Intent(this, com.android.snippets.ui.MemoryActivity::class.java)
                    startActivity(memoryIntent)
                }
            }
            intent.data = null
        }
    }
    private fun handleOpenPhotoIntent(intent: android.content.Intent?) {
        val photoId = intent?.getStringExtra("open_photo_id")
        if (!photoId.isNullOrBlank()) {
            viewModel.openDetail(photoId)
            intent.removeExtra("open_photo_id")
        }
    }

    private fun handleSearchIntent(intent: android.content.Intent?) {
        val query = intent?.getStringExtra("search_query")
            ?: intent?.getStringExtra(android.app.SearchManager.QUERY)
        if (!query.isNullOrBlank()) {
            viewModel.searchQuery = query
            viewModel.libraryCurrentTab = "Library"
            intent?.removeExtra("search_query")
            intent?.removeExtra(android.app.SearchManager.QUERY)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.requestNotificationPermission = false
        viewModel.markNotificationPermissionPrompted()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        configurePredictiveBackTransition()
        
        splashScreen.setKeepOnScreenCondition {
            viewModel.isInitialLoading
        }
        
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.remove()
        }

        if (savedInstanceState == null) {
            handleNotificationIntent(intent)
            handleTileIntent(intent)
            handleShareIntent(intent)
            handleHistoryIntent(intent)
            handleOpenPhotoIntent(intent)
            handleDeepLinkIntent(intent)
            handleSearchIntent(intent)
        }

        setContent {
            val isDarkTheme = when (settingsViewModel.themePreference) {
                com.android.snippets.viewmodel.ThemePreference.LIGHT -> false
                com.android.snippets.viewmodel.ThemePreference.DARK -> true
                com.android.snippets.viewmodel.ThemePreference.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            var showSplashOverlay by remember { mutableStateOf(true) }

            SnippetsTheme(
                darkTheme = isDarkTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val uiState by viewModel.uiState.collectAsState()
                            val photos = uiState.photos
                            val targetPhotoId = pendingNotificationPhotoId
                            val notificationToken = pendingNotificationToken
                            androidx.compose.runtime.LaunchedEffect(notificationToken, uiState.isInitialLoading, photos) {
                                if (!uiState.isInitialLoading && notificationToken != 0L && photos.isNotEmpty()) {
                                    val intent = android.content.Intent(this@MainActivity, com.android.snippets.ui.MemoryActivity::class.java).apply {
                                        if (targetPhotoId != null) {
                                            putExtra("photo_id", targetPhotoId)
                                        } else if (memoryViewModel.curatedMemories.isNotEmpty()) {
                                            putExtra("memory_index", 0)
                                        } else {
                                            photos.firstOrNull()?.let { putExtra("photo_id", it.id) }
                                        }
                                    }
                                    startActivity(intent)
                                    
                                    pendingNotificationToken = 0L
                                    pendingNotificationPhotoId = null
                                }
                            }

                            val pendingRecap = uiState.pendingRecapLaunch
                            androidx.compose.runtime.LaunchedEffect(pendingRecap) {
                                pendingRecap?.let { recap ->
                                    when (recap) {
                                        is com.android.snippets.viewmodel.SnippetsViewModel.RecapLaunchEvent.Monthly -> {
                                            startActivity(android.content.Intent(this@MainActivity, com.android.snippets.ui.MonthlyRecapActivity::class.java).apply {
                                                putExtra("month", recap.month)
                                                putExtra("year", recap.year)
                                            })
                                        }
                                        is com.android.snippets.viewmodel.SnippetsViewModel.RecapLaunchEvent.Yearly -> {
                                            startActivity(android.content.Intent(this@MainActivity, com.android.snippets.ui.YearlyRecapActivity::class.java).apply {
                                                putExtra("year", recap.year)
                                            })
                                        }
                                    }
                                    viewModel.pendingRecapLaunch = null
                                }
                            }

                            val shareToken = pendingShareToken
                            val sharedUri = pendingSharedImageUri
                            val sharedUris = pendingSharedImageUris
                            val sharedLink = pendingSharedText
                            val sharedName = pendingSharedLocationName
                            androidx.compose.runtime.LaunchedEffect(shareToken, uiState.isInitialLoading) {
                                if (!uiState.isInitialLoading && shareToken != 0L) {
                                    val urisToProcess = when {
                                        sharedUris.isNotEmpty() -> sharedUris
                                        sharedUri != null -> listOf(sharedUri)
                                        else -> emptyList()
                                    }
                                    if (urisToProcess.isNotEmpty()) {
                                        viewModel.addPhotosToLibrary(urisToProcess, locationLink = sharedLink, locationName = sharedName)
                                        viewModel.libraryCurrentTab = "Library"
                                    }
                                    
                                    pendingShareToken = 0L
                                    pendingSharedImageUri = null
                                    pendingSharedImageUris = emptyList()
                                    pendingSharedText = null
                                    pendingSharedLocationName = null
                                }
                            }

                            val historyToken = pendingHistoryIntentToken
                            androidx.compose.runtime.LaunchedEffect(historyToken, uiState.isInitialLoading) {
                                if (!uiState.isInitialLoading && historyToken != 0L) {
                                    viewModel.libraryCurrentTab = "Library"
                                    pendingHistoryIntentToken = 0L
                                }
                            }

                            val hasPromptedPermission = uiState.hasPromptedNotificationPermission
                            androidx.compose.runtime.LaunchedEffect(uiState.isInitialLoading, showSplashOverlay, hasPromptedPermission) {
                                if (!uiState.isInitialLoading && !showSplashOverlay) {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        val permissionGranted = ContextCompat.checkSelfPermission(
                                            this@MainActivity,
                                            android.Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (!permissionGranted && !hasPromptedPermission) {
                                            viewModel.requestNotificationPermission = true
                                        }
                                    }
                                }
                            }

                            val requestPermission = uiState.requestNotificationPermission
                            androidx.compose.runtime.LaunchedEffect(requestPermission) {
                                if (requestPermission) {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        val permissionState = ContextCompat.checkSelfPermission(
                                            this@MainActivity,
                                            android.Manifest.permission.POST_NOTIFICATIONS
                                        )
                                        if (permissionState != PackageManager.PERMISSION_GRANTED) {
                                            viewModel.markNotificationPermissionPrompted()
                                            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            viewModel.requestNotificationPermission = false
                                            viewModel.markNotificationPermissionPrompted()
                                        }
                                    } else {
                                        viewModel.requestNotificationPermission = false
                                    }
                                }
                            }
                            val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
                            SnippetsApp(viewModel, windowSizeClass)

                            if (showSplashOverlay) {
                                ComposeSplashScreen(
                                    isDarkTheme = isDarkTheme,
                                    isInitialLoading = uiState.isInitialLoading,
                                    onAnimationFinished = {
                                        showSplashOverlay = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
        handleTileIntent(intent)
        handleShareIntent(intent)
        handleHistoryIntent(intent)
        handleOpenPhotoIntent(intent)
        handleDeepLinkIntent(intent)
        handleSearchIntent(intent)
    }
}



@Composable
private fun ComposeSplashScreen(
    isDarkTheme: Boolean,
    isInitialLoading: Boolean,
    onAnimationFinished: () -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background

    var startExitAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(isInitialLoading) {
        if (!isInitialLoading) {
            delay(200L)
            startExitAnimation = true
        }
    }

    val duration = 700

    val bgAlpha by animateFloatAsState(
        targetValue = if (startExitAnimation) 0f else 1f,
        animationSpec = tween(durationMillis = duration),
        finishedListener = {
            if (it == 0f) {
                onAnimationFinished()
            }
        }
    )

    if (bgAlpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = bgAlpha }
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    }
}


