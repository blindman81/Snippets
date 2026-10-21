package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.android.snippets.model.Photo
import com.android.snippets.ui.DownloadMaterialSymbol
import com.android.snippets.ui.ShareOutlinedMaterialSymbol
import com.android.snippets.ui.KidStarOutlinedMaterialSymbol
import com.android.snippets.ui.DeleteMaterialSymbol
import com.ln.android.snippets.R
import com.android.snippets.ui.shapes.CirclePolygon
import com.android.snippets.ui.shapes.Cookie9ToCircleMorph
import com.android.snippets.ui.shapes.CookiePolygon
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.shapes.Cookie9Shape
import com.android.snippets.ui.shapes.MorphShape
import com.android.snippets.ui.shapes.RoundedPolygonShape
import com.android.snippets.ui.util.Motion
import com.android.snippets.ui.MoreHorizMaterialSymbol
import com.android.snippets.ui.MoreHorizFilledMaterialSymbol
import com.android.snippets.ui.MoreHorizOutlinedMaterialSymbol
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * PhotoCookieSplitButton component consisting of two connected cookie-shaped buttons:
 * A primary action button (e.g. Add), and a trailing dropdown arrow button.
 *
 * Features:
 * - Circle shape morph on hold: holding a button morphs it into a circle.
 * - Tapping a button turns it primary.
 * - Wiggle physics on the adjacent button on tap.
 * - Only after these animations complete does the action (alert dialog, share sheet, etc.) open.
 * - Opens a connected button group menu with customizable columns (default 3 columns matching design).
 */
