package com.android.snippets.ui

import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.android.snippets.ui.shapes.LocalAppShape
import com.android.snippets.ui.shapes.LocalAppShapeType
import com.android.snippets.ui.shapes.AppShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.drawable.BitmapDrawable
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import com.android.snippets.ui.util.rotateWithBounds
import com.android.snippets.viewmodel.Screen
import com.android.snippets.viewmodel.SnippetsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import kotlin.random.Random


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: SnippetsViewModel,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    BackHandler {
        viewModel.navigateLibrary()
    }
    val photoList = viewModel.activeMemoriesSnapshot.ifEmpty { viewModel.curatedMemories }
    val pagerState = rememberPagerState(initialPage = viewModel.currentMemoryIndex + 1) {
        photoList.size + 2 // [Close, Photos..., Close]
    }
    val transitionTargetId by remember { derivedStateOf { photoList.getOrNull(pagerState.currentPage - 1)?.id } }
    
    // Sync pager state back to ViewModel and handle swipe-to-close
    LaunchedEffect(pagerState.targetPage) {
        if (pagerState.targetPage == 0 || pagerState.targetPage == photoList.size + 1) {
            // Immediate transition trigger
            viewModel.navigateLibrary()
            return@LaunchedEffect
        }
        viewModel.onMemoryViewed(pagerState.targetPage - 1)
    }

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val view = LocalView.current
    val activity = view.context as? androidx.activity.ComponentActivity
    
    val isDarkTheme = when (viewModel.themePreference) {
        com.android.snippets.viewmodel.ThemePreference.LIGHT -> false
        com.android.snippets.viewmodel.ThemePreference.DARK -> true
        com.android.snippets.viewmodel.ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    val photoLightnessMap = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(isDarkTheme) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightStatusBars = false
        }
    }

    DisposableEffect(isDarkTheme) {
        onDispose {
            activity?.window?.let { window ->
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !isDarkTheme
            }
        }
    }

    var showShareSheet by remember { mutableStateOf(false) }

    val uiAlpha by animateFloatAsState(
        targetValue = if (pagerState.isScrollInProgress) 0f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "uiAlpha"
    )

    // State for long-press hold detection to freeze spin & pause timer
    var isPressed by remember { mutableStateOf(false) }

    val shapeType = LocalAppShapeType.current
    val isSpinningShape = when (shapeType) {
        AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> true
        else -> false
    }

    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var swayAngle by remember { mutableFloatStateOf(0f) }
    var pulseScale by remember { mutableFloatStateOf(1f) }
    var translationXOffset by remember { mutableFloatStateOf(0f) }
    var translationYOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPressed) {
        if (!isPressed) {
            var lastTime = withFrameNanos { it }
            var accumulatedTime = 0f
            while (true) {
                withFrameNanos { frameTime ->
                    val deltaNano = frameTime - lastTime
                    lastTime = frameTime
                    val deltaMs = deltaNano / 1_000_000f
                    
                    if (isSpinningShape) {
                        val deltaDegrees = deltaMs * (360f / 60000f)
                        rotationAngle = (rotationAngle + deltaDegrees) % 360f
                    } else {
                        accumulatedTime += deltaMs
                        val fraction = accumulatedTime * (2f * kotlin.math.PI.toFloat() / 4000f)
                        
                        when (shapeType) {
                            AppShape.GEM, AppShape.SQUARE -> {
                                val swayDegrees = 3f * kotlin.math.sin(fraction)
                                swayAngle = swayDegrees
                            }
                            AppShape.PENTAGON -> {
                                pulseScale = 1f + 0.04f * kotlin.math.sin(fraction)
                            }
                            AppShape.CLOVER_4_LEAF -> {
                                translationYOffset = 25f * kotlin.math.sin(fraction)
                            }
                            AppShape.CLOVER_8_LEAF -> {
                                translationXOffset = 25f * kotlin.math.sin(fraction)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    // Story Progress Timer (10 seconds total per memory)
    var currentMemoryProgress by remember { mutableFloatStateOf(0f) }
    var wavePhase by remember { mutableFloatStateOf(0f) }

    // Story Progress Timer and Auto-Advance Loop bound to settledPage to prevent mid-animation cancellation
    LaunchedEffect(pagerState.settledPage, isPressed) {
        val activeSettled = pagerState.settledPage
        if (activeSettled >= 1 && activeSettled <= photoList.size) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            currentMemoryProgress = 0f

            var lastTime = withFrameNanos { it }
            while (currentMemoryProgress < 1f) {
                withFrameNanos { frameTime ->
                    if (!isPressed) {
                        val deltaNano = frameTime - lastTime
                        val deltaSec = deltaNano / 1_000_000_000f
                        currentMemoryProgress = (currentMemoryProgress + deltaSec / 10f).coerceAtMost(1f)
                        wavePhase = (wavePhase + deltaSec * 8f) % (2f * PI.toFloat())
                    }
                    lastTime = frameTime
                }
            }
            if (currentMemoryProgress >= 1f) {
                snapshotFlow { pagerState.isScrollInProgress }
                    .first { !it }
                coroutineScope.launch {
                    pagerState.animateScrollToPage(activeSettled + 1)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { clip = true }
        ) { pageIndex ->
            val photo = photoList.getOrNull(pageIndex - 1)
            if (photo == null) {
                Box(modifier = Modifier.fillMaxSize())
                return@HorizontalPager
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                isPressed = true
                                val released = tryAwaitRelease()
                                isPressed = false
                                if (released) {
                                    if (offset.x < size.width * 0.3f) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.settledPage - 1)
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.settledPage + 1)
                                        }
                                    }
                                }
                            },
                            onLongPress = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            }
                        )
                    }
            ) {
                // Blurred background photo
                AsyncImage(
                    model = photo.uriString,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(32.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                )

                // Dim/Tint overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Spacer to clear the status bar / date header
                    Spacer(modifier = Modifier.height(72.dp))

                    Card(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            viewModel.openDetail(photo.id)
                        },
                        shape = LocalAppShape.current,
                        border = BorderStroke(4.dp, Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .aspectRatio(1f)
                            .graphicsLayer {
                                rotationZ = when (shapeType) {
                                    AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> rotationAngle
                                    AppShape.GEM, AppShape.SQUARE -> swayAngle
                                    else -> 0f
                                }
                                scaleX = if (shapeType == AppShape.PENTAGON) pulseScale else 1f
                                scaleY = if (shapeType == AppShape.PENTAGON) pulseScale else 1f
                                translationX = if (shapeType == AppShape.CLOVER_8_LEAF) translationXOffset else 0f
                                translationY = if (shapeType == AppShape.CLOVER_4_LEAF) translationYOffset else 0f
                            }
                            .then(
                                if (photo.id == transitionTargetId && viewModel.currentScreen != Screen.Detail && sharedTransitionScope != null && animatedVisibilityScope != null) {
                                    with(sharedTransitionScope) {
                                        Modifier.sharedBounds(
                                            rememberSharedContentState(key = "memory_${photo.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            boundsTransform = { _, _ -> spring(dampingRatio = 0.8f, stiffness = 500f) },
                                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.Crop)
                                        )
                                    }
                                } else Modifier
                            )
                    ) {
                        AsyncImage(
                            model = photo.uriString,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            onSuccess = { state ->
                                val drawable = state.result.drawable
                                val bitmap = (drawable as? BitmapDrawable)?.bitmap
                                if (bitmap != null) {
                                    val topHeight = (bitmap.height * 0.1f).toInt().coerceIn(1, bitmap.height)
                                    Palette.from(bitmap)
                                        .setRegion(0, 0, bitmap.width, topHeight)
                                        .generate { palette ->
                                            palette?.let { p ->
                                                val rgb = p.getDominantColor(android.graphics.Color.BLACK)
                                                val isLight = ColorUtils.calculateLuminance(rgb) > 0.5
                                                photoLightnessMap[pageIndex] = isLight
                                            }
                                        }
                                }
                            },
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = when (shapeType) {
                                        AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> -rotationAngle
                                        AppShape.GEM, AppShape.SQUARE -> -swayAngle
                                        else -> 0f
                                    }
                                    scaleX = 1.25f / (if (shapeType == AppShape.PENTAGON) pulseScale else 1f)
                                    scaleY = 1.25f / (if (shapeType == AppShape.PENTAGON) pulseScale else 1f)
                                    translationX = if (shapeType == AppShape.CLOVER_8_LEAF) -translationXOffset else 0f
                                    translationY = if (shapeType == AppShape.CLOVER_4_LEAF) -translationYOffset else 0f
                                }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Snippets Cloud overlaid at the bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 48.dp, top = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val cloudWidth = 360.dp
                        val cloudHeight = 240.dp
                        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                        ) {
                            photo.snippets.forEachIndexed { index, snippet ->
                                FloatingSnippet(
                                    text = snippet,
                                    index = index,
                                    total = photo.snippets.size,
                                    photoColors = emptyList(),
                                    forcedColor = viewModel.getSnippetColor(snippet),
                                    forcedStyle = viewModel.getSnippetStyle(snippet),
                                    screenWidth = cloudWidth,
                                    screenHeight = cloudHeight
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Navigation Header containing M3 Wavy Progress Bar and Date Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .alpha(uiAlpha)
        ) {
            // Segmented M3 Wavy / Flat Progress Bar
            MemoryStoryProgressBar(
                count = photoList.size,
                currentIndex = (pagerState.settledPage - 1).coerceIn(0, photoList.size - 1),
                currentProgress = currentMemoryProgress,
                wavePhase = wavePhase,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .padding(bottom = 8.dp)
            )

            // Date & Location Header Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeIndex = (pagerState.settledPage - 1).coerceIn(0, photoList.size - 1)
                val photo = photoList.getOrNull(activeIndex)
                val dateString = if (photo != null) {
                    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
                    val timeFormat = if (is24Hour) "HH:mm" else "h:mm a"
                    val pattern = if (viewModel.showTimeInMemories) "EEE, d MMM • $timeFormat" else "EEE, d MMM"
                    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(photo.date))
                } else ""
                val locationText = if (photo != null) {
                    com.android.snippets.ui.util.LocationUtils.getLocationFromExif(context, photo)
                } else null

                Text(
                    text = dateString.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                if (!locationText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier.height(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = locationText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryStoryProgressBar(
    count: Int,
    currentIndex: Int,
    currentProgress: Float,
    wavePhase: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until count) {
            val progress = when {
                i < currentIndex -> 1f
                i == currentIndex -> currentProgress
                else -> 0f
            }
            val isActive = (i == currentIndex)

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
            ) {
                val width = size.width
                val height = size.height
                val centerY = height / 2f
                val cornerRadius = height / 2f

                if (!isActive) {
                    // Inactive or completed memory segments
                    val trackColor = if (i < currentIndex) Color.White else Color.White.copy(alpha = 0.35f)
                    drawRoundRect(
                        color = trackColor,
                        size = size,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                } else {
                    // Active memory segment
                    val activeWidth = width * progress
                    val remainingWidth = width - activeWidth

                    // Draw background track ONLY for the remaining upcoming portion so no flat bar appears behind the squiggly wave
                    if (remainingWidth > 0f) {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.35f),
                            topLeft = androidx.compose.ui.geometry.Offset(activeWidth, 0f),
                            size = Size(remainingWidth, height),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                        )
                    }

                    // Active memory progress bar (M3 Expressive specs): 8dp height, 4dp stroke, 40dp wavelength, 3dp amplitude
                    val maxAmplitude = 3.dp.toPx()
                    val amplitude = when {
                        progress <= 0.1f -> 0f
                        progress <= 0.15f -> maxAmplitude * ((progress - 0.1f) / 0.05f)
                        progress <= 0.6f -> maxAmplitude
                        else -> maxAmplitude * (1f - (progress - 0.6f) / 0.4f)
                    }

                    if (amplitude > 0.1f) {
                        val path = Path()
                        val numPoints = 60
                        for (step in 0..numPoints) {
                            val x = (activeWidth * step) / numPoints
                            val wave = sin((x / 40.dp.toPx()) * 2 * PI + wavePhase).toFloat()
                            val y = centerY + wave * amplitude
                            if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = Color.White,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    } else {
                        // Flattened state
                        if (activeWidth > 0f) {
                            drawRoundRect(
                                color = Color.White,
                                size = Size(activeWidth, height),
                                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                            )
                        }
                    }
                }

                // 4dp circle stop indicator at the end of the last memory's progress bar
                if (i == count - 1) {
                    val dotColor = if (i <= currentIndex) Color.White else Color.White.copy(alpha = 0.6f)
                    drawCircle(
                        color = dotColor,
                        radius = 2.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(width - 2.dp.toPx(), centerY)
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingSnippet(
    text: String, 
    index: Int, 
    total: Int, 
    photoColors: List<Int> = emptyList(),
    forcedColor: Int? = null,
    forcedStyle: com.android.snippets.viewmodel.SnippetStyle? = null,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    // 1. Unified Scaling/Style Logic
    val scalingFactor = com.android.snippets.ui.util.DistributionMath.getCloudScalingFactor(total)

    val stableRandom = remember(text) { Random(text.hashCode().toLong()) }
    val personality = stableRandom.nextInt(0, 5)
    val isFilled = true

    val baseFontSize = when(personality) {
        0, 1 -> 24 // Large
        2, 3 -> 18 // Medium
        else -> 14 // Small
    }
    
    val edgePadding = 24.dp
    val rotationGutter = 18.dp
    val maxSnippetWidth = (screenWidth - (edgePadding * 2) - rotationGutter).coerceAtLeast(96.dp)
    val density = LocalDensity.current
    
    // Improved width estimation for emojis/emoticons/special chars
    val emojiCount = text.count { it.code > 0x2000 }
    val specialCount = text.count { it in "-_!?.()[]{}<>:;" }
    val normalCount = text.length - emojiCount - specialCount
    val cloudLetterSpacing = if (forcedStyle == com.android.snippets.viewmodel.SnippetStyle.Spaced) 1.5.sp else 0.sp
    val estimatedSpacingWidth = with(density) {
        if (text.length > 1) (cloudLetterSpacing * (text.length - 1)).toDp() else 0.dp
    }
    
    val estimatedPillWidth = with(density) {
        (
            (normalCount * baseFontSize * 0.6f) + 
            (emojiCount * baseFontSize * 1.5f) + 
            (specialCount * baseFontSize * 0.6f) + 
            (baseFontSize * 2.5f) // Padding constant
        ).sp.toDp()
    } * scalingFactor + (estimatedSpacingWidth * scalingFactor)
    
    val finalScaling = if (estimatedPillWidth.value > maxSnippetWidth.value) {
        scalingFactor * (maxSnippetWidth.value / estimatedPillWidth.value)
    } else {
        scalingFactor
    }
    val fittedPillWidth = estimatedPillWidth.coerceAtMost(maxSnippetWidth)

    val baseStyle = when(personality) {
        0, 1 -> MaterialTheme.typography.headlineMedium.copy(fontSize = (24 * finalScaling).sp, fontWeight = FontWeight.Bold)
        2, 3 -> MaterialTheme.typography.titleLarge.copy(fontSize = (18 * finalScaling).sp, fontWeight = FontWeight.Bold)
        else -> MaterialTheme.typography.labelLarge.copy(fontSize = (14 * finalScaling).sp, fontWeight = FontWeight.Normal)
    }
    val textStyle = com.android.snippets.ui.getSnippetTextStyle(
        forcedStyle ?: com.android.snippets.viewmodel.SnippetStyle.Default,
        baseStyle,
        isCloud = true
    )

    val colorStrategy = (index + stableRandom.nextInt(0, 10)) % 3
    val colorInt = if (forcedColor != null) {
        forcedColor
    } else {
        when (colorStrategy) {
            0 -> if (photoColors.isNotEmpty()) photoColors[stableRandom.nextInt(photoColors.size)] else MaterialTheme.colorScheme.primary.toArgb()
            1 -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)[stableRandom.nextInt(3)].toArgb()
            else -> listOf(Color(0xFFFF5722), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00BCD4))[stableRandom.nextInt(4)].toArgb()
        }
    }
    val snippetColor = Color(colorInt)
    val rotation = (stableRandom.nextFloat() * 16 - 8)

    val containerColor = if (isFilled) snippetColor.copy(alpha = 0.25f) else Color.Transparent
    val borderColor = if (!isFilled) snippetColor.copy(alpha = 0.40f) else null

    Box(modifier = Modifier.rotateWithBounds(rotation)) {
        Surface(
            color = containerColor,
            contentColor = snippetColor,
            shape = androidx.compose.foundation.shape.CircleShape,
            border = if (borderColor != null) BorderStroke(1.5.dp, borderColor) else null,
            modifier = Modifier
                .widthIn(max = maxSnippetWidth)
                .sizeIn(minWidth = 1.dp, minHeight = 1.dp)
        ) {
            val snippetGradient = remember(snippetColor) {
                Brush.linearGradient(colors = listOf(snippetColor, snippetColor.copy(alpha = 0.55f)))
            }
            Text(
                text = text,
                style = textStyle.copy(brush = snippetGradient),
                modifier = Modifier.padding(
                    horizontal = (when (personality) {
                        0, 1 -> 24
                        2, 3 -> 18
                        else -> 12
                    } * finalScaling.coerceAtMost(1.5f)).dp,
                    vertical = (when (personality) {
                        0, 1 -> 12
                        2, 3 -> 9
                        else -> 6
                    } * finalScaling.coerceAtMost(1.5f)).dp
                )
            )
        }
    }
}
