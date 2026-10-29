package com.android.snippets.ui.components

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.Job
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.android.snippets.ui.util.LocalTiltActive
import com.android.snippets.ui.util.LocalTiltModalFocus
import kotlin.math.roundToInt
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
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
 * CompositionLocal indicating whether the content is currently hosted within a [BlurredSurface].
 */
val LocalIsOnBlurredSurface = compositionLocalOf { false }

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
    alpha: Float = HazeDefaults.tintAlpha,
    blurStartRadius: Dp = 4.dp,
    blurEndRadius: Dp = HazeDefaults.blurRadius,
    border: BorderStroke? = null,
    shadowElevation: Dp = 0.dp,
    tonalElevation: Dp = 0.dp,
    contentColor: Color = contentColorFor(color),
    hazeState: HazeState? = LocalHazeState.current,
    content: @Composable () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (hazeState != null) {
            val hazeStyle = remember(color, alpha, blurEndRadius) {
                HazeStyle(
                    tint = HazeTint(color.copy(alpha = alpha)),
                    blurRadius = blurEndRadius,
                    noiseFactor = HazeDefaults.noiseFactor
                )
            }

            Box(
                modifier = modifier
                    .then(
                        if (shadowElevation > 0.dp) {
                            Modifier.shadow(elevation = shadowElevation, shape = shape, clip = false)
                        } else Modifier
                    )
                    .clip(shape)
                    .background(color.copy(alpha = alpha.coerceIn(0f, 0.4f)))
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
                CompositionLocalProvider(
                    LocalContentColor provides contentColor,
                    LocalIsOnBlurredSurface provides true
                ) {
                    content()
                }
            }
        } else {
            Surface(
                modifier = modifier,
                shape = shape,
                color = color.copy(alpha = alpha),
                border = border,
                shadowElevation = shadowElevation,
                tonalElevation = tonalElevation,
                contentColor = contentColor
            ) {
                CompositionLocalProvider(LocalIsOnBlurredSurface provides true) {
                    content()
                }
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
            contentColor = contentColor
        ) {
            CompositionLocalProvider(LocalIsOnBlurredSurface provides true) {
                content()
            }
        }
    }
}


/**
 * Calculates the vertical upward offset required when the soft keyboard overlaps
 * with the bottom of a centered alert dialog.
 *
 * @param containerHeight The total height of the screen/container
 * @param dialogHeight The height of the dialog card
 * @param imeBottom The bottom inset of the keyboard in pixels
 * @param statusBarTop The top inset of the status bar in pixels
 * @param marginPx Safety margin in pixels between the dialog bottom and the keyboard
 * @return The number of pixels to move the dialog up (>= 0)
 */
internal fun calculateKeyboardOverlapOffset(
    containerHeight: Int,
    dialogHeight: Int,
    imeBottom: Int,
    statusBarTop: Int,
    marginPx: Int
): Int {
    if (imeBottom <= 0 || containerHeight <= 0 || dialogHeight <= 0) {
        return 0
    }

    val defaultDialogBottom = (containerHeight + dialogHeight) / 2
    val keyboardTop = containerHeight - imeBottom

    val overlap = (defaultDialogBottom + marginPx) - keyboardTop
    if (overlap <= 0) {
        return 0
    }

    val defaultDialogTop = (containerHeight - dialogHeight) / 2
    val maxMoveUp = (defaultDialogTop - (statusBarTop + marginPx)).coerceAtLeast(0)
    return overlap.coerceAtMost(maxMoveUp)
}

/**
 * An AlertDialog replacement that renders as an in-Compose modal surface with genuine frosted glass
 * blur sampling from ambient HazeState.
 *
 * It does NOT blur the rest of the screen; only the dialog surface card has the frosted glass blur.
 */
