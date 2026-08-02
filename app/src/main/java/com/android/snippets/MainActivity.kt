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
import com.android.snippets.viewmodel.SnippetsViewModel
import androidx.compose.runtime.getValue

import com.ln.android.snippets.R
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
import androidx.compose.ui.res.colorResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.android.snippets.ui.components.LoadingIndicator
import kotlinx.coroutines.delay



class MainActivity : ComponentActivity() {
    private val viewModel: SnippetsViewModel by viewModels()
    private var pendingNotificationPhotoId by mutableStateOf<String?>(null)
    private var pendingNotificationToken by mutableStateOf(0L)
    private var pendingSharedImageUri by mutableStateOf<android.net.Uri?>(null)
    private var pendingSharedText by mutableStateOf<String?>(null)
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
            intent.removeExtra("open_memory")
        }
    }

    private fun handleTileIntent(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra("open_add_photo", false) == true || intent?.action == "com.android.snippets.action.ADD_PHOTO") {
            viewModel.pendingAddPhotoIntentToken = System.currentTimeMillis()
            intent.removeExtra("open_add_photo")
        }
    }

    private fun handleShareIntent(intent: android.content.Intent?) {
        if (intent?.action == android.content.Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM) as? android.net.Uri
            }
            val rawText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
                ?: intent.getStringExtra(android.content.Intent.EXTRA_SUBJECT)
                ?: intent.getCharSequenceExtra(android.content.Intent.EXTRA_TEXT)?.toString()
            val extractedUrl = extractUrlOnly(rawText)
            if (uri != null) {
                pendingSharedImageUri = uri
                pendingSharedText = extractedUrl
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.requestNotificationPermission = false
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
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
        }

        setContent {
            val isDarkTheme = when (viewModel.themePreference) {
                com.android.snippets.viewmodel.ThemePreference.LIGHT -> false
                com.android.snippets.viewmodel.ThemePreference.DARK -> true
                com.android.snippets.viewmodel.ThemePreference.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            val view = androidx.compose.ui.platform.LocalView.current
            val activity = view.context as androidx.activity.ComponentActivity
            
            val currentScreen = viewModel.currentScreen
            androidx.compose.runtime.SideEffect {
                val window = activity.window
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                if (currentScreen != com.android.snippets.viewmodel.Screen.Memory) {
                    insetsController.isAppearanceLightStatusBars = !isDarkTheme
                }
                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
            }
            
            var showSplashOverlay by remember { mutableStateOf(true) }

            SnippetsTheme(
                darkTheme = isDarkTheme,
                dynamicColor = viewModel.useDynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val photos = viewModel.photos
                            val targetPhotoId = pendingNotificationPhotoId
                            val notificationToken = pendingNotificationToken
                            androidx.compose.runtime.LaunchedEffect(notificationToken, photos) {
                                if (photos.isNotEmpty() && notificationToken != 0L) {
                                    if (targetPhotoId != null) {
                                        viewModel.openMemoryForPhoto(targetPhotoId)
                                    } else if (viewModel.curatedMemories.isNotEmpty()) {
                                        viewModel.openMemory(0)
                                    }
                                    
                                    pendingNotificationToken = 0L
                                    pendingNotificationPhotoId = null
                                }
                            }

                            val shareToken = pendingShareToken
                            val sharedUri = pendingSharedImageUri
                            val sharedLink = pendingSharedText
                            androidx.compose.runtime.LaunchedEffect(shareToken, viewModel.isInitialLoading) {
                                if (!viewModel.isInitialLoading && shareToken != 0L && sharedUri != null) {
                                    viewModel.updateShowEatlist(true)
                                    viewModel.addPhotoToCollection(sharedUri, "Eatlist", locationLink = sharedLink)
                                    viewModel.currentScreen = com.android.snippets.viewmodel.Screen.Library
                                    viewModel.libraryCurrentTab = "Eatlist"
                                    
                                    pendingShareToken = 0L
                                    pendingSharedImageUri = null
                                    pendingSharedText = null
                                }
                            }

                            val historyToken = pendingHistoryIntentToken
                            androidx.compose.runtime.LaunchedEffect(historyToken, viewModel.isInitialLoading) {
                                if (!viewModel.isInitialLoading && historyToken != 0L) {
                                    viewModel.currentScreen = com.android.snippets.viewmodel.Screen.Library
                                    viewModel.showHistoryBottomSheet = true
                                    pendingHistoryIntentToken = 0L
                                }
                            }

                            val requestPermission = viewModel.requestNotificationPermission
                            androidx.compose.runtime.LaunchedEffect(requestPermission) {
                                if (requestPermission) {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        val permissionState = ContextCompat.checkSelfPermission(
                                            this@MainActivity,
                                            android.Manifest.permission.POST_NOTIFICATIONS
                                        )
                                        if (permissionState != PackageManager.PERMISSION_GRANTED) {
                                            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            viewModel.requestNotificationPermission = false
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
                                    isInitialLoading = viewModel.isInitialLoading,
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
    }
}



@Composable
private fun ComposeSplashScreen(
    isDarkTheme: Boolean,
    isInitialLoading: Boolean,
    onAnimationFinished: () -> Unit
) {
    val bgColor = if (isDarkTheme) Color(0xFF42474E) else Color(0xFFF2E0D1)
    val iconColor = colorResource(id = R.color.splash_cookie_color)

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
            LoadingIndicator(
                color = iconColor
            )
        }
    }
}


