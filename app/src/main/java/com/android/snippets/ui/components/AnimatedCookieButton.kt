package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import kotlinx.coroutines.flow.first
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
import com.android.snippets.ui.CookieShape

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
    iconSize: androidx.compose.ui.unit.Dp = size * 0.55f
) {
    val view = LocalView.current
    var isHolding by remember { mutableStateOf(false) }
    var isTapped by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }

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
                    rotationZ = rotation.value
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
                    .graphicsLayer { rotationZ = -rotation.value },
                tint = if (enabled) animatedContentColor else animatedContentColor.copy(alpha = 0.38f)
            )
        }
    }


    // Spin animation loop: ensures each rotation finishes cleanly without snapping
    LaunchedEffect(Unit) {
        while (true) {
            if (isTapped) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(300, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f))
                )
                // Wait a bit before allowing another tap spin to avoid spam
                kotlinx.coroutines.delay(150)
                isTapped = false
            } else if (isHolding) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(600, easing = LinearEasing)
                )
            } else {
                // Suspend until either holding or tapped becomes true
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

    // Spin on entry if configured
    LaunchedEffect(spinOnEntry) {
        if (spinOnEntry) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = tween(1000, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f))
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
