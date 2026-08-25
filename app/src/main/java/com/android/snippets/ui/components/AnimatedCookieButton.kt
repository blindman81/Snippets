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
import com.android.snippets.ui.shapes.CookieHoldMorphs
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.shapes.MorphSequenceShape
import com.android.snippets.ui.util.Motion

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
    shape: androidx.compose.ui.graphics.Shape = CookieShape,
    isSpinning: Boolean = true,
    spinOnEntry: Boolean = false,
    enabled: Boolean = true,
    hapticOnHold: Boolean = true,
    iconSize: androidx.compose.ui.unit.Dp = size * 0.55f,
    useAnimatedGradient: Boolean = true,
    gradientColors: List<Color>? = null
) {
    AnimatedCookieButtonImpl(
        onClick = onClick,
        iconContent = { iconModifier, tint ->
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = iconModifier,
                tint = tint
            )
        },
        modifier = modifier,
        tooltip = tooltip,
        containerColor = containerColor,
        contentColor = contentColor,
        size = size,
        shape = shape,
        isSpinning = isSpinning,
        spinOnEntry = spinOnEntry,
        enabled = enabled,
        hapticOnHold = hapticOnHold,
        iconSize = iconSize,
        useAnimatedGradient = useAnimatedGradient,
        gradientColors = gradientColors
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedCookieButton(
    onClick: () -> Unit,
    icon: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tooltip: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    shape: androidx.compose.ui.graphics.Shape = CookieShape,
    isSpinning: Boolean = true,
    spinOnEntry: Boolean = false,
    enabled: Boolean = true,
    hapticOnHold: Boolean = true,
    iconSize: androidx.compose.ui.unit.Dp = size * 0.55f,
    useAnimatedGradient: Boolean = true,
    gradientColors: List<Color>? = null
) {
    AnimatedCookieButtonImpl(
        onClick = onClick,
        iconContent = { iconModifier, tint ->
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = icon),
                contentDescription = contentDescription,
                modifier = iconModifier,
                tint = tint
            )
        },
        modifier = modifier,
        tooltip = tooltip,
        containerColor = containerColor,
        contentColor = contentColor,
        size = size,
        shape = shape,
        isSpinning = isSpinning,
        spinOnEntry = spinOnEntry,
        enabled = enabled,
        hapticOnHold = hapticOnHold,
        iconSize = iconSize,
        useAnimatedGradient = useAnimatedGradient,
        gradientColors = gradientColors
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedCookieButtonImpl(
    onClick: () -> Unit,
    iconContent: @Composable (modifier: Modifier, tint: Color) -> Unit,
    modifier: Modifier = Modifier,
    tooltip: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    shape: androidx.compose.ui.graphics.Shape = CookieShape,
    isSpinning: Boolean = true,
    spinOnEntry: Boolean = false,
    enabled: Boolean = true,
    hapticOnHold: Boolean = true,
    iconSize: androidx.compose.ui.unit.Dp = size * 0.55f,
    useAnimatedGradient: Boolean = true,
    gradientColors: List<Color>? = null
) {
    val view = LocalView.current
    var isHolding by remember { mutableStateOf(false) }
    var isTapped by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    val animScaleX = remember { Animatable(1f) }
    val animScaleY = remember { Animatable(1f) }
    val morphProgress = remember { Animatable(0f) }

    val isActive = isHolding || isTapped
    val targetContainer = if (isActive) MaterialTheme.colorScheme.primary else containerColor
    val targetContent = if (isActive || useAnimatedGradient) MaterialTheme.colorScheme.onPrimary else contentColor

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

    val gradientBrush = if (useAnimatedGradient || isActive) {
        rememberAnimatedGradientBrush(
            colors = gradientColors ?: AnimatedGradientDefaults.themeGradient()
        )
    } else null

    val currentShape = if (isHolding || morphProgress.value > 0.001f) {
        MorphSequenceShape(CookieHoldMorphs, morphProgress.value)
    } else {
        shape
    }

    val scope = rememberCoroutineScope()
    var spinJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val content = @Composable {
        Box(
            modifier = modifier
                .size(size)
                .graphicsLayer {
                    rotationZ = rotation.value
                    scaleX = animScaleX.value
                    scaleY = animScaleY.value
                }
                .clip(currentShape)
                .then(
                    if (gradientBrush != null && enabled) {
                        Modifier.background(gradientBrush)
                    } else {
                        Modifier.background(if (enabled) animatedContainerColor else animatedContainerColor.copy(alpha = 0.38f))
                    }
                )
                .pointerInput(enabled, isSpinning) {
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
                            if (spinJob?.isActive == true) return@detectTapGestures
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            if (isSpinning) {
                                spinJob = scope.launch {
                                    isTapped = true
                                    val scaleJobX = launch {
                                        animScaleX.animateTo(0.84f, Motion.PressSpring)
                                        animScaleX.animateTo(1f, Motion.BouncySpring)
                                    }
                                    val scaleJobY = launch {
                                        animScaleY.animateTo(0.84f, Motion.PressSpring)
                                        animScaleY.animateTo(1f, Motion.BouncySpring)
                                    }
                                    val anim = launch {
                                        rotation.animateTo(
                                            targetValue = rotation.value + 360f,
                                            animationSpec = Motion.BouncySpring
                                        )
                                    }
                                    kotlinx.coroutines.delay(180)
                                    onClick()
                                    anim.join()
                                    scaleJobX.join()
                                    scaleJobY.join()
                                    isTapped = false
                                }
                            } else {
                                isTapped = true
                                scope.launch {
                                    val scaleJobX = launch {
                                        animScaleX.animateTo(0.84f, Motion.PressSpring)
                                        animScaleX.animateTo(1f, Motion.BouncySpring)
                                    }
                                    val scaleJobY = launch {
                                        animScaleY.animateTo(0.84f, Motion.PressSpring)
                                        animScaleY.animateTo(1f, Motion.BouncySpring)
                                    }
                                    kotlinx.coroutines.delay(120)
                                    scaleJobX.join()
                                    scaleJobY.join()
                                    isTapped = false
                                }
                                onClick()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val iconModifier = Modifier
                .size(iconSize)
                .graphicsLayer { 
                    rotationZ = -rotation.value
                }
            val tint = if (enabled) animatedContentColor else animatedContentColor.copy(alpha = 0.38f)
            iconContent(iconModifier, tint)
        }
    }

    // Hold scale expansion: grows to 1.3x so morphing shapes are clearly visible outside the finger
    LaunchedEffect(isHolding) {
        if (isHolding) {
            launch { animScaleX.animateTo(1.3f, animationSpec = Motion.BouncySpring) }
            launch { animScaleY.animateTo(1.3f, animationSpec = Motion.BouncySpring) }
        } else if (!isTapped) {
            launch { animScaleX.animateTo(1f, animationSpec = Motion.BouncySpring) }
            launch { animScaleY.animateTo(1f, animationSpec = Motion.BouncySpring) }
        }
    }


    // Continuous shape morphing while holding (slower pace to clearly admire each shape transition): 12-sided cookie -> pill -> pentagon -> 12-sided cookie -> 4-sided cookie -> very sunny -> oval -> 12-sided cookie
    LaunchedEffect(isHolding) {
        if (isHolding) {
            while (true) {
                morphProgress.animateTo(
                    targetValue = morphProgress.value + CookieHoldMorphs.size,
                    animationSpec = tween(CookieHoldMorphs.size * 900, easing = LinearEasing)
                )
            }
        } else {
            if (morphProgress.value > 0f) {
                morphProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = Motion.BouncySpring
                )
            }
        }
    }

    // Haptic feedback loop while holding (opt-in per call site)
    LaunchedEffect(isHolding) {
        if (isHolding && hapticOnHold) {
            while (true) {
                view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
                kotlinx.coroutines.delay(160)
            }
        }
    }

    // Spin on entry if configured
    LaunchedEffect(spinOnEntry) {
        if (spinOnEntry) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = Motion.BouncySpring
            )
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
