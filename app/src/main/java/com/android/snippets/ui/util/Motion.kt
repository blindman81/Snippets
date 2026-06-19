package com.android.snippets.ui.util

import androidx.compose.animation.*
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

    fun screenTransition(initialState: Screen, targetState: Screen, motionScheme: androidx.compose.material3.MotionScheme): ContentTransform {
        return when {
            targetState == Screen.Detail ->
                (fadeIn(animationSpec = motionScheme.fastEffectsSpec()) +
                    scaleIn(initialScale = 0.95f, animationSpec = motionScheme.fastSpatialSpec()))
                    .togetherWith(fadeOut(animationSpec = motionScheme.fastEffectsSpec()))

            initialState == Screen.Detail ->
                fadeIn(animationSpec = motionScheme.fastEffectsSpec())
                    .togetherWith(fadeOut(animationSpec = motionScheme.fastEffectsSpec()))

            targetState == Screen.Memory ->
                (slideInVertically(animationSpec = motionScheme.defaultSpatialSpec()) { it } +
                    fadeIn(animationSpec = motionScheme.defaultEffectsSpec()))
                    .togetherWith(fadeOut(animationSpec = motionScheme.fastEffectsSpec()))

            targetState == Screen.Library ->
                fadeIn(animationSpec = motionScheme.defaultEffectsSpec())
                    .togetherWith(fadeOut(animationSpec = motionScheme.fastEffectsSpec()))

            else ->
                fadeIn(animationSpec = motionScheme.defaultEffectsSpec())
                    .togetherWith(fadeOut(animationSpec = motionScheme.fastEffectsSpec()))
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
