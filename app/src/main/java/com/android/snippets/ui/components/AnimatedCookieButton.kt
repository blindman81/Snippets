package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.shapes.LocalAppShape
import com.android.snippets.ui.shapes.LocalAppShapeType
import com.android.snippets.ui.shapes.AppShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedCookieButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tooltip: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    shape: androidx.compose.ui.graphics.Shape = LocalAppShape.current,
    isSpinning: Boolean = true,
    spinOnEntry: Boolean = false,
    enabled: Boolean = true,
    hapticOnHold: Boolean = true,
    iconSize: androidx.compose.ui.unit.Dp = size * 0.55f
) {
    val view = LocalView.current
    var isHolding by remember { mutableStateOf(false) }
    var isTapped by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    val animScaleX = remember { Animatable(1f) }
    val animScaleY = remember { Animatable(1f) }

    val shapeType = LocalAppShapeType.current
    val isSpinningShape = when (shapeType) {
        AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> true
        else -> false
    }

    val isActive = isHolding || isTapped
    val targetContainer = if (isActive) MaterialTheme.colorScheme.primary else containerColor
    val targetContent = if (isActive) MaterialTheme.colorScheme.onPrimary else contentColor

    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = tween(150),
        label = "cookie_button_container_color"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = targetContent,
        animationSpec = tween(150),
        label = "cookie_button_content_color"
    )

    val content = @Composable {
        Box(
            modifier = modifier
                .size(size)
                .graphicsLayer {
                    rotationZ = when (shapeType) {
                        AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> rotation.value
                        AppShape.PENTAGON -> rotation.value
                        else -> 0f
                    }
                    scaleX = animScaleX.value
                    scaleY = animScaleY.value
                }
                .clip(shape)
                .background(if (enabled) animatedContainerColor else animatedContainerColor.copy(alpha = 0.38f))
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            try {
                                awaitRelease()
                            } finally {
                                isHolding = false
                            }
                        },
                        onTap = {
                            isTapped = true
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            onClick()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer { 
                        rotationZ = when (shapeType) {
                            AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> -rotation.value
                            AppShape.PENTAGON -> -rotation.value
                            else -> 0f
                        }
                    },
                tint = if (enabled) animatedContentColor else animatedContentColor.copy(alpha = 0.38f)
            )
        }
    }


    // Spin/Pulse/Wiggle/Squish animation loop
    LaunchedEffect(Unit) {
        while (true) {
            if (isTapped) {
                when (shapeType) {
                    AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> {
                        rotation.animateTo(
                            targetValue = rotation.value + 360f,
                            animationSpec = tween(300, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f))
                        )
                    }
                    AppShape.COOKIE_4_SIDED -> {
                        launch {
                            animScaleX.animateTo(0.75f, animationSpec = tween(70, easing = FastOutLinearInEasing))
                            animScaleX.animateTo(1.15f, animationSpec = tween(90, easing = FastOutSlowInEasing))
                            animScaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        }
                        launch {
                            animScaleY.animateTo(0.75f, animationSpec = tween(70, easing = FastOutLinearInEasing))
                            animScaleY.animateTo(1.15f, animationSpec = tween(90, easing = FastOutSlowInEasing))
                            animScaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        }
                    }
                    AppShape.GEM, AppShape.SQUARE -> {
                        launch {
                            animScaleX.animateTo(1.18f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                            animScaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        }
                        launch {
                            animScaleY.animateTo(1.18f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                            animScaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        }
                    }
                    AppShape.PENTAGON -> {
                        rotation.animateTo(-15f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                        rotation.animateTo(15f, animationSpec = tween(120, easing = FastOutSlowInEasing))
                        rotation.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                    AppShape.CLOVER_4_LEAF -> {
                        launch {
                            animScaleX.animateTo(1.25f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                            animScaleX.animateTo(0.8f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                            animScaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        }
                        launch {
                            animScaleY.animateTo(0.75f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                            animScaleY.animateTo(1.2f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                            animScaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        }
                    }
                    AppShape.CLOVER_8_LEAF -> {
                        launch {
                            animScaleX.animateTo(1.15f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                            animScaleX.animateTo(1.03f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                            animScaleX.animateTo(1.20f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                            animScaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        }
                        launch {
                            animScaleY.animateTo(1.15f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                            animScaleY.animateTo(1.03f, animationSpec = tween(80, easing = FastOutSlowInEasing))
                            animScaleY.animateTo(1.20f, animationSpec = tween(100, easing = FastOutSlowInEasing))
                            animScaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        }
                    }
                }
                kotlinx.coroutines.delay(150)
                isTapped = false
            } else if (isHolding) {
                when (shapeType) {
                    AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> {
                        rotation.animateTo(
                            targetValue = rotation.value + 360f,
                            animationSpec = tween(600, easing = LinearEasing)
                        )
                    }
                    AppShape.COOKIE_4_SIDED -> {
                        launch { animScaleX.animateTo(0.82f, animationSpec = tween(120, easing = FastOutSlowInEasing)) }
                        launch { animScaleY.animateTo(0.82f, animationSpec = tween(120, easing = FastOutSlowInEasing)) }
                        androidx.compose.runtime.snapshotFlow { isHolding }.first { !it }
                        launch { animScaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                        launch { animScaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                    }
                    AppShape.GEM, AppShape.SQUARE -> {
                        launch { animScaleX.animateTo(0.9f, animationSpec = tween(150, easing = FastOutSlowInEasing)) }
                        launch { animScaleY.animateTo(0.9f, animationSpec = tween(150, easing = FastOutSlowInEasing)) }
                        androidx.compose.runtime.snapshotFlow { isHolding }.first { !it }
                        launch { animScaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                        launch { animScaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                    }
                    AppShape.PENTAGON -> {
                        rotation.animateTo(12f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                        androidx.compose.runtime.snapshotFlow { isHolding }.first { !it }
                        rotation.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                    AppShape.CLOVER_4_LEAF -> {
                        launch { animScaleX.animateTo(1.15f, animationSpec = tween(150, easing = FastOutSlowInEasing)) }
                        launch { animScaleY.animateTo(0.85f, animationSpec = tween(150, easing = FastOutSlowInEasing)) }
                        androidx.compose.runtime.snapshotFlow { isHolding }.first { !it }
                        launch { animScaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                        launch { animScaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                    }
                    AppShape.CLOVER_8_LEAF -> {
                        val pulseJob = launch {
                            while (isHolding) {
                                animScaleX.animateTo(1.08f, animationSpec = tween(200, easing = FastOutSlowInEasing))
                                animScaleX.animateTo(1.02f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                                animScaleX.animateTo(1.12f, animationSpec = tween(250, easing = FastOutSlowInEasing))
                                animScaleX.animateTo(1.0f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                kotlinx.coroutines.delay(200)
                            }
                        }
                        val pulseJobY = launch {
                            while (isHolding) {
                                animScaleY.animateTo(1.08f, animationSpec = tween(200, easing = FastOutSlowInEasing))
                                animScaleY.animateTo(1.02f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                                animScaleY.animateTo(1.12f, animationSpec = tween(250, easing = FastOutSlowInEasing))
                                animScaleY.animateTo(1.0f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                                kotlinx.coroutines.delay(200)
                            }
                        }
                        androidx.compose.runtime.snapshotFlow { isHolding }.first { !it }
                        pulseJob.cancel()
                        pulseJobY.cancel()
                        launch { animScaleX.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                        launch { animScaleY.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                    }
                }
            } else {
                androidx.compose.runtime.snapshotFlow { isHolding || isTapped }
                    .first { it }
            }
        }
    }

    // Haptic feedback loop while holding (opt-in per call site)
    LaunchedEffect(isHolding) {
        if (isHolding && hapticOnHold) {
            while (true) {
                view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
                kotlinx.coroutines.delay(100)
            }
        }
    }

    // Spin/Pulse on entry if configured
    LaunchedEffect(spinOnEntry) {
        if (spinOnEntry) {
            when (shapeType) {
                AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> {
                    rotation.animateTo(
                        targetValue = rotation.value + 360f,
                        animationSpec = tween(1000, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f))
                    )
                }
                AppShape.COOKIE_4_SIDED -> {
                    animScaleX.snapTo(0.5f)
                    animScaleY.snapTo(0.5f)
                    launch {
                        animScaleX.animateTo(1.2f, animationSpec = tween(200, easing = FastOutSlowInEasing))
                        animScaleX.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                    launch {
                        animScaleY.animateTo(1.2f, animationSpec = tween(200, easing = FastOutSlowInEasing))
                        animScaleY.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                }
                AppShape.PENTAGON -> {
                    launch {
                        animScaleX.snapTo(0f)
                        animScaleX.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                    }
                    launch {
                        animScaleY.snapTo(0f)
                        animScaleY.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                    }
                    rotation.animateTo(15f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                    rotation.animateTo(-15f, animationSpec = tween(200, easing = FastOutSlowInEasing))
                    rotation.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                }
                AppShape.CLOVER_4_LEAF -> {
                    animScaleX.snapTo(0f)
                    animScaleY.snapTo(0f)
                    launch {
                        animScaleX.animateTo(1.15f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                        animScaleX.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                    launch {
                        kotlinx.coroutines.delay(50)
                        animScaleY.animateTo(1.15f, animationSpec = tween(300, easing = FastOutSlowInEasing))
                        animScaleY.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                    }
                }
                AppShape.CLOVER_8_LEAF, AppShape.GEM, AppShape.SQUARE -> {
                    launch {
                        animScaleX.snapTo(0f)
                        animScaleX.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                    }
                    launch {
                        animScaleY.snapTo(0f)
                        animScaleY.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                    }
                }
            }
        }
    }

    if (tooltip != null) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
            tooltip = {
                PlainTooltip(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(tooltip)
                }
            },
            state = rememberTooltipState()
        ) {
            content()
        }
    } else {
        content()
    }
}
