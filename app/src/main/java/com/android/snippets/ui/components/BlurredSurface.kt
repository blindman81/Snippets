package com.android.snippets.ui.components

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
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
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
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
 * Blends a base surface color with the system Material You Monet dynamic color (surfaceTint)
 * so that frosted glass blur across the app has a rich, vibrant Monet tint rather than looking gray.
 */
@Composable
fun rememberMonetTintedColor(
    baseColor: Color,
    tintColor: Color = MaterialTheme.colorScheme.surfaceTint,
    weight: Float = 0.0f
): Color {
    return remember(baseColor, tintColor, weight) {
        if (weight <= 0f) baseColor else {
            Color(
                red = (baseColor.red * (1f - weight) + tintColor.red * weight).coerceIn(0f, 1f),
                green = (baseColor.green * (1f - weight) + tintColor.green * weight).coerceIn(0f, 1f),
                blue = (baseColor.blue * (1f - weight) + tintColor.blue * weight).coerceIn(0f, 1f),
                alpha = baseColor.alpha
            )
        }
    }
}

/**
 * A surface container that applies Haze backdrop blur (progressive vertical gradient on Android 13+)
 * to blur the content scrolling underneath it.
 *
 * The blur is tinted with the system's Material You Monet dynamic color palette rather than gray.
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
    monetTintWeight: Float = 0.0f,
    content: @Composable () -> Unit
) {
    val tintedColor = rememberMonetTintedColor(baseColor = color, weight = monetTintWeight)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (hazeState != null) {
            val hazeStyle = HazeStyle(
                tint = HazeTint(tintedColor.copy(alpha = alpha)),
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
                    .background(tintedColor.copy(alpha = alpha.coerceIn(0f, 0.4f)))
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
                color = tintedColor.copy(alpha = alpha),
                border = border,
                shadowElevation = shadowElevation,
                tonalElevation = tonalElevation,
                contentColor = contentColor,
                content = content
            )
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
 * A bottom sheet that applies frosted glass blur with Material You Monet color tinting
 * to its background surface on Android 13+ devices.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlurredModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    alpha: Float = HazeDefaults.tintAlpha,
    contentColor: Color = contentColorFor(containerColor),
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    hazeState: HazeState? = LocalHazeState.current,
    content: @Composable ColumnScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }

    val visibleState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    val scrimAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scrimAlpha.animateTo(1f, animationSpec = tween(durationMillis = 250))
    }

    fun triggerDismiss() {
        if (!isDismissing) {
            isDismissing = true
            visibleState.targetState = false
            coroutineScope.launch {
                scrimAlpha.animateTo(0f, animationSpec = tween(durationMillis = 220))
            }
            coroutineScope.launch {
                delay(240)
                onDismissRequest()
            }
        }
    }

    BackHandler(enabled = !isDismissing, onBack = { triggerDismiss() })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(900f)
    ) {
        // Scrim backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = scrimAlpha.value }
                .background(scrimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { triggerDismiss() }
                )
        )

        val motionScheme = MaterialTheme.motionScheme

        // Sheet content
        AnimatedVisibility(
            visibleState = visibleState,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = motionScheme.fastSpatialSpec()
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            var dragOffsetY by remember { mutableFloatStateOf(0f) }
            val animatedDragOffsetY by animateFloatAsState(
                targetValue = dragOffsetY,
                animationSpec = motionScheme.defaultSpatialSpec(),
                label = "sheet_drag_offset"
            )

            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        if (dragOffsetY > 0f && available.y != 0f) {
                            val newOffset = (dragOffsetY + available.y).coerceAtLeast(0f)
                            val consumedY = newOffset - dragOffsetY
                            dragOffsetY = newOffset
                            return Offset(0f, consumedY)
                        }
                        return Offset.Zero
                    }

                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        if (available.y > 0f) {
                            dragOffsetY = (dragOffsetY + available.y).coerceAtLeast(0f)
                            return Offset(0f, available.y)
                        }
                        return Offset.Zero
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        if (dragOffsetY > 160f) {
                            triggerDismiss()
                            return available
                        } else {
                            dragOffsetY = 0f
                        }
                        return Velocity.Zero
                    }

                    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                        if (dragOffsetY > 160f || available.y > 600f) {
                            triggerDismiss()
                        } else {
                            dragOffsetY = 0f
                        }
                        return Velocity.Zero
                    }
                }
            }

            BlurredSurface(
                shape = shape,
                color = containerColor,
                alpha = alpha,
                blurStartRadius = 4.dp,
                blurEndRadius = HazeDefaults.blurRadius,
                contentColor = contentColor,
                hazeState = hazeState,
                monetTintWeight = 0.0f,
                modifier = modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, animatedDragOffsetY.roundToInt()) }
                    .nestedScroll(nestedScrollConnection)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffsetY > 160f) {
                                    triggerDismiss()
                                } else {
                                    dragOffsetY = 0f
                                }
                            },
                            onDragCancel = {
                                dragOffsetY = 0f
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                if (dragAmount > 0 || dragOffsetY > 0) {
                                    dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                                }
                            }
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(contentWindowInsets().only(WindowInsetsSides.Bottom))
                ) {
                    if (dragHandle != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            dragHandle()
                        }
                    }
                    CompositionLocalProvider(
                        LocalTiltActive provides (!isDismissing)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * An AlertDialog replacement that renders as an in-Compose modal surface with genuine frosted glass
 * blur sampling from ambient HazeState, tinted with Material You Monet dynamic color.
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
    hazeState: HazeState? = LocalHazeState.current
) {
    val coroutineScope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val scrimAlpha = remember { Animatable(0f) }

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
            .zIndex(999f),
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
            ) + fadeOut(animationSpec = motionScheme.fastEffectsSpec())
        ) {
            CompositionLocalProvider(
                LocalTiltActive provides (!isDismissing)
            ) {
                BlurredSurface(
                shape = shape,
                color = containerColor,
                alpha = 0.65f,
                shadowElevation = 8.dp,
                tonalElevation = tonalElevation,
                monetTintWeight = 0.0f,
                hazeState = hazeState,
                modifier = modifier
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
                                icon = Icons.Default.Close,
                                contentDescription = "Close",
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                size = 40.dp
                            )
                            if (onConfirm != null) {
                                AnimatedCookieButton(
                                    onClick = { triggerConfirm() },
                                    icon = Icons.Default.Check,
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