@Composable
fun PhotoCookieSplitButton(
    primaryIcon: ImageVector,
    onMenuToggle: (() -> Unit)? = null,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    trailingFilledIcon: ImageVector = MoreHorizFilledMaterialSymbol,
    trailingOutlinedIcon: ImageVector = MoreHorizOutlinedMaterialSymbol,
    items: List<CookieButtonItem> = emptyList(),
    primaryText: String? = null,
    contentDescription: String? = primaryText,
    isPrimaryActive: Boolean = false,
    menuColumns: Int = 2,
    buttonSize: Dp = 64.dp,
    gap: Dp = 1.dp,
    overlap: Dp = 4.dp,
    shape: Shape = CookieShape,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    isSpinning: Boolean = true,
    enabled: Boolean = true,
    isOnBlurredSurface: Boolean = LocalIsOnBlurredSurface.current
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    val actualTrailingIcon = trailingIcon ?: if (expanded) trailingFilledIcon else trailingOutlinedIcon

    val effectiveMorph = remember(shape) {
        if (shape is RoundedPolygonShape) {
            androidx.graphics.shapes.Morph(shape.polygon.normalized(), CirclePolygon.normalized())
        } else {
            Cookie9ToCircleMorph
        }
    }

    // Leading button animatables
    val rotationLeading = remember { Animatable(0f) }
    val morphProgressLeading = remember { Animatable(0f) }
    val pressScaleLeading = remember { Animatable(1f) }
    val wiggleLeading = remember { Animatable(0f) }
    var wiggleLeadingJob by remember { mutableStateOf<Job?>(null) }
    var spinLeadingJob by remember { mutableStateOf<Job?>(null) }
    var tappedLeading by remember { mutableStateOf(false) }

    // Trailing button animatables
    val rotationTrailing = remember { Animatable(0f) }
    val morphProgressTrailing = remember { Animatable(0f) }
    val pressScaleTrailing = remember { Animatable(1f) }
    val wiggleTrailing = remember { Animatable(0f) }
    var wiggleTrailingJob by remember { mutableStateOf<Job?>(null) }
    var spinTrailingJob by remember { mutableStateOf<Job?>(null) }
    var tappedTrailing by remember { mutableStateOf(false) }

    LaunchedEffect(isPrimaryActive) {
        morphProgressLeading.animateTo(if (isPrimaryActive) 1f else 0f, Motion.BouncySpring)
    }

    LaunchedEffect(expanded) {
        morphProgressTrailing.animateTo(if (expanded) 1f else 0f, Motion.BouncySpring)
    }

    fun triggerWiggleLeading(direction: Float = -1f) {
        wiggleLeadingJob?.cancel()
        wiggleLeadingJob = scope.launch {
            wiggleLeading.snapTo(0.05f * direction)
            wiggleLeading.animateTo(0f, animationSpec = Motion.wiggleSpec(direction))
        }
    }

    fun triggerWiggleTrailing(direction: Float = 1f) {
        wiggleTrailingJob?.cancel()
        wiggleTrailingJob = scope.launch {
            wiggleTrailing.snapTo(0.05f * direction)
            wiggleTrailing.animateTo(0f, animationSpec = Motion.wiggleSpec(direction))
        }
    }

    // Colors
    val isLeadingPrimary = isPrimaryActive || tappedLeading
    val isLeadingCallerPrimary = unselectedContainerColor == MaterialTheme.colorScheme.primary
    val leadingPrimaryAlpha by animateFloatAsState(
        targetValue = if (isLeadingPrimary || isLeadingCallerPrimary) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "split_leading_primary_alpha"
    )

    val animatedLeadingContainerColor by animateColorAsState(
        targetValue = if (isLeadingPrimary) selectedContainerColor else unselectedContainerColor,
        animationSpec = tween(durationMillis = 200),
        label = "split_leading_container"
    )
    val animatedLeadingContentColor by animateColorAsState(
        targetValue = if (isLeadingPrimary || isLeadingCallerPrimary) selectedContentColor else unselectedContentColor,
        animationSpec = tween(durationMillis = 200),
        label = "split_leading_content"
    )

    val isTrailingPrimary = expanded || tappedTrailing
    val isTrailingCallerPrimary = unselectedContainerColor == MaterialTheme.colorScheme.primary
    val trailingPrimaryAlpha by animateFloatAsState(
        targetValue = if (isTrailingPrimary || isTrailingCallerPrimary) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "split_trailing_primary_alpha"
    )

    val animatedTrailingContainerColor by animateColorAsState(
        targetValue = if (isTrailingPrimary) selectedContainerColor else unselectedContainerColor,
        animationSpec = tween(durationMillis = 200),
        label = "split_trailing_container"
    )
    val animatedTrailingContentColor by animateColorAsState(
        targetValue = if (isTrailingPrimary || isTrailingCallerPrimary) selectedContentColor else unselectedContentColor,
        animationSpec = tween(durationMillis = 200),
        label = "split_trailing_content"
    )

    val tiltLeading by com.android.snippets.ui.util.rememberCookieTilt(enabled = enabled && isLeadingPrimary)
    val tiltTrailing by com.android.snippets.ui.util.rememberCookieTilt(enabled = enabled && isTrailingPrimary)

    val shapeLeading = if (morphProgressLeading.value > 0.001f) {
        MorphShape(effectiveMorph, morphProgressLeading.value)
    } else {
        shape
    }

    val shapeTrailing = if (morphProgressTrailing.value > 0.001f) {
        MorphShape(effectiveMorph, morphProgressTrailing.value)
    } else {
        shape
    }

    Box(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading (Primary) Button
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .zIndex(if (morphProgressLeading.value > 0.001f || pressScaleLeading.value < 0.99f || isLeadingPrimary) 2f else 1f)
                    .graphicsLayer {
                        scaleX = pressScaleLeading.value
                        scaleY = pressScaleLeading.value
                    }
                    .semantics { role = Role.Button }
                    .pointerInput(enabled, isPrimaryActive) {
                        if (!enabled) return@pointerInput
                        detectTapGestures(
                            onPress = {
                                var isHeld = false
                                val holdJob = scope.launch {
                                    kotlinx.coroutines.delay(120L)
                                    isHeld = true
                                    launch { pressScaleLeading.animateTo(0.92f, Motion.PressSpring) }
                                    launch {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        morphProgressLeading.animateTo(1f, Motion.BouncySpring)
                                    }
                                    launch {
                                        morphProgressTrailing.animateTo(1f, Motion.BouncySpring)
                                    }
                                }
                                try {
                                    awaitRelease()
                                } finally {
                                    holdJob.cancel()
                                    if (isHeld) {
                                        scope.launch {
                                            launch { pressScaleLeading.animateTo(1f, Motion.BouncySpring) }
                                            launch { morphProgressLeading.animateTo(if (isPrimaryActive) 1f else 0f, Motion.BouncySpring) }
                                            launch { morphProgressTrailing.animateTo(if (expanded) 1f else 0f, Motion.BouncySpring) }
                                        }
                                    }
                                }
                            },
                            onTap = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                tappedLeading = true

                                // Perform action simultaneously
                                onPrimaryClick()

                                // Wiggle adjacent button (trailing button) - only morph when held
                                triggerWiggleTrailing(1f)

                                spinLeadingJob?.cancel()
                                spinLeadingJob = scope.launch {
                                    launch {
                                        pressScaleLeading.animateTo(0.92f, Motion.PressSpring)
                                        pressScaleLeading.animateTo(
                                            1f,
                                            androidx.compose.animation.core.spring(
                                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    launch {
                                        morphProgressLeading.animateTo(1f, Motion.BouncySpring)
                                    }
                                    if (isSpinning) {
                                        launch {
                                            rotationLeading.animateTo(
                                                rotationLeading.value + 360f,
                                                Motion.BouncySpring
                                            )
                                        }
                                    }
                                    kotlinx.coroutines.delay(250L)
                                    tappedLeading = false
                                    if (!isPrimaryActive) {
                                        morphProgressLeading.animateTo(0f, Motion.BouncySpring)
                                    }
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = rotationLeading.value + wiggleLeading.value + tiltLeading
                        }
                ) {
                    if (isOnBlurredSurface) {
                        val secondaryBg = if (enabled) unselectedContainerColor else unselectedContainerColor.copy(alpha = 0.38f)
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .border(
                                    border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
                                    shape = shapeLeading
                                )
                                .clip(shapeLeading)
                                .background(secondaryBg)
                        )
                        if (leadingPrimaryAlpha > 0.001f) {
                            val primaryBg = if (enabled) selectedContainerColor else selectedContainerColor.copy(alpha = 0.38f)
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .graphicsLayer { alpha = leadingPrimaryAlpha }
                                    .border(1.dp, primaryBg, shapeLeading)
                                    .clip(shapeLeading)
                                    .background(primaryBg)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(shapeLeading)
                                .background(if (enabled) animatedLeadingContainerColor else animatedLeadingContainerColor.copy(alpha = 0.38f))
                        )
                    }
                }

                Icon(
                    imageVector = primaryIcon,
                    contentDescription = contentDescription ?: primaryText,
                    modifier = Modifier.size(buttonSize * 0.5f),
                    tint = if (enabled) animatedLeadingContentColor else animatedLeadingContentColor.copy(alpha = 0.38f)
                )
            }

            // Trailing (Menu Trigger) Button
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .zIndex(if (morphProgressTrailing.value > 0.001f || pressScaleTrailing.value < 0.99f || expanded) 2f else 0f)
                    .graphicsLayer {
                        scaleX = pressScaleTrailing.value
                        scaleY = pressScaleTrailing.value
                    }
                    .semantics { role = Role.Button }
                    .pointerInput(enabled) {
                        if (!enabled) return@pointerInput
                        detectTapGestures(
                            onPress = {
                                var isHeld = false
                                val holdJob = scope.launch {
                                    kotlinx.coroutines.delay(120L)
                                    isHeld = true
                                    launch { pressScaleTrailing.animateTo(0.92f, Motion.PressSpring) }
                                    launch {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        morphProgressTrailing.animateTo(1f, Motion.BouncySpring)
                                    }
                                    launch {
                                        morphProgressLeading.animateTo(1f, Motion.BouncySpring)
                                    }
                                }
                                try {
                                    awaitRelease()
                                } finally {
                                    holdJob.cancel()
                                    if (isHeld) {
                                        scope.launch {
                                            launch { pressScaleTrailing.animateTo(1f, Motion.BouncySpring) }
                                            launch { morphProgressTrailing.animateTo(if (expanded) 1f else 0f, Motion.BouncySpring) }
                                            launch { morphProgressLeading.animateTo(if (isPrimaryActive) 1f else 0f, Motion.BouncySpring) }
                                        }
                                    }
                                }
                            },
                            onTap = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                tappedTrailing = true

                                // Perform menu toggle simultaneously
                                expanded = !expanded
                                if (expanded) onMenuToggle?.invoke()

                                // Wiggle adjacent button (leading button) - only morph when held
                                triggerWiggleLeading(-1f)

                                spinTrailingJob?.cancel()
                                spinTrailingJob = scope.launch {
                                    launch {
                                        pressScaleTrailing.animateTo(0.92f, Motion.PressSpring)
                                        pressScaleTrailing.animateTo(
                                            1f,
                                            androidx.compose.animation.core.spring(
                                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    tappedTrailing = false
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = rotationTrailing.value + wiggleTrailing.value + tiltTrailing
                        }
                ) {
                    if (isOnBlurredSurface) {
                        val secondaryBg = if (enabled) unselectedContainerColor else unselectedContainerColor.copy(alpha = 0.38f)
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .border(
                                    border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
                                    shape = shapeTrailing
                                )
                                .clip(shapeTrailing)
                                .background(secondaryBg)
                        )
                        if (trailingPrimaryAlpha > 0.001f) {
                            val primaryBg = if (enabled) selectedContainerColor else selectedContainerColor.copy(alpha = 0.38f)
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .graphicsLayer { alpha = trailingPrimaryAlpha }
                                    .border(1.dp, primaryBg, shapeTrailing)
                                    .clip(shapeTrailing)
                                    .background(primaryBg)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(shapeTrailing)
                                .background(if (enabled) animatedTrailingContainerColor else animatedTrailingContainerColor.copy(alpha = 0.38f))
                        )
                    }
                }

                Icon(
                    imageVector = actualTrailingIcon,
                    contentDescription = "More options",
                    modifier = Modifier.size(buttonSize * 0.5f),
                    tint = if (enabled) animatedTrailingContentColor else animatedTrailingContentColor.copy(alpha = 0.38f)
                )
            }
        }

        // Button Group Menu (Dropdown) - 2 button CookieButtonGroup
        if (items.isNotEmpty()) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = androidx.compose.ui.unit.DpOffset(x = 0.dp, y = 8.dp),
                containerColor = Color.Transparent,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = null,
                modifier = Modifier.background(Color.Transparent)
            ) {
                BlurredSurface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp
                ) {
                    val menuItems = items.map { item ->
                        item.copy(
                            onClick = item.onClick?.let { origClick ->
                                {
                                    expanded = false
                                    origClick()
                                }
                            }
                        )
                    }

                    Box(modifier = Modifier.padding(8.dp)) {
                        CookieButtonGroup(
                            selectedIndex = -1,
                            items = menuItems,
                            columns = menuColumns,
                            cookieSize = buttonSize,
                            overlap = -1.dp,
                            rowSpacing = 1.dp,
                            shape = shape,
                            selectedContainerColor = selectedContainerColor,
                            selectedContentColor = selectedContentColor,
                            unselectedContainerColor = unselectedContainerColor,
                            unselectedContentColor = unselectedContentColor,
                            isSpinning = true,
                            enableWiggle = true,
                            wiggleNeighborOnToggleOn = true
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SplitButton(
    primaryIcon: ImageVector,
    primaryText: String? = null,
    contentDescription: String? = primaryText,
    onPrimaryClick: () -> Unit,
    dropdownContent: @Composable ColumnScope.(() -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val view = LocalView.current

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )

    val leadingShapes = SplitButtonDefaults.leadingButtonShapesFor(48.dp)
    val trailingShapes = SplitButtonDefaults.trailingButtonShapesFor(48.dp)

    Box(modifier = modifier) {
        SplitButtonLayout(
            leadingButton = {
                SplitButtonDefaults.LeadingButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onPrimaryClick()
                    },
                    shapes = leadingShapes
                ) {
                    Icon(
                        imageVector = primaryIcon,
                        contentDescription = contentDescription ?: primaryText,
                        modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize)
                    )
                    if (!primaryText.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = primaryText)
                    }
                }
            },
            trailingButton = {
                Box {
                    SplitButtonDefaults.TrailingButton(
                        checked = expanded,
                        onCheckedChange = { isChecked ->
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            expanded = isChecked
                        },
                        shapes = trailingShapes
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "More options",
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        shape = RoundedCornerShape(12.dp),
                        offset = androidx.compose.ui.unit.DpOffset(0.dp, 4.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            dropdownContent { expanded = false }
                        }
                    }
                }
            }
        )
    }
}

@Deprecated("Use PhotoCookieSplitButton instead", ReplaceWith("PhotoCookieSplitButton(primaryIcon, onMenuToggle, onPrimaryClick, modifier, trailingIcon, trailingFilledIcon, trailingOutlinedIcon, items, primaryText, contentDescription, isPrimaryActive, menuColumns, buttonSize, gap, overlap, shape, selectedContainerColor, selectedContentColor, unselectedContainerColor, unselectedContentColor, isSpinning, enabled, isOnBlurredSurface)"))
@Composable
fun CookieSplitButton(
    primaryIcon: ImageVector,
    onMenuToggle: (() -> Unit)? = null,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    trailingFilledIcon: ImageVector = MoreHorizFilledMaterialSymbol,
    trailingOutlinedIcon: ImageVector = MoreHorizOutlinedMaterialSymbol,
    items: List<CookieButtonItem> = emptyList(),
    primaryText: String? = null,
    contentDescription: String? = primaryText,
    isPrimaryActive: Boolean = false,
    menuColumns: Int = 2,
    buttonSize: Dp = 64.dp,
    gap: Dp = 1.dp,
    overlap: Dp = 4.dp,
    shape: Shape = CookieShape,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    isSpinning: Boolean = true,
    enabled: Boolean = true,
    isOnBlurredSurface: Boolean = LocalIsOnBlurredSurface.current
) {
    PhotoCookieSplitButton(
        primaryIcon = primaryIcon,
        onMenuToggle = onMenuToggle,
        onPrimaryClick = onPrimaryClick,
        modifier = modifier,
        trailingIcon = trailingIcon,
        trailingFilledIcon = trailingFilledIcon,
        trailingOutlinedIcon = trailingOutlinedIcon,
        items = items,
        primaryText = primaryText,
        contentDescription = contentDescription,
        isPrimaryActive = isPrimaryActive,
        menuColumns = menuColumns,
        buttonSize = buttonSize,
        gap = gap,
        overlap = overlap,
        shape = shape,
        selectedContainerColor = selectedContainerColor,
        selectedContentColor = selectedContentColor,
        unselectedContainerColor = unselectedContainerColor,
        unselectedContentColor = unselectedContentColor,
        isSpinning = isSpinning,
        enabled = enabled,
        isOnBlurredSurface = isOnBlurredSurface
    )
}

/**
 * Photo-specific overload of [PhotoCookieSplitButton].
 * Displays the primary Add Snippet action and the 8 photo actions (2 per row in 4 rows).
 */
@Composable
fun PhotoCookieSplitButton(
    photo: Photo,
    onAdd: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNamePhotoClick: () -> Unit,
    onAddLinkClick: () -> Unit,
    onRate: () -> Unit,
    onSnippets: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimaryActive: Boolean = false,
    buttonSize: Dp = 64.dp
) {
    val view = LocalView.current
    val hasSnippets = photo.snippets.isNotEmpty()
    val isFavorite = photo.isFavorite
    val hasName = !photo.name.isNullOrBlank()
    val hasLocation = !photo.locationLink.isNullOrBlank()
    val hasRating = photo.rating > 0

    PhotoCookieSplitButton(
        primaryIcon = Icons.Default.Add,
        contentDescription = "Add snippets",
        onPrimaryClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onAdd()
        },
        isPrimaryActive = isPrimaryActive,
        menuColumns = 2,
        buttonSize = buttonSize,
        modifier = modifier,
        items = run {
            val iconSize = (buttonSize * 0.44f).coerceAtLeast(24.dp)
            listOf(
                cookieButtonItem(
                    icon = { mod, tint ->
                        Icon(
                            imageVector = DownloadMaterialSymbol,
                            contentDescription = "Download",
                            modifier = mod.size(iconSize),
                            tint = tint
                        )
                    },
                    tooltip = "Download",
                    enabled = hasSnippets,
                    onClick = onDownload
                ),
                cookieButtonItem(
                    icon = { mod, tint ->
                        Icon(
                            imageVector = ShareOutlinedMaterialSymbol,
                            contentDescription = "Share",
                            modifier = mod.size(iconSize),
                            tint = tint
                        )
                    },
                    tooltip = "Share",
                    enabled = hasSnippets,
                    onClick = onShare
                ),
                cookieButtonItem(
                    icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                    tooltip = if (isFavorite) "Unfavorite" else "Favorite",
                    isSelected = isFavorite,
                    iconSize = iconSize,
                    onClick = onToggleFavorite
                ),
                cookieButtonItem(
                    icon = if (hasName) Icons.AutoMirrored.Filled.Label else Icons.AutoMirrored.Outlined.Label,
                    contentDescription = if (hasName) "Rename photo" else "Name photo",
                    tooltip = if (hasName) "Rename photo" else "Name photo",
                    isSelected = hasName,
                    iconSize = iconSize,
                    onClick = onNamePhotoClick
                ),
                cookieButtonItem(
                    icon = if (hasLocation) Icons.Filled.Place else Icons.Outlined.Place,
                    contentDescription = if (hasLocation) "Edit location link" else "Link to a place",
                    tooltip = if (hasLocation) "Edit location link" else "Link to a place",
                    isSelected = hasLocation,
                    iconSize = iconSize,
                    onClick = onAddLinkClick
                ),
                cookieButtonItem(
                    icon = { mod, tint ->
                        if (hasRating) {
                            Box(
                                modifier = mod.size(iconSize),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_star_rating),
                                    contentDescription = "Rating ${photo.rating}",
                                    tint = tint,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Text(
                                    text = photo.rating.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(top = 0.5.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = KidStarOutlinedMaterialSymbol,
                                contentDescription = "Rate",
                                modifier = mod.size(iconSize),
                                tint = tint
                            )
                        }
                    },
                    tooltip = "Rate",
                    isSelected = hasRating,
                    onClick = onRate
                ),
                cookieButtonItem(
                    icon = if (hasSnippets) Icons.AutoMirrored.Filled.TextSnippet else Icons.AutoMirrored.Outlined.TextSnippet,
                    contentDescription = "Snippets",
                    tooltip = "Snippets",
                    enabled = hasSnippets,
                    isSelected = hasSnippets,
                    iconSize = iconSize,
                    onClick = onSnippets
                ),
                cookieButtonItem(
                    icon = { mod, tint ->
                        Icon(
                            imageVector = DeleteMaterialSymbol,
                            contentDescription = "Delete",
                            modifier = mod.size(iconSize),
                            tint = tint
                        )
                    },
                    tooltip = "Delete",
                    onClick = onDelete
                )
            )
        }
    )
}


