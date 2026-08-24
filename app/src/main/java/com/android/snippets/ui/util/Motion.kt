package com.android.snippets.ui.util

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.android.snippets.viewmodel.Screen

object Motion {
    val ExpressiveSpring = spring<Float>(
        dampingRatio = 0.55f,
        stiffness = 300f
    )

    val PressSpring = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = 1200f
    )

    private val NavEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
    private const val NavDuration = 300
    private const val ExitDuration = 250

    fun screenTransition(initialState: Screen, targetState: Screen, motionScheme: androidx.compose.material3.MotionScheme): ContentTransform {
        val isNavigatingBack = targetState == Screen.Library ||
            (initialState in listOf(Screen.SelectIcon, Screen.PhotosCarousel) && targetState == Screen.Settings) ||
            (initialState in listOf(Screen.MonthlyRecap, Screen.YearlyRecap))

        return when {
            targetState == Screen.Detail ->
                (fadeIn(animationSpec = tween(NavDuration, easing = NavEasing)) +
                    scaleIn(initialScale = 0.95f, animationSpec = tween(NavDuration, easing = NavEasing)))
                    .togetherWith(fadeOut(animationSpec = tween(ExitDuration, easing = FastOutLinearInEasing)))

            initialState == Screen.Detail ->
                fadeIn(animationSpec = tween(NavDuration, easing = NavEasing))
                    .togetherWith(fadeOut(animationSpec = tween(ExitDuration, easing = FastOutLinearInEasing)))

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

    fun bottomBarSwap(scope: AnimatedContentTransitionScope<Boolean>, motionScheme: androidx.compose.material3.MotionScheme): ContentTransform = with(scope) {
        val enter: EnterTransition =
            slideInVertically(animationSpec = motionScheme.fastSpatialSpec()) { it } +
                fadeIn(animationSpec = motionScheme.fastEffectsSpec())
        val exit: ExitTransition =
            slideOutVertically(animationSpec = motionScheme.fastSpatialSpec()) { it } +
                fadeOut(animationSpec = motionScheme.fastEffectsSpec())

        enter.togetherWith(exit).using(SizeTransform(clip = true))
    }
}
