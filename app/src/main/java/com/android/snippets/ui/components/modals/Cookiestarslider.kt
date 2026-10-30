package com.android.snippets.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.ui.util.CookieTiltController
import com.android.snippets.ui.util.Motion
import com.ln.android.snippets.R
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Cookiestarslider(
    initialRating: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    hazeState: HazeState? = LocalHazeState.current
) {
    val view = LocalView.current
    var rating by remember { mutableIntStateOf(initialRating.coerceIn(0, 5)) }
    val coroutineScope = rememberCoroutineScope()

    var isDismissing by remember { mutableStateOf(false) }
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val scrimAlpha = remember { Animatable(0f) }

    // 5 Cookiestar spin rotation animatables (cookiestars 1..5)
    val cookiestarRotations = remember {
        List(5) { Animatable(0f) }
    }

    DisposableEffect(Unit) {
        CookieTiltController.beginTiltModalFocus()
        onDispose {
            CookieTiltController.endTiltModalFocus()
        }
    }

    LaunchedEffect(Unit) {
        scrimAlpha.animateTo(1f, animationSpec = tween(durationMillis = 200))
        if (rating > 0) {
            for (i in 0 until rating) {
                launch {
                    cookiestarRotations[i].animateTo(
                        targetValue = 360f,
                        animationSpec = Motion.BouncySpring
                    )
                }
            }
        }
    }

    val closeAndConfirm: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            visibleState.targetState = false
            coroutineScope.launch {
                scrimAlpha.animateTo(0f, animationSpec = tween(durationMillis = 200))
            }
            coroutineScope.launch {
                delay(220)
                onConfirm(rating)
            }
        }
    }

    fun spinCookiestar(cookiestarIndex: Int) {
        if (cookiestarIndex in 1..5) {
            coroutineScope.launch {
                val anim = cookiestarRotations[cookiestarIndex - 1]
                anim.animateTo(
                    targetValue = anim.value + 360f,
                    animationSpec = Motion.BouncySpring
                )
            }
        }
    }

    fun setCookiestars(newRating: Int) {
        val target = newRating.coerceIn(0, 5)
        if (target != rating) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val previousRating = rating
            rating = target
            if (target > previousRating) {
                for (i in (previousRating + 1)..target) {
                    spinCookiestar(i)
                }
            } else if (target > 0) {
                spinCookiestar(target)
            }
        }
    }

    BackHandler(enabled = !isDismissing) { closeAndConfirm() }

    // Fullscreen in-tree overlay spanning true edge-to-edge behind status bar & navigation bar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f),
        contentAlignment = Alignment.Center
    ) {
        // Subtle screen dim - soft dimming rather than a heavy dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = scrimAlpha.value }
                .background(Color.Black.copy(alpha = 0.16f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { closeAndConfirm() }
                )
        )

        AnimatedVisibility(
            visibleState = visibleState,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(180)) + scaleIn(
                initialScale = 0.90f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 220)
            ) + fadeOut(animationSpec = tween(durationMillis = 180)) + scaleOut(
                targetScale = 0.90f,
                animationSpec = tween(durationMillis = 220)
            )
        ) {
            // Pure vertical-oriented slider without any background container/card, with Haze blur
            Box(
                modifier = Modifier
                    .width(108.dp)
                    .height(354.dp)
                    .testTag("orientation")
                    .pointerInput(Unit) {
                        detectTapGestures { /* consume tap inside */ }
                    },
                contentAlignment = Alignment.Center
            ) {
                CookiestarRatingBarVertical(
                    rating = rating,
                    cookiestarRotations = cookiestarRotations,
                    hazeState = hazeState,
                    onRatingChange = { newRating -> setCookiestars(newRating) },
                    onCookiestarClick = { clickedIndex ->
                        if (rating == 1 && clickedIndex == 1) {
                            setCookiestars(0)
                        } else {
                            if (rating != clickedIndex) {
                                setCookiestars(clickedIndex)
                            } else {
                                spinCookiestar(clickedIndex)
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Vertical Cookiestar rating slider with frosted glass Haze blur.
 *
 * Oriented 1 to 5 from bottom to top (1 at the bottom, 5 at the top).
 * Uses 16dp outer corners and 2dp inner corners facing the horizontal drag handle.
 */
@Composable
private fun CookiestarRatingBarVertical(
    rating: Int,
    cookiestarRotations: List<Animatable<Float, AnimationVector1D>>,
    hazeState: HazeState? = LocalHazeState.current,
    onRatingChange: (Int) -> Unit,
    onCookiestarClick: (Int) -> Unit
) {
    val handleThickness = 4.dp
    val handleWidth = 64.dp
    val handleSpacing = 6.dp
    val trackWidth = 52.dp
    val barHeight = 354.dp

    // Unselected track is at the top (5 down to rating + 1)
    val unselectedShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 2.dp,
        bottomEnd = 2.dp
    )

    // Selected track is at the bottom (rating down to 1)
    val selectedShape = RoundedCornerShape(
        topStart = 2.dp,
        topEnd = 2.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )

    fun calculateRatingFromY(y: Float, totalHeight: Float): Int {
        if (totalHeight <= 0f) return 0
        // Measured from bottom (1 at bottom, 5 at top)
        val fractionFromBottom = ((totalHeight - y) / totalHeight).coerceIn(0f, 1f)
        return when {
            fractionFromBottom < 0.10f -> 0
            fractionFromBottom < 0.28f -> 1
            fractionFromBottom < 0.48f -> 2
            fractionFromBottom < 0.68f -> 3
            fractionFromBottom < 0.88f -> 4
            else -> 5
        }
    }

    Box(
        modifier = Modifier
            .width(108.dp)
            .height(barHeight)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onRatingChange(calculateRatingFromY(offset.y, size.height.toFloat()))
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, _ ->
                        change.consume()
                        onRatingChange(calculateRatingFromY(change.position.y, size.height.toFloat()))
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(trackWidth)
                .height(barHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Unselected Segment at the TOP (Cookiestars 5 downTo (rating + 1) + endpoint dot at top)
            if (rating < 5) {
                val unselectedBg = MaterialTheme.colorScheme.secondaryContainer
                val unselectedModifier = if (hazeState != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Modifier
                        .clip(unselectedShape)
                        .background(unselectedBg.copy(alpha = 0.30f))
                        .hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                tint = HazeTint(unselectedBg.copy(alpha = 0.60f)),
                                blurRadius = 24.dp,
                                noiseFactor = HazeDefaults.noiseFactor
                            )
                        )
                } else {
                    Modifier
                        .clip(unselectedShape)
                        .background(unselectedBg)
                }

                Box(
                    modifier = Modifier
                        .weight((5 - rating).toFloat())
                        .width(trackWidth)
                        .then(unselectedModifier),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (index in 5 downTo (rating + 1)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CookiestarItem(
                                    cookiestarIndex = index,
                                    rotation = cookiestarRotations[index - 1].value,
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    kidStarColor = MaterialTheme.colorScheme.primaryContainer,
                                    countTextColor = MaterialTheme.colorScheme.primary,
                                    onCookiestarClick = onCookiestarClick
                                )
                            }
                        }
                    }

                    // Endpoint dot at the very top of the unselected track
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f))
                    )
                }
            }

            // Drag Handle: Occupies space immediately after (above) the selected cookiestar
            Spacer(modifier = Modifier.height(handleSpacing))
            val handleBg = MaterialTheme.colorScheme.primary
            val handleModifier = if (hazeState != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Modifier
                    .width(handleWidth)
                    .height(handleThickness)
                    .clip(CircleShape)
                    .background(handleBg.copy(alpha = 0.50f))
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            tint = HazeTint(handleBg.copy(alpha = 0.85f)),
                            blurRadius = 16.dp,
                            noiseFactor = HazeDefaults.noiseFactor
                        )
                    )
            } else {
                Modifier
                    .width(handleWidth)
                    .height(handleThickness)
                    .clip(CircleShape)
                    .background(handleBg)
            }
            Box(modifier = handleModifier)
            Spacer(modifier = Modifier.height(handleSpacing))

            // Selected Segment at the BOTTOM (Cookiestars rating downTo 1)
            if (rating > 0) {
                val selectedBg = MaterialTheme.colorScheme.primary
                val selectedModifier = if (hazeState != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Modifier
                        .clip(selectedShape)
                        .background(selectedBg.copy(alpha = 0.35f))
                        .hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                tint = HazeTint(selectedBg.copy(alpha = 0.70f)),
                                blurRadius = 24.dp,
                                noiseFactor = HazeDefaults.noiseFactor
                            )
                        )
                } else {
                    Modifier
                        .clip(selectedShape)
                        .background(selectedBg)
                }

                Box(
                    modifier = Modifier
                        .weight(rating.toFloat())
                        .width(trackWidth)
                        .then(selectedModifier),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (index in rating downTo 1) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CookiestarItem(
                                    cookiestarIndex = index,
                                    rotation = cookiestarRotations[index - 1].value,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    kidStarColor = MaterialTheme.colorScheme.primary,
                                    countTextColor = MaterialTheme.colorScheme.primaryContainer,
                                    onCookiestarClick = onCookiestarClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Cookiestar representation.
 *
 * The outer cookie shape spins when clicked or selected, while the inner Kid Star Material Symbol
 * and count digit (1, 2, 3, 4, 5) remain upright and static without spinning.
 */
@Composable
private fun CookiestarItem(
    cookiestarIndex: Int,
    rotation: Float,
    containerColor: Color,
    kidStarColor: Color,
    countTextColor: Color,
    onCookiestarClick: (Int) -> Unit
) {
    val cookiestarSize = 44.dp
    val kidStarSize = 28.dp

    Box(
        modifier = Modifier
            .size(cookiestarSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 22.dp)
            ) {
                onCookiestarClick(cookiestarIndex)
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. Cookie shape: Rotates/spins on selection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotation
                }
                .clip(CookieShape)
                .background(containerColor)
        )

        // 2. Kid Star Material Symbol + Count Digit (STATIC - DOES NOT SPIN)
        Box(
            modifier = Modifier.size(kidStarSize),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_star_rating),
                contentDescription = null,
                tint = kidStarColor,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = cookiestarIndex.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSansFlexRound,
                    fontSize = 12.sp,
                    color = countTextColor
                )
            )
        }
    }
}
