package com.android.snippets.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.android.snippets.ui.util.rotateWithBounds
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import kotlin.math.*

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
    var showShareSheet by remember { mutableStateOf(false) }

    val uiAlpha by animateFloatAsState(
        targetValue = if (pagerState.isScrollInProgress) 0f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "uiAlpha"
    )

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().graphicsLayer { 
                // Hardware accelerate the pager for smoother swiping and transitions
                clip = true
            }
        ) { pageIndex ->
            val photo = photoList.getOrNull(pageIndex - 1)
            if (photo == null) {
                Box(modifier = Modifier.fillMaxSize())
                return@VerticalPager
            }
            
            val dateString = remember(photo.date) {
                SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(photo.date))
            }

            Box(modifier = Modifier.fillMaxSize()) {

                // Main Canvas (Box Layout)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Central Photo Core with Overlaid Snippets
                    val isPhotoPage = pageIndex >= 1 && pageIndex <= photoList.size
                    val canSwipeDownOnImageToClose = isPhotoPage
                    
                    Card(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            viewModel.openDetail(photo.id)
                        },
                        shape = RectangleShape,
                        border = null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxSize()
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
                        var isPressed by remember { mutableStateOf(false) }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                                            isPressed = event.changes.any { it.pressed }
                                        }
                                    }
                                }
                        ) {
                            var isZoomingIn by remember { mutableStateOf(true) }
                            val scaleAnim = remember { Animatable(1f) }

                            LaunchedEffect(isPressed) {
                                if (!isPressed) {
                                    while (true) {
                                        val target = if (isZoomingIn) 1.1f else 1f
                                        val duration = (kotlin.math.abs(target - scaleAnim.value) / 0.1f * 8000).toInt()
                                        if (duration > 0) {
                                            scaleAnim.animateTo(
                                                targetValue = target,
                                                animationSpec = tween(durationMillis = duration, easing = LinearEasing)
                                            )
                                        }
                                        isZoomingIn = !isZoomingIn
                                    }
                                } else {
                                    scaleAnim.stop()
                                }
                            }
                            val scale = scaleAnim.value

                            AsyncImage(
                                model = photo.uriString,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                alignment = Alignment { size, space, _ ->
                                    val x = ((space.width - size.width) * 0.5f).toInt().coerceIn(
                                        (space.width - size.width).coerceAtMost(0),
                                        (space.width - size.width).coerceAtLeast(0)
                                    )
                                    val y = ((space.height - size.height) * 0.5f).toInt().coerceIn(
                                        (space.height - size.height).coerceAtMost(0),
                                        (space.height - size.height).coerceAtLeast(0)
                                    )
                                    androidx.compose.ui.unit.IntOffset(x, y)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                            )
                            
                            // Dark Scrim at bottom to make snippets readable
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.5f)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent, 
                                                Color.Black.copy(alpha = 0.6f),
                                                Color.Black.copy(alpha = 0.9f)
                                            )
                                        )
                                    )
                            )

                            // Snippets Cloud overlaid at the bottom
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp, top = 24.dp),
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


            }
        }

        // Top Navigation (Date Header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .alpha(uiAlpha), // Apply fade animation
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val photoList = viewModel.activeMemoriesSnapshot.ifEmpty { viewModel.curatedMemories }
                val pageIndex = viewModel.currentMemoryIndex
                val photo = photoList.getOrNull(pageIndex)
                val dateString = if (photo != null) {
                    SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(photo.date))
                } else ""

                Text(
                    text = dateString.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

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
    val isFilled = (personality % 2 == 0)

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

    val containerColor = if (isFilled) snippetColor.copy(alpha = 0.10f) else Color.Transparent
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
            Text(
                text = text,
                style = textStyle,
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
