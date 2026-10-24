package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TooltipAnchorPosition
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
import androidx.compose.ui.zIndex
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
    isOnBlurredSurface: Boolean = LocalIsOnBlurredSurface.current,
    enableTilt: Boolean = true,
    badge: (@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null,
    holdScale: Float = 1.45f
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
        isOnBlurredSurface = isOnBlurredSurface,
        enableTilt = enableTilt,
        badge = badge,
        holdScale = holdScale
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
    isOnBlurredSurface: Boolean = LocalIsOnBlurredSurface.current,
    enableTilt: Boolean = true,
    badge: (@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null,
    holdScale: Float = 1.45f
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
        isOnBlurredSurface = isOnBlurredSurface,
        enableTilt = enableTilt,
        badge = badge,
        holdScale = holdScale
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedCookieButton(
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
    isOnBlurredSurface: Boolean = LocalIsOnBlurredSurface.current,
    enableTilt: Boolean = true,
    badge: (@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null,
    holdScale: Float = 1.45f
) {
    AnimatedCookieButtonImpl(
        onClick = onClick,
        iconContent = iconContent,
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
        isOnBlurredSurface = isOnBlurredSurface,
        enableTilt = enableTilt,
        badge = badge,
        holdScale = holdScale
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
    isOnBlurredSurface: Boolean = LocalIsOnBlurredSurface.current,
    enableTilt: Boolean = true,
    badge: (@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null,
    holdScale: Float = 1.45f
) {
    val view = LocalView.current
    var isHoldMorphing by remember { mutableStateOf(false) }
    var isHolding by remember { mutableStateOf(false) }
    var isHoldCompleted by remember { mutableStateOf(false) }
    var isTapped by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableLongStateOf(0L) }
    val rotation = remember { Animatable(0f) }
    val animScaleX = remember { Animatable(1f) }
    val animScaleY = remember { Animatable(1f) }
    val animIconScale = remember { Animatable(1f) }
    var morphStepIndex by remember { mutableIntStateOf(0) }
    val stepProgress = remember { Animatable(0f) }
    val globalRotation = remember { Animatable(0f) }
    val settleRotation = remember { Animatable(0f) }
    var isSettling by remember { mutableStateOf(false) }

    val isCallerPrimary = containerColor == MaterialTheme.colorScheme.primary
    val isActive = (isHolding && !isHoldCompleted) || isTapped
    val isPrimary = isActive || isCallerPrimary

    val targetContainer = if (isActive) MaterialTheme.colorScheme.primary else containerColor
    val targetContent = if (isPrimary) MaterialTheme.colorScheme.onPrimary else contentColor

    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = tween(durationMillis = 200),
        label = "cookie_button_container_color"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = targetContent,
        animationSpec = tween(durationMillis = 200),
        label = "cookie_button_content_color"
    )

    val primaryAlpha by animateFloatAsState(
        targetValue = if (isPrimary) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "cookie_button_primary_alpha"
    )

    val tiltAngle by com.android.snippets.ui.util.rememberCookieTilt(enabled = enableTilt && enabled && isPrimary)

    val isMorphActive = (isHolding && !isHoldCompleted) || isSettling
    val isMorphing = (isHoldMorphing && !isHoldCompleted) || isSettling

    val badgeAlpha by animateFloatAsState(
        targetValue = if (isMorphing) 0f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "cookie_badge_alpha"
    )
    val badgeScale by animateFloatAsState(
        targetValue = if (isMorphing) 0f else 1f,
        animationSpec = if (isMorphing) tween(durationMillis = 180) else Motion.BouncySpring,
        label = "cookie_badge_scale"
    )

    val currentShape = if (isMorphActive) {
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
    } else {
        shape
    }

    val currentOnClick by rememberUpdatedState(onClick)
    val currentIsSpinning by rememberUpdatedState(isSpinning)
    val scope = rememberCoroutineScope()
    var spinJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val isElevated = isMorphActive || isHolding || animScaleX.value > 1.01f || isTapped

    val buttonContent: @Composable (Modifier) -> Unit = { btnModifier ->
        Box(
            modifier = btnModifier
                .zIndex(if (isElevated) 10f else 0f)
                .size(size)
                .graphicsLayer {
                    rotationZ = rotation.value + tiltAngle
                    scaleX = animScaleX.value
                    scaleY = animScaleY.value
                    clip = false
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures(
                        onPress = {
                            pressStartTime = System.currentTimeMillis()
                            isHolding = true
                            isHoldCompleted = false
                            try {
                                awaitRelease()
                            } finally {
                                isHolding = false
                                isHoldCompleted = false
                            }
                        },
                        onLongPress = {
                            // Absorbs long-press gesture so releasing does not fire tap
                        },
                        onTap = {
                            if (System.currentTimeMillis() - pressStartTime >= 300L) return@detectTapGestures
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            val shouldSpin = currentIsSpinning
                            spinJob?.cancel()
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
                                val rotJob = if (shouldSpin) {
                                    launch {
                                        rotation.animateTo(
                                            targetValue = rotation.value + 360f,
                                            animationSpec = Motion.BouncySpring
                                        )
                                    }
                                } else null

                                kotlinx.coroutines.delay(180L)
                                currentOnClick()

                                rotJob?.join()
                                scaleJobX.join()
                                scaleJobY.join()
                                isTapped = false
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isOnBlurredSurface) {
                // Outline variant layer (bottom):
                // Retains button container color with M3 outline variant border.
                // Kept at full opacity so it never fades out when turning primary.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(currentShape)
                        .background(if (enabled) containerColor else containerColor.copy(alpha = 0.38f))
                        .border(
                            border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
                            shape = currentShape
                        )
                )
                // Primary layer: drawn OVER the outline variant so the outline is not visible when primary
                if (primaryAlpha > 0.001f) {
                    val primaryColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer { alpha = primaryAlpha }
                            .border(1.dp, primaryColor, currentShape)
                            .clip(currentShape)
                            .background(primaryColor)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(currentShape)
                        .background(if (enabled) animatedContainerColor else animatedContainerColor.copy(alpha = 0.38f))
                )
            }
            val iconModifier = Modifier
                .size(iconSize)
                .graphicsLayer { 
                    rotationZ = -(rotation.value + tiltAngle)
                    scaleX = animIconScale.value
                    scaleY = animIconScale.value
                }
            val tint = if (enabled) animatedContentColor else animatedContentColor.copy(alpha = 0.38f)
            iconContent(iconModifier, tint)
        }
    }

    val content = @Composable {
        val elevatedModifier = modifier.zIndex(if (isElevated) 10f else 0f)
        if (badge != null) {
            Box(
                modifier = elevatedModifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                buttonContent(Modifier)
                if (badgeAlpha > 0.001f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = badgeAlpha
                                scaleX = badgeScale
                                scaleY = badgeScale
                            }
                    ) {
                        badge()
                    }
                }
            }
        } else {
            buttonContent(elevatedModifier)
        }
    }

    // Hold scale expansion: grows to 1.45x so morphing shapes are clearly visible outside the finger,
    // while icon shrinks to small size (0.5x) so morphing shapes are prominent,
    // and springs back to 1f with BouncySpring when released OR when the last shape is reached
    LaunchedEffect(isHolding, isHoldCompleted) {
        if (isHolding && !isHoldCompleted) {
            kotlinx.coroutines.delay(150L)
            if (!isHolding || isHoldCompleted) return@LaunchedEffect
            isHoldMorphing = true
            launch { animScaleX.animateTo(holdScale, animationSpec = Motion.BouncySpring) }
            launch { animScaleY.animateTo(holdScale, animationSpec = Motion.BouncySpring) }
            launch { animIconScale.animateTo(0.5f, animationSpec = Motion.BouncySpring) }
        } else {
            isHoldMorphing = false
            launch { animIconScale.animateTo(1f, animationSpec = Motion.BouncySpring) }
            if (!isTapped) {
                launch { animScaleX.animateTo(1f, animationSpec = Motion.BouncySpring) }
                launch { animScaleY.animateTo(1f, animationSpec = Motion.BouncySpring) }
            }
        }
    }


    // Shape morphing while holding:
    // Follows the official M3 Expressive LoadingIndicator shape morph implementation:
    // Uses the LoadingIndicator spring spec (dampingRatio = 0.6f, stiffness = 200f, visibilityThreshold = 0.01f),
    // continuous global rotation (tween(4666, LinearEasing), RepeatMode.Restart),
    // and 650ms morph interval (MorphIntervalMillis = 650L) with 90 degree clockwise right-turn per shape.
    // When the last 9-sided cookie shape is reached, triggers the return animation that happens
    // when you hold off the button, returning to scale 1.0f with holdSpringSpec and resting on the last shape.
    LaunchedEffect(isHolding) {
        if (isHolding) {
            isHoldCompleted = false
            isSettling = false
            morphStepIndex = 0
            stepProgress.snapTo(0f)
            globalRotation.snapTo(0f)

            // Small debounce to ensure an intentional hold rather than an immediate tap
            kotlinx.coroutines.delay(120L)
            if (!isHolding) return@LaunchedEffect

            // Continuous global rotation from LoadingIndicator (4666ms per 360Â°)
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
                if (hapticOnHold) {
                    view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK)
                }

                val deferred = async {
                    stepProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = morphAnimationSpec
                    )
                }
                kotlinx.coroutines.delay(650L)
                deferred.await()
            }

            // All steps reached! Last 9-sided cookie completed!
            globalRotationJob.cancel()

            // Trigger return animation that happens when holding off the button:
            // "when the last 9 sided cookie is reached, use the animation that happens when you hold off the button. make the button go back to its place with the last shape"
            val currentRot = (morphStepIndex * 90f) + (stepProgress.value * 90f) + globalRotation.value
            val targetRot = kotlin.math.round(currentRot / 360f) * 360f
            settleRotation.snapTo(currentRot)
            isSettling = true
            isHoldCompleted = true

            scope.launch {
                settleRotation.animateTo(targetRot, animationSpec = Motion.BouncySpring)
                isSettling = false
            }
        } else {
            if (!isHoldCompleted) {
                isSettling = false
                stepProgress.snapTo(0f)
                morphStepIndex = 0
                globalRotation.snapTo(0f)
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
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
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
