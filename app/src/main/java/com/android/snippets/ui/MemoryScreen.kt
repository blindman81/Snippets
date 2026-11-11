package com.android.snippets.ui

import android.content.Intent
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.blur.BlurRadiusSpec
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.allowHardware
import com.android.snippets.logic.MemoryWorker
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.theme.*
import com.android.snippets.ui.util.Motion
import com.android.snippets.ui.util.rotateWithBounds
import com.android.snippets.viewmodel.MemoryViewModel
import com.ln.android.snippets.R
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import kotlin.random.Random


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context as? androidx.activity.ComponentActivity
    val backDispatcher = androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val photo = viewModel.currentPhoto

    if (photo == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp
            )
        }
        return
    }

    val isDarkTheme = when (viewModel.themePreference) {
        com.android.snippets.viewmodel.ThemePreference.LIGHT -> false
        com.android.snippets.viewmodel.ThemePreference.DARK -> true
        com.android.snippets.viewmodel.ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

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

    DisposableEffect(photo.id) {
        MemoryWorker.isInMemoryScreen = true
        MemoryWorker.cancelSummaryNotification(context)
        onDispose {
            MemoryWorker.isInMemoryScreen = false
            MemoryWorker.cancelSummaryNotification(context)
        }
    }

    // State for enter transition settling so rotation/timer wait until open
    var isOpening by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(400)
        isOpening = false
    }

    val isOpeningTransition = isOpening

    val uiAlpha by animateFloatAsState(
        targetValue = if (isOpeningTransition) 0f else 1f,
        animationSpec = Motion.StandardFade,
        label = "uiAlpha"
    )

    // State to ensure memory is viewed once on screen for 3 seconds
    var isMarkedViewed by remember(photo.id) { mutableStateOf(photo.isViewed) }

    LaunchedEffect(photo.id, isOpeningTransition) {
        if (!isOpeningTransition && !isMarkedViewed) {
            delay(3000)
            isMarkedViewed = true
            viewModel.onMemoryViewed(photo.id)
        }
    }

    // State for long-press hold detection to freeze spin & pause timer
    var isPressed by remember { mutableStateOf(false) }
    val shouldAnimateSpin = !isPressed && !isOpeningTransition

    var rotationAngle by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(shouldAnimateSpin) {
        if (shouldAnimateSpin) {
            var lastTime = withFrameNanos { it }
            while (true) {
                withFrameNanos { frameTime ->
                    val deltaNano = frameTime - lastTime
                    lastTime = frameTime
                    val deltaMs = deltaNano / 1_000_000f

                    val deltaDegrees = deltaMs * (360f / 60000f)
                    rotationAngle = (rotationAngle + deltaDegrees) % 360f
                }
            }
        }
    }

    // Story Progress Timer (10 seconds total for this individual memory)
    var currentMemoryProgress by remember { mutableFloatStateOf(0f) }
    var wavePhase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(photo.id, isOpeningTransition) {
        if (!isOpeningTransition) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            currentMemoryProgress = 0f

            var lastTime = 0L
            while (currentMemoryProgress < 1f && !isOpeningTransition) {
                withFrameNanos { frameTime ->
                    if (!isPressed && !isOpeningTransition) {
                        if (lastTime != 0L) {
                            val deltaNano = frameTime - lastTime
                            val deltaSec = (deltaNano / 1_000_000_000f).coerceAtMost(0.05f)
                            currentMemoryProgress = (currentMemoryProgress + deltaSec / 10f).coerceAtMost(1f)
                            wavePhase = (wavePhase + deltaSec * 8f) % (2f * PI.toFloat())
                        }
                    }
                    lastTime = frameTime
                }
                if (!isMarkedViewed && currentMemoryProgress >= 0.3f) {
                    isMarkedViewed = true
                    viewModel.onMemoryViewed(photo.id)
                }
            }
            if (currentMemoryProgress >= 1f && !isOpeningTransition) {
                if (!isMarkedViewed) {
                    isMarkedViewed = true
                    viewModel.onMemoryViewed(photo.id)
                }
                backDispatcher?.onBackPressed() ?: activity?.onBackPressedDispatcher?.onBackPressed() ?: activity?.finish()
            }
        }
    }

    val hazeState = rememberHazeState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clipToBounds()
            .graphicsLayer { clip = true }
            .pointerInput(photo.id) {
                detectTapGestures(
                    onPress = { offset ->
                        isPressed = true
                        val released = tryAwaitRelease()
                        isPressed = false
                        if (released) {
                            if (offset.x < size.width * 0.3f) {
                                if (currentMemoryProgress > 0.15f) {
                                    currentMemoryProgress = 0f
                                } else {
                                    backDispatcher?.onBackPressed() ?: activity?.onBackPressedDispatcher?.onBackPressed() ?: activity?.finish()
                                }
                            } else {
                                backDispatcher?.onBackPressed() ?: activity?.onBackPressedDispatcher?.onBackPressed() ?: activity?.finish()
                            }
                        }
                    },
                    onLongPress = {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                )
            }
    ) {
        // Blurred background photo with hazeSource
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .graphicsLayer { clip = true }
                .hazeSource(state = hazeState)
        ) {
            AsyncImage(
                model = photo.uriString,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.08f
                        scaleY = 1.08f
                    }
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Modifier.blur(
                                BlurRadiusSpec.verticalGradient(
                                    startRadius = 4.dp,
                                    endRadius = 24.dp
                                ),
                                edgeTreatment = BlurredEdgeTreatment.Rectangle
                            )
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Modifier.blur(32.dp, edgeTreatment = BlurredEdgeTreatment.Rectangle)
                        } else {
                            Modifier
                        }
                    )
            )

            // Dim/Tint overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Spacer to clear the status bar / date header (progress bar + rating icon + date row ≈ 100dp)
            Spacer(modifier = Modifier.height(140.dp))

            val pageRotation = if (!isOpeningTransition) rotationAngle else 0f

            Card(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    val intent = Intent(context, com.android.snippets.MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("open_photo_id", photo.id)
                    }
                    context.startActivity(intent)
                    activity?.finish()
                },
                shape = CookieShape,
                border = BorderStroke(4.dp, Color.White),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        rotationZ = pageRotation
                    }
                    .clip(CookieShape)
            ) {
                val imageRequest = remember(photo.uriString) {
                    ImageRequest.Builder(context)
                        .data(photo.uriString)
                        .crossfade(false)
                        .allowHardware(false)
                        .memoryCacheKey(photo.uriString)
                        .build()
                }
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = -pageRotation
                            scaleX = 1.25f
                            scaleY = 1.25f
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                ) {
                    photo.snippets.forEachIndexed { index, snippet ->
                        FloatingSnippet(
                            text = snippet,
                            index = index,
                            total = photo.snippets.size,
                            photoColors = emptyList(),
                            screenWidth = cloudWidth,
                            screenHeight = cloudHeight,
                            hazeState = hazeState
                        )
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
                .zIndex(1f)
        ) {
            // M3 Wavy / Flat Progress Bar across full memory duration width
            MemoryStoryProgressBar(
                progress = currentMemoryProgress,
                wavePhase = wavePhase,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .padding(bottom = 8.dp)
            )

            if (photo.rating > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 6.dp)
                        .size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star_rating),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = photo.rating.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(0xFF1F1F1F),
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }

            // Header Row: Label icon & Photo Name (Left) | Calendar icon & Date/Time (Center) | Location icon & Location (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
                val timeFormat = if (is24Hour) "HH:mm" else "h:mm a"
                val pattern = "EEE, d MMM • $timeFormat"
                val dateString = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(photo.date))
                val locationText = com.android.snippets.ui.util.LocationUtils.getLocationFromExif(context, photo)
                val photoName = photo.name?.takeIf { it.isNotBlank() }

                // Left: Label icon and Photo name (2dp gap from calendar pill)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (photoName != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .padding(end = 2.dp)
                                .heightIn(min = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 7.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Label,
                                    contentDescription = "Photo name",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                WrappingPillText(
                                    text = photoName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 14.sp
                                    ),
                                    color = Color.White,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Center: Calendar month icon and day, date month and time in pill
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    modifier = Modifier.height(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Date",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = dateString.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                // Right: Location on icon to show location (2dp gap from calendar pill)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (!locationText.isNullOrBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .heightIn(min = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 7.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                WrappingPillText(
                                    text = locationText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 14.sp
                                    ),
                                    color = Color.White,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WrappingPillText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    maxLines: Int,
    overflow: TextOverflow,
    modifier: Modifier = Modifier
) {
    var textWidthDp by remember(text) { mutableStateOf<androidx.compose.ui.unit.Dp?>(null) }
    val density = LocalDensity.current

    Text(
        text = text,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = { layoutResult ->
            val maxLineWidth = (0 until layoutResult.lineCount).maxOfOrNull { line ->
                layoutResult.getLineRight(line) - layoutResult.getLineLeft(line)
            } ?: 0f
            if (maxLineWidth > 0f) {
                val calculatedDp = with(density) { (ceil(maxLineWidth).toInt() + 1).toDp() }
                if (textWidthDp != calculatedDp) {
                    textWidthDp = calculatedDp
                }
            }
        },
        modifier = if (textWidthDp != null) {
            modifier.width(textWidthDp!!)
        } else {
            modifier
        }
    )
}

