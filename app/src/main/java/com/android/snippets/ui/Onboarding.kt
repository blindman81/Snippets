package com.android.snippets.ui

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.ln.android.snippets.R
import com.android.snippets.ui.components.Cookiebutton
import com.android.snippets.ui.components.CookieButtonGroup
import com.android.snippets.ui.components.cookieButtonItem
import com.android.snippets.ui.shapes.CookieHoldMorphs
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.shapes.MorphSequenceShape
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.ui.theme.SnippetsTheme
import com.android.snippets.ui.theme.configurePredictiveBackTransition
import com.android.snippets.ui.util.Motion
import com.android.snippets.viewmodel.SettingsViewModel
import com.android.snippets.viewmodel.ThemePreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        configurePredictiveBackTransition()

        setContent {
            val isDarkTheme = when (viewModel.themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            SnippetsTheme(
                darkTheme = isDarkTheme,
                dynamicColor = viewModel.useDynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    OnboardingScreen(onFinish = { finish() })
                }
            }
        }
    }
}

private enum class OnboardingPage {
    WELCOME,
    COOKIE_BUTTON,
    BUTTON_GROUP,
    GO_TO_LIBRARY
}

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }

    var currentPage by remember { mutableStateOf(OnboardingPage.WELCOME) }
    // Sequential fade: outgoing page fades out completely (350ms) BEFORE incoming page fades in (400ms)
    // to prevent cookies from different pages overlapping on top of each other.
    val pageFadeOutSpec = remember {
        tween<Float>(durationMillis = 350, easing = FastOutSlowInEasing)
    }
    val pageFadeInSpec = remember {
        tween<Float>(durationMillis = 400, delayMillis = 350, easing = FastOutSlowInEasing)
    }

    // 90 degree back-and-forth slow rotation for background cut-off cookies (can be stopped before exit)
    val bgRotation = remember { Animatable(0f) }
    var isStoppingBgRotation by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isStoppingBgRotation, lifecycleOwner) {
        if (!isStoppingBgRotation) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    bgRotation.animateTo(
                        targetValue = 90f,
                        animationSpec = tween(durationMillis = 8000, easing = FastOutSlowInEasing)
                    )
                    bgRotation.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 8000, easing = FastOutSlowInEasing)
                    )
                }
            }
        } else {
            bgRotation.stop()
        }
    }

    val baseTextStyle = remember {
        TextStyle(
            fontFamily = GoogleSansFlexRound,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val bigCookieSize = 320.dp
        val cornerCookieSize = 240.dp
        val cookieColor = MaterialTheme.colorScheme.surfaceContainerHighest

        // Top-left cut-off 9-sided cookie (centered on top-left corner)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = -cornerCookieSize / 2, y = -cornerCookieSize / 2)
                .size(cornerCookieSize)
                .graphicsLayer {
                    rotationZ = bgRotation.value
                    shape = CookieShape
                    clip = true
                    alpha = 0.7f
                }
                .background(cookieColor)
        )

        // Top-right cut-off 9-sided cookie (flush with top edge, cut off on right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = bigCookieSize * 0.38f, y = 0.dp)
                .size(bigCookieSize)
                .graphicsLayer {
                    rotationZ = bgRotation.value
                    shape = CookieShape
                    clip = true
                    alpha = 0.7f
                }
                .background(cookieColor)
        )

        // Bottom-left cut-off 9-sided cookie (flush with bottom edge, cut off on left extending to 50% width)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = -bigCookieSize * 0.44f, y = 0.dp)
                .size(bigCookieSize)
                .graphicsLayer {
                    rotationZ = -bgRotation.value
                    shape = CookieShape
                    clip = true
                    alpha = 0.7f
                }
                .background(cookieColor)
        )

        // Bottom-right cut-off 9-sided cookie (centered on bottom-right corner)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = cornerCookieSize / 2, y = cornerCookieSize / 2)
                .size(cornerCookieSize)
                .graphicsLayer {
                    rotationZ = -bgRotation.value
                    shape = CookieShape
                    clip = true
                    alpha = 0.7f
                }
                .background(cookieColor)
        )

        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    fadeIn(animationSpec = pageFadeInSpec) togetherWith fadeOut(animationSpec = pageFadeOutSpec)
                },
                contentAlignment = Alignment.Center,
                label = "onboarding_content_transition",
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    OnboardingPage.WELCOME -> {
                        WelcomePage(
                            textStyle = baseTextStyle,
                            onNext = { currentPage = OnboardingPage.COOKIE_BUTTON }
                        )
                    }
                    OnboardingPage.COOKIE_BUTTON -> {
                        CookiebuttonPage(
                            baseTextStyle = baseTextStyle,
                            onNext = { currentPage = OnboardingPage.BUTTON_GROUP }
                        )
                    }
                    OnboardingPage.BUTTON_GROUP -> {
                        ButtonGroupPage(
                            baseTextStyle = baseTextStyle,
                            onNext = { currentPage = OnboardingPage.GO_TO_LIBRARY }
                        )
                    }
                    OnboardingPage.GO_TO_LIBRARY -> {
                        GoToLibraryPage(
                            baseTextStyle = baseTextStyle,
                            onStopBackgroundRotation = { isStoppingBgRotation = true },
                            onFinish = onFinish
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomePage(
    textStyle: TextStyle,
    onNext: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var textStage by remember { mutableIntStateOf(0) }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            textStage = 0
            delay(350L)
            textStage = 1
            delay(3600L)
            textStage = 0
            delay(500L)
            onNext()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = textStage,
            transitionSpec = {
                (fadeIn(
                    animationSpec = tween(durationMillis = 600, delayMillis = 150, easing = FastOutSlowInEasing)
                ) togetherWith fadeOut(
                    animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
                )).using(null)
            },
            contentAlignment = Alignment.Center,
            label = "welcome_text_fade",
            modifier = Modifier.fillMaxWidth()
        ) { stage ->
            if (stage == 1) {
                Text(
                    text = "Welcome",
                    style = textStyle.copy(
                        fontSize = 28.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CookiebuttonPage(
    baseTextStyle: TextStyle,
    onNext: () -> Unit
) {
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var textStage by remember { mutableIntStateOf(0) }

    // Colors: matches exact in-app Cookiebutton primaryAlpha + lerp behavior
    var isPrimary by remember { mutableStateOf(false) }

    val primaryAlpha by animateFloatAsState(
        targetValue = if (isPrimary) 1f else 0f,
        animationSpec = if (isPrimary) {
            MaterialTheme.motionScheme.fastEffectsSpec()
        } else {
            tween(durationMillis = 500, easing = FastOutSlowInEasing)
        },
        label = "cookie_demo_primary_alpha"
    )

    val animatedContainerColor = lerp(
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.primary,
        primaryAlpha
    )
    val animatedContentColor = lerp(
        MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.onPrimary,
        primaryAlpha
    )

    // Spin animation
    val rotation = remember { Animatable(0f) }

    // Exact in-app shape morph state
    var morphStepIndex by remember { mutableIntStateOf(0) }
    val stepProgress = remember { Animatable(0f) }
    val globalRotation = remember { Animatable(0f) }
    val settleRotation = remember { Animatable(0f) }
    var isSettling by remember { mutableStateOf(false) }
    var isHolding by remember { mutableStateOf(false) }
    var isHoldCompleted by remember { mutableStateOf(false) }
    val animScaleX = remember { Animatable(1f) }
    val animScaleY = remember { Animatable(1f) }
    val animIconScale = remember { Animatable(1f) }

    var hasMorphed by remember { mutableStateOf(false) }

    val currentShape = if (isHolding || isSettling) {
        val safeIndex = if (isSettling && isHoldCompleted) {
            CookieHoldMorphs.size - 1
        } else {
            morphStepIndex
        }
        val safeFraction = if (isSettling && isHoldCompleted) 1f else stepProgress.value
        val totalRotation = if (isSettling) {
            settleRotation.value
        } else {
            (morphStepIndex * 90f) + (stepProgress.value * 90f) + globalRotation.value
        }
        MorphSequenceShape(
            morphs = CookieHoldMorphs,
            morphIndex = safeIndex,
            fraction = safeFraction,
            rotation = totalRotation
        )
    } else if (hasMorphed) {
        MorphSequenceShape(
            morphs = CookieHoldMorphs,
            morphIndex = CookieHoldMorphs.size - 1,
            fraction = 1f,
            rotation = settleRotation.value
        )
    } else {
        MorphSequenceShape(
            morphs = CookieHoldMorphs,
            morphIndex = 0,
            fraction = 0f,
            rotation = 0f
        )
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // Reset state to clean beginning of demonstration whenever resumed
            textStage = 0
            isPrimary = false
            isHolding = false
            isHoldCompleted = false
            isSettling = false
            hasMorphed = false
            morphStepIndex = 0
            rotation.snapTo(0f)
            settleRotation.snapTo(0f)
            animScaleX.snapTo(1f)
            animScaleY.snapTo(1f)
            animIconScale.snapTo(1f)
            stepProgress.snapTo(0f)
            globalRotation.snapTo(0f)

            // Wait for page fade-in to complete
            delay(800L)
            // Part 1: "In Snippets,"
            textStage = 1

        // 3 second gap between lines in the sentence
        delay(3000L)

        // Part 2: "buttons use the Material 3 shape 9 sided cookie."
        textStage = 2

        // 5 seconds wait before next action
        delay(5000L)

        // Part 3: "Tap to make them turn primary and spin."
        textStage = 3

        // 3 second gap after text before spin and new color
        delay(3000L)

        // Material You primary (in-app fastEffectsSpec transition)
        isPrimary = true
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        launch {
            try {
                animScaleX.animateTo(0.84f, Motion.PressSpring)
                animScaleX.animateTo(1f, Motion.BouncySpring)
            } finally {
                withContext(NonCancellable) {
                    if (animScaleX.value != 1f) {
                        animScaleX.animateTo(1f, Motion.BouncySpring)
                    }
                }
            }
        }
        launch {
            try {
                animScaleY.animateTo(0.84f, Motion.PressSpring)
                animScaleY.animateTo(1f, Motion.BouncySpring)
            } finally {
                withContext(NonCancellable) {
                    if (animScaleY.value != 1f) {
                        animScaleY.animateTo(1f, Motion.BouncySpring)
                    }
                }
            }
        }
        // Execute 360 spin
        rotation.animateTo(
            targetValue = rotation.value + 360f,
            animationSpec = Motion.BouncySpring
        )

        // Keep the primary color while "Tap to make them turn primary and spin." is displayed
        delay(4000L)

        // Cookie turns back to container color in exact sync with text fading out
        isPrimary = false

        // Part 4: "Hold them to morph the cookie into other Material 3 shapes."
        textStage = 4

        // 3 second gap after text before hold-morph starts
        delay(3000L)

        // In-app exact shape morph animation execution
        // Turns primary color while held
        isPrimary = true
        isHolding = true
        isHoldCompleted = false
        isSettling = false
        morphStepIndex = 0
        stepProgress.snapTo(0f)
        globalRotation.snapTo(0f)

        val holdScale = 1.45f
        launch { animScaleX.animateTo(holdScale, animationSpec = Motion.BouncySpring) }
        launch { animScaleY.animateTo(holdScale, animationSpec = Motion.BouncySpring) }
        launch { animIconScale.animateTo(0.5f, animationSpec = Motion.BouncySpring) }

        val globalRotationJob = launch {
            globalRotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4666, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }

        val morphAnimationSpec = spring<Float>(
            dampingRatio = 0.6f,
            stiffness = 200f,
            visibilityThreshold = 0.1f
        )
        val totalSteps = CookieHoldMorphs.size

        for (step in 0 until totalSteps) {
            morphStepIndex = step
            stepProgress.snapTo(0f)
            view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)

            val deferred = async {
                stepProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = morphAnimationSpec
                )
            }
            delay(650L)
            deferred.await()
        }

        globalRotationJob.cancel()

        // Settle rotation and spring back scale (exact hold release behavior)
        val currentRot = (morphStepIndex * 90f) + (stepProgress.value * 90f) + globalRotation.value
        val targetRot = kotlin.math.round(currentRot / 360f) * 360f
        settleRotation.snapTo(currentRot)
        isSettling = true
        isHoldCompleted = true

        // Loose primary AS it is going back to cookie9sided (exact app behavior)
        isPrimary = false

        launch { animIconScale.animateTo(1f, animationSpec = Motion.BouncySpring) }
        launch { animScaleX.animateTo(1f, animationSpec = Motion.BouncySpring) }
        launch { animScaleY.animateTo(1f, animationSpec = Motion.BouncySpring) }

        settleRotation.animateTo(targetRot, animationSpec = Motion.BouncySpring)
        hasMorphed = true
        isSettling = false
        isHolding = false
        isHoldCompleted = false

        // 4 seconds gap before transitioning to next page
        delay(4000L)
        onNext()
        }
    }

    val mediumTextStyle = baseTextStyle.copy(
        fontSize = 18.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // XL Cookie in the middle center (136dp)
        Box(
            modifier = Modifier
                .size(136.dp)
                .graphicsLayer {
                    rotationZ = rotation.value
                    scaleX = animScaleX.value
                    scaleY = animScaleY.value
                    clip = false
                }
                .clip(currentShape)
                .background(animatedContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Cookie,
                contentDescription = null,
                tint = animatedContentColor,
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer {
                        rotationZ = -rotation.value
                        scaleX = animIconScale.value
                        scaleY = animIconScale.value
                    }
            )
        }

        Spacer(modifier = Modifier.height(44.dp))

        // Text in the middle center below the cookie, fading in and out at the exact same place
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = textStage,
                transitionSpec = {
                    (fadeIn(
                        animationSpec = tween(durationMillis = 500, delayMillis = 200, easing = FastOutSlowInEasing)
                    ) togetherWith fadeOut(
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    )).using(null)
                },
                contentAlignment = Alignment.Center,
                label = "cookie_page_text_crossfade",
                modifier = Modifier.fillMaxWidth()
            ) { stage ->
                val text = when (stage) {
                    1 -> "In Snippets,"
                    2 -> "buttons use the Material 3 shape 9 sided cookie."
                    3 -> "Tap to make them turn primary and spin."
                    4 -> "Hold them to morph the cookie into other Material 3 shapes."
                    else -> ""
                }
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = mediumTextStyle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonGroupPage(
    baseTextStyle: TextStyle,
    onNext: () -> Unit
) {
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var textStage by remember { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var simulatedHeldIndex by remember { mutableIntStateOf(-1) }

    val libraryIcon = ImageVector.vectorResource(R.drawable.ic_collections_bookmark_filled)
    val items = remember(libraryIcon) {
        listOf(
            cookieButtonItem(
                icon = libraryIcon,
                contentDescription = "Library"
            ),
            cookieButtonItem(
                icon = Icons.Rounded.Favorite,
                contentDescription = "Favorite"
            ),
            cookieButtonItem(
                icon = Icons.Rounded.Cookie,
                contentDescription = "Cookie"
            )
        )
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            textStage = 0
            selectedIndex = 0
            simulatedHeldIndex = -1

            // Wait for page fade-in to complete
            delay(800L)
            selectedIndex = 0
            // Part 1: "This is a cookie button group."
            textStage = 1

        // 5 seconds wait
        delay(5000L)

        // Part 2: "Hold a button to make itself and the adjacent button morph into a circle."
        textStage = 2

        // 3 second gap after text before hold demonstration
        delay(3500L)

        // Simulate holding button 2: button 2 gains primary, button 2 & adjacent button 1 morph into circles
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        simulatedHeldIndex = 2
        delay(3000L)
        // Release hold: morphs back and loses primary
        simulatedHeldIndex = -1
        delay(3000L)

        // Part 3: "Tap a button to make the previous button wiggle and lose primary."
        textStage = 3

        // Clear 4.5 second gap (full 3.5s+ of readable text after fade-in) before next text
        delay(4500L)

        // Part 4: "The selected button turns primary and spins."
        textStage = 4

        // 3.5 second gap after text appears before showing the action
        delay(3500L)

        // Switch to the third button (index 2):
        // Confirmation haptic feedback, button 2 squishes and spins 360°, button 0 loses primary and wiggles
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        selectedIndex = 2

        // 4 seconds gap after action before advancing to Page 3 (text and button group fade out in sync)
        delay(4000L)
        onNext()
        }
    }

    val mediumTextStyle = baseTextStyle.copy(
        fontSize = 18.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Button group in the center
        CookieButtonGroup(
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = it },
            items = items,
            cookieSize = 64.dp,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedContentColor = MaterialTheme.colorScheme.onPrimary,
            unselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            enableTilt = false,
            heldIndex = simulatedHeldIndex
        )

        Spacer(modifier = Modifier.height(44.dp))

        // Text in the middle center below the button group, fading in and out at the exact same place
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = textStage,
                transitionSpec = {
                    (fadeIn(
                        animationSpec = tween(durationMillis = 500, delayMillis = 200, easing = FastOutSlowInEasing)
                    ) togetherWith fadeOut(
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    )).using(null)
                },
                contentAlignment = Alignment.Center,
                label = "button_group_text_crossfade",
                modifier = Modifier.fillMaxWidth()
            ) { stage ->
                val text = when (stage) {
                    1 -> "This is a cookie button group."
                    2 -> "Hold a button to make itself and the adjacent button morph into a circle."
                    3 -> "Tap a button to make the previous button wiggle and lose primary."
                    4 -> "The selected button turns primary and spins."
                    else -> ""
                }
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = mediumTextStyle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun GoToLibraryPage(
    baseTextStyle: TextStyle,
    onStopBackgroundRotation: () -> Unit,
    onFinish: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var textVisible by remember { mutableStateOf(false) }
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            textVisible = false
            // Wait for page fade-in to complete before fading in text
            delay(800L)
            textVisible = true
        }
    }

    val mediumTextStyle = baseTextStyle.copy(
        fontSize = 18.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Cookiebutton(
            onClick = {
                onStopBackgroundRotation()
                onFinish()
            },
            icon = Icons.Rounded.ChevronRight,
            contentDescription = "Go to Library",
            size = 136.dp,
            iconSize = 68.dp,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            enableTilt = false
        )

        Spacer(modifier = Modifier.height(44.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = textVisible,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 150, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing))
                },
                contentAlignment = Alignment.Center,
                label = "go_to_library_text_fade",
                modifier = Modifier.fillMaxWidth()
            ) { visible ->
                if (visible) {
                    Text(
                        text = "Go to Library.",
                        style = mediumTextStyle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
