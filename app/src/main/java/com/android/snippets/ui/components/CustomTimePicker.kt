package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.shapes.Cookie12Shape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

private enum class TimePickerMode {
    HOUR, MINUTE
}

/**
 * Custom Material 3 TimePicker featuring:
 * - 12-sided cookie shape (Cookie12Shape) clock dial container
 * - Morphing pill-shaped AM / PM toggle
 * - Smooth spring animations and haptic feedback
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomTimePicker(
    state: TimePickerState,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf(TimePickerMode.HOUR) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Top Time Display (Hour, Colon, Minute, AM/PM Toggle) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val isHourSelected = mode == TimePickerMode.HOUR
            val isMinuteSelected = mode == TimePickerMode.MINUTE

            val displayHour = if (state.is24hour) {
                state.hour
            } else {
                val h = state.hour % 12
                if (h == 0) 12 else h
            }

            // Hour Box
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isHourSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        mode = TimePickerMode.HOUR
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "%02d".format(displayHour),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isHourSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Colon Separator
            Text(
                text = ":",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Minute Box
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isMinuteSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        mode = TimePickerMode.MINUTE
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "%02d".format(state.minute),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isMinuteSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // AM / PM Toggle (Default design from latest M3 Compose docs)
            if (!state.is24hour) {
                Spacer(modifier = Modifier.width(12.dp))

                val isPm = state.hour >= 12
                val shape = RoundedCornerShape(8.dp)
                val outlineColor = MaterialTheme.colorScheme.outlineVariant
                val selectedContainer = MaterialTheme.colorScheme.primaryContainer
                val selectedContent = MaterialTheme.colorScheme.onPrimaryContainer
                val unselectedContent = MaterialTheme.colorScheme.onSurfaceVariant
                val unselectedContainer = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f)

                Column(
                    modifier = Modifier
                        .width(52.dp)
                        .height(80.dp)
                        .background(unselectedContainer, shape)
                        .border(BorderStroke(1.dp, outlineColor), shape)
                        .clip(shape)
                ) {
                    val amShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(amShape)
                            .background(if (!isPm) selectedContainer else unselectedContainer)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isPm) {
                                    state.hour -= 12
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AM",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (!isPm) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (!isPm) selectedContent else unselectedContent,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(outlineColor)
                    )

                    val pmShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(pmShape)
                            .background(if (isPm) selectedContainer else unselectedContainer)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isPm) {
                                    state.hour += 12
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PM",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isPm) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isPm) selectedContent else unselectedContent,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // --- Clock Dial with Cookie12Shape Container ---
        val dialSizeDp = 256.dp
        val density = LocalDensity.current
        val dialSizePx = with(density) { dialSizeDp.toPx() }
        val centerPx = dialSizePx / 2f
        val radiusOuterPx = with(density) { 96.dp.toPx() }
        val radiusInnerPx = with(density) { 64.dp.toPx() }

        // Compute current angle in degrees
        val targetAngleDeg = when (mode) {
            TimePickerMode.HOUR -> {
                val h = state.hour % 12
                val h12 = if (h == 0) 12 else h
                (h12 * 30f) % 360f
            }
            TimePickerMode.MINUTE -> {
                (state.minute * 6f) % 360f
            }
        }

        val animatedAngleDeg by animateFloatAsState(
            targetValue = targetAngleDeg,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "clock_selector_angle"
        )

        val isInner24Hour = state.is24hour && mode == TimePickerMode.HOUR && (state.hour == 0 || state.hour > 12)
        val targetRadiusPx = if (isInner24Hour) radiusInnerPx else radiusOuterPx
        val animatedRadiusPx by animateFloatAsState(
            targetValue = targetRadiusPx,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "clock_selector_radius"
        )

        fun handlePointer(pos: Offset, isTap: Boolean) {
            val dx = pos.x - centerPx
            val dy = pos.y - centerPx
            val dist = kotlin.math.hypot(dx, dy)
            var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
            if (deg < 0f) deg += 360f

            when (mode) {
                TimePickerMode.HOUR -> {
                    val rawHour = (round(deg / 30f).toInt() % 12).let { if (it == 0) 12 else it }
                    if (state.is24hour) {
                        val isInner = dist < (radiusInnerPx + radiusOuterPx) / 2f
                        val newHour = if (isInner) {
                            if (rawHour == 12) 0 else rawHour + 12
                        } else {
                            if (rawHour == 12) 12 else rawHour
                        }
                        if (state.hour != newHour) {
                            state.hour = newHour
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        }
                    } else {
                        val isPm = state.hour >= 12
                        val newHour = if (isPm) {
                            if (rawHour == 12) 12 else rawHour + 12
                        } else {
                            if (rawHour == 12) 0 else rawHour
                        }
                        if (state.hour != newHour) {
                            state.hour = newHour
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        }
                    }

                    if (isTap) {
                        scope.launch {
                            delay(280)
                            mode = TimePickerMode.MINUTE
                        }
                    }
                }
                TimePickerMode.MINUTE -> {
                    val newMin = (round(deg / 6f).toInt() % 60)
                    if (state.minute != newMin) {
                        state.minute = newMin
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(dialSizeDp)
                .clip(Cookie12Shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .pointerInput(mode, state.is24hour) {
                    detectTapGestures { offset ->
                        handlePointer(offset, isTap = true)
                    }
                }
                .pointerInput(mode, state.is24hour) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            handlePointer(offset, isTap = false)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            handlePointer(change.position, isTap = false)
                        },
                        onDragEnd = {
                            if (mode == TimePickerMode.HOUR) {
                                scope.launch {
                                    delay(100)
                                    mode = TimePickerMode.MINUTE
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Draw Hand & Selector Handle
            val primaryColor = MaterialTheme.colorScheme.primary
            val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

            Canvas(modifier = Modifier.fillMaxSize()) {
                val rad = (animatedAngleDeg - 90f) * (PI / 180f)
                val handleCenter = Offset(
                    x = centerPx + (animatedRadiusPx * cos(rad)).toFloat(),
                    y = centerPx + (animatedRadiusPx * sin(rad)).toFloat()
                )

                // Center pivot
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(centerPx, centerPx)
                )

                // Selector line
                drawLine(
                    color = primaryColor,
                    start = Offset(centerPx, centerPx),
                    end = handleCenter,
                    strokeWidth = 2.dp.toPx()
                )

                // Selector handle
                val handleRadius = if (state.is24hour && isInner24Hour) 16.dp.toPx() else 20.dp.toPx()
                drawCircle(
                    color = primaryColor,
                    radius = handleRadius,
                    center = handleCenter
                )
            }

            // Draw Number Labels
            if (mode == TimePickerMode.HOUR) {
                // Outer numbers (1..12)
                for (h in 1..12) {
                    val angle = (h * 30f - 90f) * (PI / 180f)
                    val x = (radiusOuterPx * cos(angle)).toFloat()
                    val y = (radiusOuterPx * sin(angle)).toFloat()

                    val isSelected = if (state.is24hour) {
                        state.hour == h || (h == 12 && state.hour == 12)
                    } else {
                        val currentH12 = (state.hour % 12).let { if (it == 0) 12 else it }
                        currentH12 == h
                    }

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x.toInt(), y.toInt()) }
                            .size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = h.toString(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Inner numbers for 24-hour mode (13..23, 00)
                if (state.is24hour) {
                    for (h in 1..12) {
                        val angle = (h * 30f - 90f) * (PI / 180f)
                        val x = (radiusInnerPx * cos(angle)).toFloat()
                        val y = (radiusInnerPx * sin(angle)).toFloat()

                        val val24 = if (h == 12) 0 else h + 12
                        val isSelected = state.hour == val24

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(x.toInt(), y.toInt()) }
                                .size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "%02d".format(val24),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            } else {
                // Minute numbers (00, 05, 10, ... 55)
                for (i in 0 until 12) {
                    val min = i * 5
                    val angle = (i * 30f - 90f) * (PI / 180f)
                    val x = (radiusOuterPx * cos(angle)).toFloat()
                    val y = (radiusOuterPx * sin(angle)).toFloat()

                    val isSelected = state.minute == min

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x.toInt(), y.toInt()) }
                            .size(44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%02d".format(min),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
