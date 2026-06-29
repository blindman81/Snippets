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
import com.android.snippets.ui.shapes.LocalAppShape
import com.android.snippets.ui.shapes.LocalAppShapeType
import com.android.snippets.ui.shapes.toComposeShape
import com.android.snippets.ui.shapes.AppShape
import com.android.snippets.ui.shapes.PolygonDrawable
import com.android.snippets.ui.shapes.getNormalizedPolygon
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
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import kotlinx.coroutines.delay



class MainActivity : ComponentActivity() {
    private val viewModel: SnippetsViewModel by viewModels()
    private var pendingNotificationPhotoId by mutableStateOf<String?>(null)
    private var pendingNotificationToken by mutableStateOf(0L)

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

        


        handleNotificationIntent(intent)
        handleTileIntent(intent)

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
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalAppShape provides viewModel.selectedShape.toComposeShape(),
                    LocalAppShapeType provides viewModel.selectedShape
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
                                    val index = viewModel.curatedMemories.indexOfFirst { it.id == targetPhotoId }
                                    
                                    if (index != -1) {
                                        viewModel.openMemory(index)
                                    } else if (viewModel.curatedMemories.isNotEmpty()) {
                                        viewModel.openMemory(0)
                                    }
                                    
                                    pendingNotificationToken = 0L
                                    pendingNotificationPhotoId = null
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
                                    shapeType = viewModel.selectedShape,
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
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
        handleTileIntent(intent)
    }
}

private val AnticipateEasing = Easing { fraction ->
    android.view.animation.AnticipateInterpolator().getInterpolation(fraction)
}

@Composable
private fun ComposeSplashScreen(
    shapeType: AppShape,
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

    val infiniteTransition = rememberInfiniteTransition(label = "SplashInfinite")
    val infiniteSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "infiniteSpin"
    )
    val infiniteSine by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "infiniteSine"
    )

    val duration = 700
    val animProgress = animateFloatAsState(
        targetValue = if (startExitAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = duration,
            easing = AnticipateEasing
        ),
        finishedListener = {
            if (it == 1f) {
                onAnimationFinished()
            }
        }
    )

    val bgAlpha by animateFloatAsState(
        targetValue = if (startExitAnimation) 0f else 1f,
        animationSpec = tween(durationMillis = duration)
    )

    if (bgAlpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = bgAlpha }
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            val shape = shapeType.toComposeShape()
            val p = animProgress.value

            var scaleX = 1f
            var scaleY = 1f
            var rotationZ = 0f
            var translationX = 0f

            val idleSine = kotlin.math.sin(infiniteSine.toDouble()).toFloat()

            when (shapeType) {
                AppShape.COOKIE_12_SIDED, AppShape.VERY_SUNNY, AppShape.PILL -> {
                    scaleX = 1f - p
                    scaleY = 1f - p
                    rotationZ = infiniteSpin + p * 360f
                }
                AppShape.COOKIE_4_SIDED -> {
                    val idleStamp = 1f + 0.08f * idleSine
                    val exitStamp = if (startExitAnimation) {
                        if (p < 0.2f) {
                            1f - (p / 0.2f) * 0.35f
                        } else if (p < 0.5f) {
                            0.65f + ((p - 0.2f) / 0.3f) * 0.55f
                        } else {
                            1.2f - ((p - 0.5f) / 0.5f) * 1.2f
                        }
                    } else {
                        1f
                    }
                    scaleX = idleStamp * exitStamp
                    scaleY = idleStamp * exitStamp
                }
                AppShape.GEM, AppShape.SQUARE -> {
                    scaleX = 1f - p
                    scaleY = 1f - p
                    val idleSway = idleSine * 12f
                    val exitSway = (kotlin.math.sin(p.toDouble() * java.lang.Math.PI * 3.0) * 20.0).toFloat()
                    rotationZ = idleSway * (1f - p) + exitSway * p
                }
                AppShape.PENTAGON -> {
                    val idlePulse = 1f + 0.06f * idleSine
                    val exitPulseScale = if (startExitAnimation) {
                        if (p < 0.25f) {
                            1f + (p / 0.25f) * 0.3f
                        } else if (p < 0.5f) {
                            1.3f - ((p - 0.25f) / 0.25f) * 0.6f
                        } else if (p < 0.75f) {
                            0.7f + ((p - 0.5f) / 0.25f) * 0.4f
                        } else {
                            1.1f - ((p - 0.75f) / 0.25f) * 1.1f
                        }
                    } else {
                        1f
                    }
                    scaleX = idlePulse * exitPulseScale
                    scaleY = idlePulse * exitPulseScale
                }
                AppShape.CLOVER_4_LEAF -> {
                    val idleScaleX = 1f + 0.06f * idleSine
                    val idleScaleY = 1f - 0.06f * idleSine
                    if (startExitAnimation) {
                        val exitScaleX = if (p < 0.25f) {
                            1f + (p / 0.25f) * 0.3f
                        } else if (p < 0.5f) {
                            1.3f - ((p - 0.25f) / 0.25f) * 0.6f
                        } else if (p < 0.75f) {
                            0.7f + ((p - 0.5f) / 0.25f) * 0.4f
                        } else {
                            1.1f - ((p - 0.75f) / 0.25f) * 1.1f
                        }
                        val exitScaleY = if (p < 0.25f) {
                            1f - (p / 0.25f) * 0.3f
                        } else if (p < 0.5f) {
                            0.7f + ((p - 0.25f) / 0.25f) * 0.6f
                        } else if (p < 0.75f) {
                            1.3f - ((p - 0.5f) / 0.25f) * 0.4f
                        } else {
                            0.9f - ((p - 0.75f) / 0.25f) * 0.9f
                        }
                        scaleX = idleScaleX * exitScaleX
                        scaleY = idleScaleY * exitScaleY
                    } else {
                        scaleX = idleScaleX
                        scaleY = idleScaleY
                    }
                }
                AppShape.CLOVER_8_LEAF -> {
                    scaleX = 1f - p
                    scaleY = 1f - p
                    val idleTranslationX = idleSine * 20f
                    val exitTranslationX = (kotlin.math.sin(p.toDouble() * java.lang.Math.PI * 3.0) * 100.0).toFloat()
                    translationX = idleTranslationX * (1f - p) + exitTranslationX * p
                }
            }

            Box(
                modifier = Modifier
                    .size(108.dp)
                    .graphicsLayer {
                        this.scaleX = scaleX
                        this.scaleY = scaleY
                        this.rotationZ = rotationZ
                        this.translationX = translationX
                    }
                    .background(color = iconColor, shape = shape)
            )
        }
    }
}


