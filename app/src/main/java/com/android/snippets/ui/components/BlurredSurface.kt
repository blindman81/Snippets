package com.android.snippets.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * CompositionLocal providing ambient HazeState for surfaces to blur background content.
 */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

/**
 * A surface container that applies Haze backdrop blur (progressive vertical gradient on Android 13+)
 * to blur the content scrolling underneath it.
 *
 * On devices below Android 13, it renders as a standard 100% opaque Surface with no blur.
 *
 * All child UI elements and content stay completely flat and sharp.
 */
@Composable
fun BlurredSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    alpha: Float = 0.82f,
    blurStartRadius: Dp = 4.dp,
    blurEndRadius: Dp = 24.dp,
    border: BorderStroke? = null,
    shadowElevation: Dp = 0.dp,
    tonalElevation: Dp = 0.dp,
    contentColor: Color = contentColorFor(color),
    hazeState: HazeState? = LocalHazeState.current,
    content: @Composable () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && hazeState != null) {
        val hazeStyle = HazeStyle(
            tint = HazeTint(color.copy(alpha = alpha)),
            blurRadius = blurEndRadius,
            noiseFactor = HazeDefaults.noiseFactor
        )

        Box(
            modifier = modifier
                .then(
                    if (shadowElevation > 0.dp) {
                        Modifier.shadow(elevation = shadowElevation, shape = shape, clip = false)
                    } else Modifier
                )
                .clip(shape)
                .hazeEffect(
                    state = hazeState,
                    style = hazeStyle
                )
                .then(
                    if (border != null) {
                        Modifier.border(border, shape)
                    } else Modifier
                )
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            border = border,
            shadowElevation = shadowElevation,
            tonalElevation = tonalElevation,
            contentColor = contentColor,
            content = content
        )
    }
}

/**
 * A bottom sheet that applies Haze backdrop blur to its background surface
 * on Android 13+ devices, falling back to a standard opaque bottom sheet on older versions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlurredModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    alpha: Float = 0.82f,
    contentColor: Color = contentColorFor(containerColor),
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    hazeState: HazeState? = LocalHazeState.current,
    content: @Composable ColumnScope.() -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && hazeState != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            sheetState = sheetState,
            shape = shape,
            containerColor = Color.Transparent,
            contentColor = contentColor,
            scrimColor = scrimColor,
            dragHandle = null
        ) {
            BlurredSurface(
                shape = shape,
                color = containerColor,
                alpha = alpha,
                blurStartRadius = 4.dp,
                blurEndRadius = 24.dp,
                contentColor = contentColor,
                hazeState = hazeState,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (dragHandle != null) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            dragHandle()
                        }
                    }
                    content()
                }
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            sheetState = sheetState,
            shape = shape,
            containerColor = containerColor,
            contentColor = contentColor,
            scrimColor = scrimColor,
            dragHandle = dragHandle,
            content = content
        )
    }
}
