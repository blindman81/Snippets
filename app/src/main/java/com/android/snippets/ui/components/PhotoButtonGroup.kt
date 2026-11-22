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
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.vectorResource
import com.android.snippets.model.Photo
import com.ln.android.snippets.R
import com.android.snippets.ui.shapes.CirclePolygon
import com.android.snippets.ui.shapes.Cookie9ToCircleMorph
import com.android.snippets.ui.shapes.CookiePolygon
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.shapes.Cookie9Shape
import com.android.snippets.ui.shapes.MorphShape
import com.android.snippets.ui.shapes.RoundedPolygonShape
import com.android.snippets.ui.util.Motion
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * PhotoButtonGroup component consisting of two connected cookie-shaped buttons:
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
fun PhotoButtonGroup(
    primaryIcon: ImageVector,
    onMenuToggle: (() -> Unit)? = null,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    trailingFilledIcon: ImageVector = ImageVector.vectorResource(R.drawable.ic_more_horiz_filled),
    trailingOutlinedIcon: ImageVector = ImageVector.vectorResource(R.drawable.ic_more_horiz_outlined),
    items: List<CookieButtonItem> = emptyList(),
    primaryText: String? = null,
    contentDescription: String? = primaryText,
    isPrimaryActive: Boolean = false,
    menuColumns: Int = 2,
    buttonSize: Dp = 56.dp,
    gap: Dp = 1.dp,
    overlap: Dp = 4.dp,
    shape: Shape = CookieShape,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    isSpinning: Boolean = true,
    enabled: Boolean = true,
    isOutlined: Boolean = true,
    isOnBlurredSurface: Boolean = LocalIsOnBlurredSurface.current
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    val actualTrailingIcon = trailingIcon ?: if (expanded) trailingFilledIcon else trailingOutlinedIcon
    val hasOutline = isOutlined || isOnBlurredSurface

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
    var isLeadingPressed by remember { mutableStateOf(false) }
    var isLeadingHeld by remember { mutableStateOf(false) }

    // Trailing button animatables
    val rotationTrailing = remember { Animatable(0f) }
    val morphProgressTrailing = remember { Animatable(0f) }
    val pressScaleTrailing = remember { Animatable(1f) }
    val wiggleTrailing = remember { Animatable(0f) }
    var wiggleTrailingJob by remember { mutableStateOf<Job?>(null) }
    var spinTrailingJob by remember { mutableStateOf<Job?>(null) }
    var tappedTrailing by remember { mutableStateOf(false) }
    var isTrailingPressed by remember { mutableStateOf(false) }
    var isTrailingHeld by remember { mutableStateOf(false) }

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
    val isLeadingPrimary = isPrimaryActive || tappedLeading || isLeadingPressed || isLeadingHeld
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

    val isTrailingPrimary = expanded || tappedTrailing || isTrailingPressed || isTrailingHeld
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
                        var holdTriggered = false
                        detectTapGestures(
                            onPress = {
                                holdTriggered = false
                                isLeadingPressed = true
                                val holdJob = scope.launch {
                                    kotlinx.coroutines.delay(120L)
                                    holdTriggered = true
                                    isLeadingHeld = true
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    launch {
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
                                    isLeadingPressed = false
                                    isLeadingHeld = false
                                    if (holdTriggered) {
                                        scope.launch {
                                            launch { morphProgressLeading.animateTo(if (isPrimaryActive) 1f else 0f, Motion.BouncySpring) }
                                            launch { morphProgressTrailing.animateTo(if (expanded) 1f else 0f, Motion.BouncySpring) }
                                        }
                                    }
                                }
                            },
                            onLongPress = {
                                // Absorbs long-press gesture so releasing after hold does not fire tap
                            },
                            onTap = {
                                if (holdTriggered) return@detectTapGestures
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                tappedLeading = true

                                // Perform action simultaneously
                                onPrimaryClick()

                                // Wiggle adjacent button (trailing button) - only morph when held
                                triggerWiggleTrailing(1f)

                                spinLeadingJob?.cancel()
                                spinLeadingJob = scope.launch {
                                    try {
                                        val scaleJob = launch {
                                            try {
                                                pressScaleLeading.animateTo(0.92f, Motion.PressSpring)
                                                pressScaleLeading.animateTo(
                                                    1f,
                                                    androidx.compose.animation.core.spring(
                                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                                        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                                                    )
                                                )
                                            } finally {
                                                withContext(NonCancellable) {
                                                    if (pressScaleLeading.value != 1f) {
                                                        pressScaleLeading.animateTo(1f, Motion.BouncySpring)
                                                    }
                                                }
                                            }
                                        }
                                        scaleJob.join()
                                    } finally {
                                        withContext(NonCancellable) {
                                            tappedLeading = false
                                            if (pressScaleLeading.value != 1f) {
                                                pressScaleLeading.animateTo(1f, Motion.BouncySpring)
                                            }
                                        }
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
                    if (hasOutline) {
                        // Outline variant (bottom layer)
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(shapeLeading)
                                .background(if (enabled) unselectedContainerColor else unselectedContainerColor.copy(alpha = 0.38f))
                                .border(
                                    border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
                                    shape = shapeLeading
                                )
                        )
                        // Primary layer (drawn OVER outline variant)
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
                    .zIndex(if (morphProgressTrailing.value > 0.001f || pressScaleTrailing.value < 0.99f || isTrailingPrimary) 2f else 0f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = pressScaleTrailing.value
                            scaleY = pressScaleTrailing.value
                        }
                        .semantics { role = Role.Button }
                        .pointerInput(enabled) {
                            if (!enabled) return@pointerInput
                            var holdTriggered = false
                            detectTapGestures(
                                onPress = {
                                    holdTriggered = false
                                    isTrailingPressed = true
                                    val holdJob = scope.launch {
                                        kotlinx.coroutines.delay(120L)
                                        holdTriggered = true
                                        isTrailingHeld = true
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        launch {
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
                                        isTrailingPressed = false
                                        isTrailingHeld = false
                                        if (holdTriggered) {
                                            scope.launch {
                                                launch { morphProgressTrailing.animateTo(if (expanded) 1f else 0f, Motion.BouncySpring) }
                                                launch { morphProgressLeading.animateTo(if (isPrimaryActive) 1f else 0f, Motion.BouncySpring) }
                                            }
                                        }
                                    }
                                },
                                onLongPress = {
                                    // Absorbs long-press gesture so releasing after hold does not fire tap
                                },
                                onTap = {
                                    if (holdTriggered) return@detectTapGestures
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    tappedTrailing = true

                                    // Perform menu toggle simultaneously
                                    expanded = !expanded
                                    if (expanded) onMenuToggle?.invoke()

                                    // Wiggle adjacent button (leading button) - only morph when held
                                    triggerWiggleLeading(-1f)

                                    spinTrailingJob?.cancel()
                                    spinTrailingJob = scope.launch {
                                        try {
                                            val scaleJob = launch {
                                                try {
                                                    pressScaleTrailing.animateTo(0.92f, Motion.PressSpring)
                                                    pressScaleTrailing.animateTo(
                                                        1f,
                                                        androidx.compose.animation.core.spring(
                                                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                                            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                                                        )
                                                    )
                                                } finally {
                                                    withContext(NonCancellable) {
                                                        if (pressScaleTrailing.value != 1f) {
                                                            pressScaleTrailing.animateTo(1f, Motion.BouncySpring)
                                                        }
                                                    }
                                                }
                                            }
                                            scaleJob.join()
                                        } finally {
                                            withContext(NonCancellable) {
                                                tappedTrailing = false
                                                if (pressScaleTrailing.value != 1f) {
                                                    pressScaleTrailing.animateTo(1f, Motion.BouncySpring)
                                                }
                                            }
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
                                rotationZ = rotationTrailing.value + wiggleTrailing.value + tiltTrailing
                            }
                    ) {
                        if (hasOutline) {
                            // Outline variant (bottom layer)
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(shapeTrailing)
                                    .background(if (enabled) unselectedContainerColor else unselectedContainerColor.copy(alpha = 0.38f))
                                    .border(
                                        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
                                        shape = shapeTrailing
                                    )
                            )
                            // Primary layer (drawn OVER outline variant)
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

                // Button Group Menu (Dropdown) - anchored to Trailing (More) Button, aligned and moved to the left
                PhotoActionDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    items = items,
                    menuColumns = menuColumns,
                    buttonSize = buttonSize,
                    shape = shape,
                    selectedContainerColor = selectedContainerColor,
                    selectedContentColor = selectedContentColor,
                    unselectedContainerColor = unselectedContainerColor,
                    unselectedContentColor = unselectedContentColor
                )
            }
        }
    }
}

/**
 * Photo-specific overload of [PhotoButtonGroup].
 * Displays the primary Add Snippet action and the 8 photo actions (2 per row in 4 rows).
 */
@Composable
fun PhotoButtonGroup(
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
    buttonSize: Dp = 56.dp,
    isOutlined: Boolean = true,
    isOnBlurredSurface: Boolean = LocalIsOnBlurredSurface.current
) {
    val view = LocalView.current
    val hasSnippets = photo.snippets.isNotEmpty()
    val isFavorite = photo.isFavorite
    val hasName = !photo.name.isNullOrBlank()
    val hasLocation = !photo.locationLink.isNullOrBlank() || !photo.locationName.isNullOrBlank()
    val hasRating = photo.rating > 0

    val downloadIcon = ImageVector.vectorResource(R.drawable.ic_download_symbol)
    val shareIcon = Icons.Rounded.Share
    val starOutlinedIcon = ImageVector.vectorResource(R.drawable.ic_kid_star_outlined)
    val deleteIcon = Icons.Rounded.Delete
    val iconSize = (buttonSize * 0.44f).coerceAtLeast(24.dp)

    val items = remember(
        hasSnippets,
        isFavorite,
        hasName,
        hasLocation,
        hasRating,
        photo.rating,
        photo.locationName,
        iconSize,
        downloadIcon,
        shareIcon,
        starOutlinedIcon,
        deleteIcon,
        onDownload,
        onShare,
        onToggleFavorite,
        onNamePhotoClick,
        onAddLinkClick,
        onRate,
        onSnippets,
        onDelete
    ) {
        listOf(
            cookieButtonItem(
                icon = downloadIcon,
                contentDescription = "Download",
                tooltip = "Download",
                iconSize = iconSize,
                enabled = hasSnippets,
                onClick = onDownload
            ),
            cookieButtonItem(
                icon = shareIcon,
                contentDescription = "Share",
                tooltip = "Share",
                iconSize = iconSize,
                enabled = hasSnippets,
                onClick = onShare
            ),
            cookieButtonItem(
                icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                tooltip = if (isFavorite) "Unfavorite" else "Favorite",
                isSelected = isFavorite,
                iconSize = iconSize,
                onClick = onToggleFavorite
            ),
            cookieButtonItem(
                icon = if (hasName) Icons.AutoMirrored.Rounded.Label else Icons.AutoMirrored.Outlined.Label,
                contentDescription = if (hasName) "Rename photo" else "Name photo",
                tooltip = if (hasName) "Rename photo" else "Name photo",
                isSelected = hasName,
                iconSize = iconSize,
                onClick = onNamePhotoClick
            ),
            cookieButtonItem(
                icon = if (hasLocation) Icons.Rounded.Place else Icons.Outlined.Place,
                contentDescription = if (hasLocation) "Edit location" else "Link to a place",
                tooltip = if (hasLocation) (photo.locationName?.takeIf { it.isNotBlank() } ?: "Edit location") else "Link to a place",
                isSelected = hasLocation,
                iconSize = iconSize,
                onClick = onAddLinkClick
            ),
            cookieButtonItem(
                icon = { mod: Modifier, tint: Color ->
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
                            imageVector = starOutlinedIcon,
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
                icon = if (hasSnippets) Icons.AutoMirrored.Rounded.TextSnippet else Icons.AutoMirrored.Outlined.TextSnippet,
                contentDescription = "Snippets",
                tooltip = "Snippets",
                enabled = hasSnippets,
                isSelected = hasSnippets,
                iconSize = iconSize,
                onClick = onSnippets
            ),
            cookieButtonItem(
                icon = deleteIcon,
                contentDescription = "Delete",
                tooltip = "Delete",
                iconSize = iconSize,
                onClick = onDelete
            )
        )
    }

    PhotoButtonGroup(
        primaryIcon = Icons.Rounded.Add,
        contentDescription = "Add snippets",
        onPrimaryClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onAdd()
        },
        isPrimaryActive = isPrimaryActive,
        menuColumns = 2,
        buttonSize = buttonSize,
        modifier = modifier,
        items = items,
        isOutlined = isOutlined,
        isOnBlurredSurface = isOnBlurredSurface
    )
}


