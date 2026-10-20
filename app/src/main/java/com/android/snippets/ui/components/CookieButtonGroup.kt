package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.graphics.shapes.Morph
import com.android.snippets.ui.shapes.CirclePolygon
import com.android.snippets.ui.shapes.Cookie9ToCircleMorph
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.shapes.Cookie9Shape
import com.android.snippets.ui.shapes.CookiePolygon
import com.android.snippets.ui.shapes.MorphShape
import com.android.snippets.ui.shapes.RoundedPolygonShape
import com.android.snippets.ui.util.Motion
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Item specification for [CookieButtonGroup].
 */
data class CookieButtonItem(
    val icon: @Composable (modifier: Modifier, tint: Color) -> Unit,
    val contentDescription: String? = null,
    val tooltip: String? = null,
    val enabled: Boolean = true,
    val isSelected: Boolean? = null,
    val modifier: Modifier = Modifier,
    val onClick: (() -> Unit)? = null,
    val badge: (@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null
)

fun cookieButtonItem(
    icon: ImageVector,
    contentDescription: String? = null,
    tooltip: String? = null,
    enabled: Boolean = true,
    isSelected: Boolean? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    iconSize: Dp = 22.dp,
    badge: (@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null
): CookieButtonItem {
    return CookieButtonItem(
        icon = { iconModifier, tint ->
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = iconModifier.size(iconSize),
                tint = tint
            )
        },
        contentDescription = contentDescription,
        tooltip = tooltip,
        enabled = enabled,
        isSelected = isSelected,
        modifier = modifier,
        onClick = onClick,
        badge = badge
    )
}

fun cookieButtonItem(
    @DrawableRes icon: Int,
    contentDescription: String? = null,
    tooltip: String? = null,
    enabled: Boolean = true,
    isSelected: Boolean? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    iconSize: Dp = 22.dp,
    badge: (@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null
): CookieButtonItem {
    return CookieButtonItem(
        icon = { iconModifier, tint ->
            Icon(
                painter = painterResource(id = icon),
                contentDescription = contentDescription,
                modifier = iconModifier.size(iconSize),
                tint = tint
            )
        },
        contentDescription = contentDescription,
        tooltip = tooltip,
        enabled = enabled,
        isSelected = isSelected,
        modifier = modifier,
        onClick = onClick,
        badge = badge
    )
}

fun cookieButtonItem(
    contentDescription: String? = null,
    tooltip: String? = null,
    enabled: Boolean = true,
    isSelected: Boolean? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    badge: (@Composable androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null,
    icon: @Composable (modifier: Modifier, tint: Color) -> Unit
): CookieButtonItem {
    return CookieButtonItem(
        icon = icon,
        contentDescription = contentDescription,
        tooltip = tooltip,
        enabled = enabled,
        isSelected = isSelected,
        modifier = modifier,
        onClick = onClick,
        badge = badge
    )
}

/**
 * CookieButtonGroup displays a row of connected scalloped cookie buttons where adjacent cookies
 * are literally attached to each other. When an item is switched to or tapped, it performs a 360-degree
 * bouncy spin while touching neighbors react like meshed gears.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookieButtonGroup(
    selectedIndex: Int = -1,
    onSelect: (Int) -> Unit = {},
    items: List<CookieButtonItem>,
    modifier: Modifier = Modifier,
    columns: Int = 1,
    cookieSize: Dp = 56.dp,
    cookieWidth: Dp = cookieSize,
    cookieHeight: Dp = cookieSize,
    overlap: Dp = 0.dp,
    rowSpacing: Dp = -overlap,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    shape: Shape = CookieShape,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    isSpinning: Boolean = true,
    enableWiggle: Boolean = true,
    wiggleNeighborOnToggleOn: Boolean = false,
    onItemClick: ((Int) -> Unit)? = null
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentOnItemClick by rememberUpdatedState(onItemClick)
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentItems by rememberUpdatedState(items)

    val rotations = remember(items.size) { List(items.size) { Animatable(0f) } }
    val morphProgresses = remember(items.size) { List(items.size) { Animatable(0f) } }
    val pressScales = remember(items.size) { List(items.size) { Animatable(1f) } }
    val wiggleRotations = remember(items.size) { List(items.size) { Animatable(0f) } }
    val wiggleJobs = remember(items.size) { arrayOfNulls<Job>(items.size) }
    val spinJobs = remember(items.size) { arrayOfNulls<Job>(items.size) }
    var spinningIndex by remember { mutableIntStateOf(-1) }
    var lastSelectedIndex by remember { mutableIntStateOf(selectedIndex) }

    fun triggerWiggle(targetIndex: Int, direction: Float = 1f) {
        if (!enableWiggle || targetIndex !in wiggleRotations.indices) return
        wiggleJobs[targetIndex]?.cancel()
        wiggleJobs[targetIndex] = scope.launch {
            wiggleRotations[targetIndex].snapTo(0.05f * direction)
            wiggleRotations[targetIndex].animateTo(
                targetValue = 0f,
                animationSpec = Motion.wiggleSpec(direction)
            )
        }
    }

    fun isItemPrimary(index: Int): Boolean {
        if (index !in currentItems.indices) return false
        val item = currentItems[index]
        return item.isSelected ?: (index == currentSelectedIndex)
    }

    val effectiveMorph = remember(shape) {
        if (shape is RoundedPolygonShape) {
            Morph(shape.polygon.normalized(), CirclePolygon.normalized())
        } else {
            Cookie9ToCircleMorph
        }
    }

    fun getTargetMorphIndex(heldIndex: Int): Int? {
        val listSize = currentItems.size
        if (listSize <= 1) return if (listSize > 0) heldIndex else null
        if (columns <= 1) {
            return if (heldIndex < listSize - 1) heldIndex + 1 else heldIndex - 1
        } else {
            val colIndex = heldIndex % columns
            val rowStart = (heldIndex / columns) * columns
            val rowEnd = minOf(rowStart + columns, listSize)
            val rowSize = rowEnd - rowStart
            return if (colIndex < rowSize - 1) {
                heldIndex + 1
            } else if (heldIndex > rowStart) {
                heldIndex - 1
            } else {
                heldIndex
            }
        }
    }

    fun triggerSpin(targetIndex: Int, isDirectClick: Boolean) {
        if (targetIndex !in currentItems.indices) return
        val item = currentItems[targetIndex]

        if (isDirectClick) {
            currentOnItemClick?.invoke(targetIndex)
        }

        if (item.onClick != null) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            val wasPrimary = isItemPrimary(targetIndex)

            // Perform intended action simultaneously
            item.onClick.invoke()

            if (enableWiggle) {
                // Tapping on a button wiggles the adjacent button
                val isRtl = layoutDirection == LayoutDirection.Rtl
                val (leftIndex, rightIndex) = if (columns <= 1) {
                    val left = if (targetIndex > 0) targetIndex - 1 else -1
                    val right = if (targetIndex + 1 < currentItems.size) targetIndex + 1 else -1
                    if (isRtl) Pair(right, left) else Pair(left, right)
                } else {
                    val col = targetIndex % columns
                    val rowStart = (targetIndex / columns) * columns
                    val rowEnd = minOf(rowStart + columns, currentItems.size)
                    val left = if (col > 0) targetIndex - 1 else -1
                    val right = if (targetIndex + 1 < rowEnd) targetIndex + 1 else -1
                    if (isRtl) Pair(right, left) else Pair(left, right)
                }

                if (rightIndex in currentItems.indices) {
                    triggerWiggle(rightIndex, direction = 1f)
                } else if (leftIndex in currentItems.indices) {
                    triggerWiggle(leftIndex, direction = -1f)
                }
            }

            if (isSpinning) {
                spinningIndex = targetIndex
                spinJobs[targetIndex]?.cancel()
                spinJobs[targetIndex] = scope.launch {
                    val scaleJob = launch {
                        pressScales[targetIndex].animateTo(0.84f, Motion.PressSpring)
                        pressScales[targetIndex].animateTo(1f, Motion.BouncySpring)
                    }
                    val rotationJob = launch {
                        rotations[targetIndex].animateTo(
                            targetValue = rotations[targetIndex].value + 360f,
                            animationSpec = Motion.BouncySpring
                        )
                    }

                    rotationJob.join()
                    scaleJob.join()
                    spinningIndex = -1
                }
            }
        } else {
            val previousSelected = lastSelectedIndex
            val isSwitch = targetIndex != previousSelected
            lastSelectedIndex = targetIndex

            if (isSwitch) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                if (enableWiggle && previousSelected in currentItems.indices && isDirectClick) {
                    val wasPrevPrimary = currentItems[previousSelected].isSelected ?: true
                    if (wasPrevPrimary) {
                        val wiggleDir = if (targetIndex > previousSelected) -1f else 1f
                        triggerWiggle(previousSelected, direction = wiggleDir)
                    }
                }
                currentOnSelect(targetIndex)
            } else if (isDirectClick) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                currentOnSelect(targetIndex)
            }

            if (isSpinning) {
                spinningIndex = targetIndex
                spinJobs[targetIndex]?.cancel()
                spinJobs[targetIndex] = scope.launch {
                    val scaleJob = launch {
                        pressScales[targetIndex].animateTo(0.84f, Motion.PressSpring)
                        pressScales[targetIndex].animateTo(1f, Motion.BouncySpring)
                    }
                    val rotationJob = launch {
                        rotations[targetIndex].animateTo(
                            targetValue = rotations[targetIndex].value + 360f,
                            animationSpec = Motion.BouncySpring
                        )
                    }

                    rotationJob.join()
                    scaleJob.join()
                    spinningIndex = -1
                }
            }
        }
    }

    LaunchedEffect(selectedIndex) {
        val prev = lastSelectedIndex
        val next = selectedIndex
        if (prev != next) {
            if (enableWiggle && prev in currentItems.indices) {
                val wasPrevPrimary = currentItems[prev].isSelected ?: true
                if (wasPrevPrimary) {
                    val wiggleDir = if (next >= 0) {
                        if (next > prev) -1f else 1f
                    } else 1f
                    triggerWiggle(prev, direction = wiggleDir)
                }
            }

            if (next in currentItems.indices && next >= 0) {
                triggerSpin(next, isDirectClick = false)
            } else {
                lastSelectedIndex = next
            }
        }
    }

    val renderCookieItem = @Composable { index: Int, item: CookieButtonItem ->
        val isSelected = item.isSelected ?: (index == currentSelectedIndex)

        val animatedContainerColor by animateColorAsState(
            targetValue = if (isSelected) selectedContainerColor else unselectedContainerColor,
            animationSpec = tween(durationMillis = 200),
            label = "cookie_container_color_$index"
        )
        val animatedContentColor by animateColorAsState(
            targetValue = if (isSelected) selectedContentColor else unselectedContentColor,
            animationSpec = tween(durationMillis = 200),
            label = "cookie_content_color_$index"
        )

        val isPrimary = isSelected && (selectedContainerColor == MaterialTheme.colorScheme.primary)
        val tiltAngle by com.android.snippets.ui.util.rememberCookieTilt(enabled = item.enabled && isPrimary)

        val cookieContent = @Composable {
            val currentShape = if (morphProgresses[index].value > 0.001f) {
                MorphShape(effectiveMorph, morphProgresses[index].value)
            } else {
                shape
            }

            Box(
                modifier = Modifier
                    .then(item.modifier)
                    .width(cookieWidth)
                    .height(cookieHeight)
                    .zIndex(if (morphProgresses[index].value > 0.001f || pressScales[index].value < 0.99f) 1f else 0f)
                    .graphicsLayer {
                        scaleX = pressScales[index].value
                        scaleY = pressScales[index].value
                    }
                    .semantics {
                        role = Role.Button
                        selected = isSelected
                        onClick {
                            triggerSpin(index, isDirectClick = true)
                            true
                        }
                    }
                    .pointerInput(item.enabled, items.size, index) {
                        if (!item.enabled) return@pointerInput
                        detectTapGestures(
                            onPress = {
                                val targetIndex = getTargetMorphIndex(index)
                                var isHeld = false
                                val holdJob = scope.launch {
                                    kotlinx.coroutines.delay(120L)
                                    isHeld = true
                                    launch {
                                        pressScales[index].animateTo(0.92f, Motion.PressSpring)
                                    }
                                    launch {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        morphProgresses[index].animateTo(
                                            targetValue = 1f,
                                            animationSpec = Motion.BouncySpring
                                        )
                                    }
                                    if (targetIndex != null && targetIndex != index && targetIndex in morphProgresses.indices) {
                                        launch {
                                            morphProgresses[targetIndex].animateTo(
                                                targetValue = 1f,
                                                animationSpec = Motion.BouncySpring
                                            )
                                        }
                                    }
                                }
                                try {
                                    awaitRelease()
                                } finally {
                                    holdJob.cancel()
                                    if (isHeld) {
                                        scope.launch {
                                            launch {
                                                pressScales[index].animateTo(1f, Motion.BouncySpring)
                                            }
                                            launch {
                                                morphProgresses[index].animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = Motion.BouncySpring
                                                )
                                            }
                                            if (targetIndex != null && targetIndex != index && targetIndex in morphProgresses.indices) {
                                                launch {
                                                    morphProgresses[targetIndex].animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = Motion.BouncySpring
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            onLongPress = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            },
                            onTap = {
                                triggerSpin(index, isDirectClick = true)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background scalloped cookie: spins in place
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            rotationZ = rotations[index].value + wiggleRotations[index].value + tiltAngle
                        }
                        .clip(currentShape)
                        .background(if (item.enabled) animatedContainerColor else animatedContainerColor.copy(alpha = 0.38f))
                )

                // Icon: remains strictly upright
                item.icon(
                    Modifier,
                    if (item.enabled) animatedContentColor else animatedContentColor.copy(alpha = 0.38f)
                )
            }
        }

        if (item.tooltip != null) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                tooltip = {
                    PlainTooltip(
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(item.tooltip)
                    }
                },
                state = rememberTooltipState()
            ) {
                cookieContent()
            }
        } else {
            cookieContent()
        }
    }

    if (columns <= 0) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(-overlap, horizontalAlignment),
            verticalArrangement = Arrangement.spacedBy(rowSpacing, Alignment.Top)
        ) {
            items.forEachIndexed { index, item ->
                renderCookieItem(index, item)
            }
        }
    } else if (columns == 1) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(-overlap, horizontalAlignment),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                renderCookieItem(index, item)
            }
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(rowSpacing, Alignment.CenterVertically),
            horizontalAlignment = horizontalAlignment
        ) {
            val chunkedItems = items.chunked(columns)
            chunkedItems.forEachIndexed { rowIndex, rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(-overlap, horizontalAlignment),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowItems.forEachIndexed { colIndex, item ->
                        val itemIndex = rowIndex * columns + colIndex
                        renderCookieItem(itemIndex, item)
                    }
                }
            }
        }
    }
}

/**
 * Convenience overload for [CookieButtonGroup] taking a list of [ImageVector] icons.
 */
@Composable
fun CookieButtonGroup(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    icons: List<ImageVector>,
    modifier: Modifier = Modifier,
    contentDescriptions: List<String?> = emptyList(),
    tooltips: List<String?> = emptyList(),
    columns: Int = 1,
    cookieSize: Dp = 56.dp,
    cookieWidth: Dp = cookieSize,
    cookieHeight: Dp = cookieSize,
    overlap: Dp = 0.dp,
    rowSpacing: Dp = -overlap,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    shape: Shape = CookieShape,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    isSpinning: Boolean = true,
    enableWiggle: Boolean = true,
    wiggleNeighborOnToggleOn: Boolean = false,
    onItemClick: ((Int) -> Unit)? = null,
    iconSize: Dp = 22.dp
) {
    val items = remember(icons, contentDescriptions, tooltips, iconSize) {
        icons.mapIndexed { index, icon ->
            val cd = contentDescriptions.getOrNull(index)
            val tt = tooltips.getOrNull(index) ?: cd
            cookieButtonItem(icon = icon, contentDescription = cd, tooltip = tt, iconSize = iconSize)
        }
    }
    CookieButtonGroup(
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        items = items,
        modifier = modifier,
        columns = columns,
        cookieSize = cookieSize,
        cookieWidth = cookieWidth,
        cookieHeight = cookieHeight,
        overlap = overlap,
        rowSpacing = rowSpacing,
        horizontalAlignment = horizontalAlignment,
        shape = shape,
        selectedContainerColor = selectedContainerColor,
        selectedContentColor = selectedContentColor,
        unselectedContainerColor = unselectedContainerColor,
        unselectedContentColor = unselectedContentColor,
        isSpinning = isSpinning,
        enableWiggle = enableWiggle,
        wiggleNeighborOnToggleOn = wiggleNeighborOnToggleOn,
        onItemClick = onItemClick
    )
}

/**
 * CookieNavBar alias for [CookieButtonGroup] as requested for cookie nav bar and cookie groupings.
 */
@Composable
fun CookieNavBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    items: List<CookieButtonItem>,
    modifier: Modifier = Modifier,
    columns: Int = 1,
    cookieSize: Dp = 56.dp,
    overlap: Dp = 0.dp,
    shape: Shape = CookieShape,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    isSpinning: Boolean = true,
    enableWiggle: Boolean = true,
    wiggleNeighborOnToggleOn: Boolean = false
) = CookieButtonGroup(
    selectedIndex = selectedIndex,
    onSelect = onSelect,
    items = items,
    modifier = modifier,
    columns = columns,
    cookieSize = cookieSize,
    overlap = overlap,
    shape = shape,
    selectedContainerColor = selectedContainerColor,
    selectedContentColor = selectedContentColor,
    unselectedContainerColor = unselectedContainerColor,
    unselectedContentColor = unselectedContentColor,
    isSpinning = isSpinning,
    enableWiggle = enableWiggle,
    wiggleNeighborOnToggleOn = wiggleNeighborOnToggleOn
)

/**
 * Backward compatibility alias for [CookieNavBar].
 */
@Deprecated("Use CookieNavBar instead", ReplaceWith("CookieNavBar(selectedIndex, onSelect, items, modifier, columns, cookieSize, overlap, shape, selectedContainerColor, selectedContentColor, unselectedContainerColor, unselectedContentColor, isSpinning, enableWiggle, wiggleNeighborOnToggleOn)"))
@Composable
fun TabNavCookie(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    items: List<CookieButtonItem>,
    modifier: Modifier = Modifier,
    columns: Int = 1,
    cookieSize: Dp = 56.dp,
    overlap: Dp = 0.dp,
    shape: Shape = CookieShape,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    isSpinning: Boolean = true,
    enableWiggle: Boolean = true,
    wiggleNeighborOnToggleOn: Boolean = false
) = CookieNavBar(
    selectedIndex = selectedIndex,
    onSelect = onSelect,
    items = items,
    modifier = modifier,
    columns = columns,
    cookieSize = cookieSize,
    overlap = overlap,
    shape = shape,
    selectedContainerColor = selectedContainerColor,
    selectedContentColor = selectedContentColor,
    unselectedContainerColor = unselectedContainerColor,
    unselectedContentColor = unselectedContentColor,
    isSpinning = isSpinning,
    enableWiggle = enableWiggle,
    wiggleNeighborOnToggleOn = wiggleNeighborOnToggleOn
)