@Composable
fun MemoryStoryProgressBar(
    progress: Float,
    wavePhase: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val cornerRadius = height / 2f

        val activeWidth = width * progress.coerceIn(0f, 1f)
        val remainingWidth = width - activeWidth

        // Draw background track for remaining upcoming portion
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
            val wavelengthPx = 40.dp.toPx()
            val numPoints = (activeWidth / 2.dp.toPx()).toInt().coerceIn(30, 300)
            for (step in 0..numPoints) {
                val x = (activeWidth * step) / numPoints
                val wave = sin((x / wavelengthPx) * 2 * PI + wavePhase).toFloat()
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

        // 4dp circle stop indicator at the end of the memory's progress bar (at the right edge)
        val dotColor = if (progress >= 1f) Color.White else Color.White.copy(alpha = 0.6f)
        drawCircle(
            color = dotColor,
            radius = 2.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(width - 2.dp.toPx(), centerY)
        )
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
    MemoryStoryProgressBar(
        progress = currentProgress,
        wavePhase = wavePhase,
        modifier = modifier
    )
}

@Composable
fun FloatingSnippet(
    text: String, 
    index: Int, 
    total: Int, 
    photoColors: List<Int> = emptyList(),
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp,
    hazeState: dev.chrisbanes.haze.HazeState? = null
) {
    // 1. Unified Scaling/Style Logic
    val scalingFactor = com.android.snippets.ui.util.DistributionMath.getCloudScalingFactor(total)

    val stableRandom = remember(text) { Random(text.hashCode().toLong()) }
    val personality = stableRandom.nextInt(0, 5)

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
    val cloudLetterSpacing = 0.sp
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
    val textStyle = com.android.snippets.ui.theme.getSnippetTextStyle(
        base = baseStyle,
        isCloud = true
    )

    val snippetColor = rememberSnippetBackgroundColor()
    val rotation = (stableRandom.nextFloat() * 16 - 8)

    val hazeStyle = remember(snippetColor) {
        dev.chrisbanes.haze.HazeStyle(
            tint = dev.chrisbanes.haze.HazeTint(snippetColor.copy(alpha = 0.50f)),
            blurRadius = 24.dp,
            noiseFactor = dev.chrisbanes.haze.HazeDefaults.noiseFactor
        )
    }

    Box(modifier = Modifier.rotateWithBounds(rotation)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .widthIn(max = maxSnippetWidth)
                .sizeIn(minWidth = 1.dp, minHeight = 1.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .then(
                    if (hazeState != null) {
                        Modifier.hazeEffect(
                            state = hazeState,
                            style = hazeStyle
                        )
                    } else Modifier
                )
                .background(snippetColor.copy(alpha = 0.50f))
        ) {
            Text(
                text = text,
                style = textStyle.copy(
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                color = rememberSnippetTextColor(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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