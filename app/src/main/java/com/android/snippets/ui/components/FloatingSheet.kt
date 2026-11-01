package com.android.snippets.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.snippets.ui.util.CookieTiltController
import com.android.snippets.ui.util.LocalTiltActive
import com.android.snippets.ui.util.LocalTiltModalFocus
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState

/**
 * A floating sheet anchored directly above the CookieToolbar with frosted glass blur.
 * Animates sliding up from the bottom and smoothly sliding down on dismissal.
 */
@Composable
fun FloatingSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetModifier: Modifier = Modifier,
    bottomSpacing: Dp = 108.dp,
    shape: Shape = RoundedCornerShape(32.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    alpha: Float = HazeDefaults.tintAlpha,
    contentColor: Color = contentColorFor(containerColor),
    hazeState: HazeState? = LocalHazeState.current,
    showScrim: Boolean = true,
    scrimColor: Color = Color.Black.copy(alpha = 0.35f),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    if (visible) {
        BackHandler { onDismissRequest() }
        DisposableEffect(Unit) {
            CookieTiltController.beginTiltModalFocus()
            onDispose {
                CookieTiltController.endTiltModalFocus()
            }
        }
    }

    // Scrim behind sheet (but behind CookieToolbar)
    if (showScrim) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.zIndex(15f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { onDismissRequest() }
                    }
                    .background(scrimColor)
            )
        }
    }

    val density = LocalDensity.current
    val pillSlideDistance = with(density) { 80.dp.roundToPx() }

    val contentAlphaState = androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "floating_sheet_content_alpha"
    )

    // Floating Sheet Content - Positioned above CookieToolbar, sliding smoothly in and out from the pill
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f)
            .safeDrawingPadding()
            .padding(bottom = bottomSpacing)
            .then(modifier),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { pillSlideDistance },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + scaleIn(
                initialScale = 0.88f,
                transformOrigin = TransformOrigin(0.5f, 1f),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(180)),
            exit = slideOutVertically(
                targetOffsetY = { pillSlideDistance },
                animationSpec = tween(
                    durationMillis = 320,
                    easing = CubicBezierEasing(0.3f, 0f, 0.1f, 1f)
                )
            ) + scaleOut(
                targetScale = 0.85f,
                transformOrigin = TransformOrigin(0.5f, 1f),
                animationSpec = tween(
                    durationMillis = 320,
                    easing = CubicBezierEasing(0.3f, 0f, 0.1f, 1f)
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 280,
                    easing = androidx.compose.animation.core.LinearEasing
                )
            )
        ) {
            CompositionLocalProvider(
                LocalTiltActive provides visible,
                LocalTiltModalFocus provides true
            ) {
                BlurredSurface(
                    shape = shape,
                    color = containerColor,
                    alpha = alpha,
                    contentColor = contentColor,
                    hazeState = hazeState,
                    shadowElevation = 12.dp,
                    modifier = sheetModifier
                        .pointerInput(Unit) {
                            detectTapGestures { /* consume taps inside */ }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(contentPadding)
                            .alpha(contentAlphaState.value),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