@Composable
fun BlurredAlertDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onConfirm: (() -> Unit)? = null,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(28.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    iconContentColor: Color = MaterialTheme.colorScheme.primary,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    textContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    tonalElevation: Dp = 6.dp,
    properties: DialogProperties = DialogProperties(),
    hazeState: HazeState? = LocalHazeState.current,
    moveUpOnKeyboardOverlap: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val scrimAlpha = remember { Animatable(0f) }

    var containerHeight by remember { mutableIntStateOf(0) }
    var dialogHeight by remember { mutableIntStateOf(0) }

    val imeInsets = WindowInsets.ime
    val statusBarInsets = WindowInsets.statusBars

    val keyboardOffsetModifier = if (moveUpOnKeyboardOverlap) {
        Modifier.offset {
            val imeBottom = imeInsets.getBottom(this)
            val statusBarTop = statusBarInsets.getTop(this)
            val marginPx = 16.dp.roundToPx()

            val moveUp = calculateKeyboardOverlapOffset(
                containerHeight = containerHeight,
                dialogHeight = dialogHeight,
                imeBottom = imeBottom,
                statusBarTop = statusBarTop,
                marginPx = marginPx
            )

            if (moveUp > 0) {
                IntOffset(x = 0, y = -moveUp)
            } else {
                IntOffset.Zero
            }
        }
    } else {
        Modifier
    }

    DisposableEffect(Unit) {
        com.android.snippets.ui.util.CookieTiltController.beginTiltModalFocus()
        onDispose {
            com.android.snippets.ui.util.CookieTiltController.endTiltModalFocus()
        }
    }

    LaunchedEffect(Unit) {
        scrimAlpha.animateTo(1f, animationSpec = tween(durationMillis = 200))
    }

    val triggerDismiss: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            visibleState.targetState = false
            coroutineScope.launch {
                scrimAlpha.animateTo(0f, animationSpec = tween(durationMillis = 200))
            }
            coroutineScope.launch {
                delay(220)
                onDismissRequest()
            }
        }
    }

    val triggerConfirm: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            visibleState.targetState = false
            coroutineScope.launch {
                scrimAlpha.animateTo(0f, animationSpec = tween(durationMillis = 200))
            }
            coroutineScope.launch {
                delay(220)
                onConfirm?.invoke()
            }
        }
    }

    BackHandler(enabled = !isDismissing, onBack = { triggerDismiss() })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f)
            .onSizeChanged { containerHeight = it.height },
        contentAlignment = Alignment.Center
    ) {
        // Scrim backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = scrimAlpha.value }
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { triggerDismiss() }
                )
        )

        val motionScheme = MaterialTheme.motionScheme

        AnimatedVisibility(
            visibleState = visibleState,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ) + fadeIn(animationSpec = motionScheme.defaultEffectsSpec()),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = motionScheme.fastSpatialSpec()
            ) + fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
            modifier = keyboardOffsetModifier
        ) {
            CompositionLocalProvider(
                LocalTiltActive provides (!isDismissing),
                LocalTiltModalFocus provides true
            ) {
                BlurredSurface(
                shape = shape,
                color = containerColor,
                alpha = 0.65f,
                shadowElevation = 8.dp,
                tonalElevation = tonalElevation,
                hazeState = hazeState,
                modifier = modifier
                    .onSizeChanged { dialogHeight = it.height }
                    .padding(horizontal = 24.dp)
                    .widthIn(min = 280.dp, max = 560.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (text != null) 16.dp else 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (icon != null) {
                                CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                                    icon()
                                }
                            }
                            if (title != null) {
                                CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                                    ProvideTextStyle(
                                        MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = titleContentColor
                                        )
                                    ) {
                                        title()
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedCookieButton(
                                onClick = { triggerDismiss() },
                                icon = Icons.Rounded.Close,
                                contentDescription = "Close",
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                size = 40.dp
                            )
                            if (onConfirm != null) {
                                AnimatedCookieButton(
                                    onClick = { triggerConfirm() },
                                    icon = Icons.Rounded.Check,
                                    contentDescription = "Confirm",
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    size = 40.dp
                                )
                            }
                        }
                    }

                    if (text != null) {
                        val hasBottomButtons = confirmButton != null || dismissButton != null
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (hasBottomButtons) Modifier.padding(bottom = 24.dp) else Modifier)
                                .align(Alignment.Start)
                        ) {
                            CompositionLocalProvider(LocalContentColor provides textContentColor) {
                                ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                                    text()
                                }
                            }
                        }
                    }

                    if (confirmButton != null || dismissButton != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (dismissButton != null) {
                                dismissButton()
                                Spacer(Modifier.width(8.dp))
                            }
                            confirmButton?.invoke()
                        }
                    }
                }
            }
        }
    }
}
}

