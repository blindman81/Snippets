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
            val iconView = try { splashScreenView.iconView } catch (e: Exception) { null }
            
            val animators = mutableListOf<android.animation.Animator>()
            
            if (iconView != null) {
                val shapeType = viewModel.selectedShape
                
                // Immediately set the custom shape drawable on the iconView
                if (iconView is android.widget.ImageView) {
                    val fillColor = ContextCompat.getColor(this, R.color.splash_cookie_color)
                    val drawable = PolygonDrawable(shapeType.getNormalizedPolygon(), fillColor)
                    iconView.setImageDrawable(drawable)
                }

                when (shapeType) {
                    AppShape.COOKIE_12_SIDED, AppShape.VERY_SUNNY, AppShape.PILL -> {
                        // Spin exit animation: zoom/scale down + rotation
                        val zoomX = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.SCALE_X,
                            1f, 0f
                        )
                        val zoomY = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.SCALE_Y,
                            1f, 0f
                        )
                        val rotation = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.ROTATION,
                            0f, 360f
                        )
                        animators.addAll(listOf(zoomX, zoomY, rotation))
                    }
                    AppShape.GEM, AppShape.SQUARE -> {
                        // Sway exit animation: zoom/scale down + sway rotation
                        val zoomX = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.SCALE_X,
                            1f, 0f
                        )
                        val zoomY = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.SCALE_Y,
                            1f, 0f
                        )
                        val rotation = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.ROTATION,
                            0f, -20f, 20f, -10f, 10f, 0f
                        )
                        animators.addAll(listOf(zoomX, zoomY, rotation))
                    }
                    AppShape.PENTAGON -> {
                        // Pulse exit animation: scale/zoom pulse down to 0
                        val zoomX = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.SCALE_X,
                            1f, 1.3f, 0.7f, 1.1f, 0f
                        )
                        val zoomY = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.SCALE_Y,
                            1f, 1.3f, 0.7f, 1.1f, 0f
                        )
                        animators.addAll(listOf(zoomX, zoomY))
                    }
                    AppShape.CLOVER_4_LEAF -> {
                        // Squash & Stretch bounce exit animation: scaleX/scaleY animated out of phase
                        val zoomX = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.SCALE_X,
                            1f, 1.3f, 0.7f, 1.1f, 0f
                        )
                        val zoomY = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.SCALE_Y,
                            1f, 0.7f, 1.3f, 0.9f, 0f
                        )
                        animators.addAll(listOf(zoomX, zoomY))
                    }
                    AppShape.CLOVER_8_LEAF -> {
                        // Translate/Sway exit animation: horizontal translation sway + scale down
                        val density = resources.displayMetrics.density
                        val swayDist = 40f * density
                        val translateX = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.TRANSLATION_X,
                            0f, -swayDist, swayDist, -swayDist / 2f, swayDist / 2f, 0f
                        )
                        val zoomX = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.SCALE_X,
                            1f, 0f
                        )
                        val zoomY = android.animation.ObjectAnimator.ofFloat(
                            iconView,
                            android.view.View.SCALE_Y,
                            1f, 0f
                        )
                        animators.addAll(listOf(translateX, zoomX, zoomY))
                    }
                }
            }
            
            val alpha = android.animation.ObjectAnimator.ofFloat(
                splashScreenView.view,
                android.view.View.ALPHA,
                1f, 0f
            )
            animators.add(alpha)
            
            val animatorSet = android.animation.AnimatorSet()
            animatorSet.duration = 700L
            animatorSet.interpolator = android.view.animation.AnticipateInterpolator()
            animatorSet.playTogether(animators)
            
            animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    splashScreenView.remove()
                }
            })
            
            splashScreenView.view.postDelayed({
                animatorSet.start()
            }, 200L)
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
