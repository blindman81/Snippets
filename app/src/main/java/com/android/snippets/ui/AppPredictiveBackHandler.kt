package com.android.snippets.ui

import androidx.activity.BackEventCompat
import androidx.activity.ExperimentalActivityApi
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CancellationException

@Stable
class PredictiveBackMotionController(
    private val progress: Animatable<Float, AnimationVector1D>,
    private val onEdgeChanged: (Int) -> Unit
) {
    suspend fun update(backEvent: BackEventCompat) {
        onEdgeChanged(backEvent.swipeEdge)
        progress.snapTo(backEvent.progress)
    }

    suspend fun finish() {
        progress.snapTo(1f)
    }

    suspend fun cancel(animationSpec: AnimationSpec<Float>) {
        progress.animateTo(0f, animationSpec)
    }

    suspend fun reset() {
        progress.snapTo(0f)
    }
}

val LocalPredictiveBackMotionController =
    staticCompositionLocalOf<PredictiveBackMotionController?> { null }

@OptIn(ExperimentalActivityApi::class)
@Composable
fun AppPredictiveBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    val currentOnBack by rememberUpdatedState(onBack)
    val motionController = LocalPredictiveBackMotionController.current
    val motionScheme = MaterialTheme.motionScheme

    PredictiveBackHandler(enabled = enabled) { backEvents ->
        var completed = false
        try {
            backEvents.collect { backEvent ->
                motionController?.update(backEvent)
            }
            completed = true
            motionController?.finish()
            currentOnBack()
        } catch (e: CancellationException) {
            motionController?.cancel(motionScheme.fastEffectsSpec())
            throw e
        } finally {
            if (completed) {
                motionController?.reset()
            }
        }
    }
}
