package com.android.snippets.ui.util

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.android.snippets.viewmodel.Screen

object Motion {
    // --- Spring Physics Tokens ---
    val BouncySpring = spring<Float>(
        dampingRatio = 0.45f,
        stiffness = 260f
    )

    val ExpressiveSpring = spring<Float>(
        dampingRatio = 0.48f,
        stiffness = 280f
    )

    val PressSpring = spring<Float>(
        dampingRatio = 0.60f,
        stiffness = 900f
    )

    val SheetPopSpring = spring<Float>(
        dampingRatio = 0.52f,
        stiffness = 320f
    )

    val FastExitSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Spatial Springs for Layout Offsets
    val ToolbarEnterSpring = spring<Dp>(
        dampingRatio = 0.65f,
        stiffness = Spring.StiffnessMediumLow
    )

    val ToolbarExitSpring = spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val ToolbarScaleEnterSpring = spring<Float>(
        dampingRatio = 0.65f,
        stiffness = Spring.StiffnessMediumLow
    )

    val ToolbarScaleExitSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val NavBarEnterSpring = spring<Dp>(
        dampingRatio = 0.65f,
        stiffness = Spring.StiffnessMediumLow
    )

    val NavBarExitSpring = spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val PhotoExpandSpring = spring<IntSize>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = 120f
    )

    // Dialog & Modal Spatial Transitions
    val DialogSlideEnter = spring<IntOffset>(
        dampingRatio = 0.52f,
        stiffness = 320f
    )

    val DialogSlideExit = tween<IntOffset>(
        durationMillis = 220,
        easing = FastOutSlowInEasing
    )

    val DialogScaleEnter = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val DialogScaleExit = tween<Float>(
        durationMillis = 220,
        easing = FastOutSlowInEasing
    )

    val ToolbarSlideEnterInt = spring<IntOffset>(
        dampingRatio = 0.65f,
        stiffness = Spring.StiffnessMediumLow
    )

    val ToolbarSlideExitInt = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // --- Timing & Effects Tokens (Fades, Alphas, Colors) ---
    val FastFade = tween<Float>(
        durationMillis = 180,
        easing = FastOutLinearInEasing
    )

    val StandardFade = tween<Float>(
        durationMillis = 200,
        easing = FastOutSlowInEasing
    )

    val StandardColorFade = tween<androidx.compose.ui.graphics.Color>(
        durationMillis = 200,
        easing = FastOutSlowInEasing
    )

    val MediumFade = tween<Float>(
        durationMillis = 250,
        easing = FastOutSlowInEasing
    )

    val SlowFade = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    val FadeEnter = spring<Float>(
        stiffness = Spring.StiffnessMediumLow
    )

    val FadeExit = spring<Float>(
        stiffness = Spring.StiffnessMediumLow
    )

    val PhotoCardFade = tween<Float>(
        durationMillis = 500
    )

    val PhotoCardExitFade = tween<Float>(
        durationMillis = 350
    )

    // --- Navigation Transitions ---
    val NavEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
    const val NavDuration = 300
    const val ExitDuration = 250

    val NavEnterFade = tween<Float>(300, easing = FastOutSlowInEasing)
    val NavEnterScale = tween<Float>(350, easing = FastOutSlowInEasing)
    val NavExitFade = tween<Float>(250, easing = FastOutSlowInEasing)
    val NavExitScale = tween<Float>(250, easing = FastOutSlowInEasing)
    val NavPopExitScale = tween<Float>(300, easing = FastOutSlowInEasing)

    val NavMemoryEnterFade = tween<Float>(400, easing = FastOutSlowInEasing)
    val NavMemoryExitFade = tween<Float>(380, easing = FastOutSlowInEasing)

    @OptIn(ExperimentalSharedTransitionApi::class)
    val SharedPhotoBoundsTransform: BoundsTransform = BoundsTransform { _, _ ->
        tween(durationMillis = 600, easing = FastOutSlowInEasing)
    }

    fun wiggleSpec(direction: Float = 1f, amplitude: Float = 14f): KeyframesSpec<Float> = keyframes {
        durationMillis = 400
        0f at 0
        (amplitude * direction) at 60 using FastOutSlowInEasing
        (-amplitude * direction) at 140 using FastOutSlowInEasing
        (amplitude * 0.6f * direction) at 220 using FastOutSlowInEasing
        (-amplitude * 0.3f * direction) at 300 using FastOutSlowInEasing
        (amplitude * 0.1f * direction) at 360 using FastOutSlowInEasing
        0f at 400
    }

    @Composable
    fun ExpressiveSheetContent(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier.fillMaxWidth()
        ) {
            content()
        }
    }

    fun screenTransition(initialState: Screen, targetState: Screen): ContentTransform {
        val isNavigatingBack = targetState == Screen.Library ||
            (initialState == Screen.EmojiPicker && targetState == Screen.Settings) ||
            (initialState in listOf(Screen.MonthlyRecap, Screen.YearlyRecap))

        return when {
            targetState == Screen.Memory ->
                (slideInVertically(animationSpec = tween(NavDuration, easing = NavEasing)) { it } +
                    fadeIn(animationSpec = tween(NavDuration, easing = NavEasing)))
                    .togetherWith(fadeOut(animationSpec = tween(ExitDuration, easing = FastOutLinearInEasing)))

            isNavigatingBack ->
                (fadeIn(animationSpec = tween(NavDuration, easing = NavEasing)) +
                    scaleIn(initialScale = 0.96f, animationSpec = tween(NavDuration, easing = NavEasing)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(ExitDuration, easing = FastOutLinearInEasing)) +
                            scaleOut(targetScale = 0.98f, animationSpec = tween(ExitDuration, easing = FastOutLinearInEasing))
                    )

            else ->
                (fadeIn(animationSpec = tween(NavDuration, easing = NavEasing)) +
                    scaleIn(initialScale = 0.96f, animationSpec = tween(NavDuration, easing = NavEasing)))
                    .togetherWith(fadeOut(animationSpec = tween(ExitDuration, easing = FastOutLinearInEasing)))
        }
    }

    fun bottomBarSwap(scope: AnimatedContentTransitionScope<Boolean>): ContentTransform = with(scope) {
        val enter: EnterTransition =
            slideInVertically(animationSpec = ToolbarSlideEnterInt) { it } +
                fadeIn(animationSpec = StandardFade)
        val exit: ExitTransition =
            slideOutVertically(animationSpec = ToolbarSlideExitInt) { it } +
                fadeOut(animationSpec = FastFade)

        enter.togetherWith(exit).using(SizeTransform(clip = true))
    }
}
