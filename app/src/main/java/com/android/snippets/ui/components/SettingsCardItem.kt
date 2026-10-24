package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.components.BlurredSurface
import com.android.snippets.ui.components.LocalHazeState

enum class CardPosition {
    Single, First, Middle, Last
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicCardContainer(
    modifier: Modifier = Modifier,
    position: CardPosition = CardPosition.Single,
    isSelected: Boolean = false,
    containerColor: Color? = null,
    useBlur: Boolean = false,
    alpha: Float = 0.55f,
    hazeState: dev.chrisbanes.haze.HazeState? = LocalHazeState.current,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topCornerTarget = if (isPressed || isSelected) 24.dp else {
        when (position) {
            CardPosition.Single, CardPosition.First -> 20.dp
            else -> 4.dp
        }
    }
    val bottomCornerTarget = if (isPressed || isSelected) 24.dp else {
        when (position) {
            CardPosition.Single, CardPosition.Last -> 20.dp
            else -> 4.dp
        }
    }

    val topCornerAnimated by animateDpAsState(
        targetValue = topCornerTarget,
        label = "dynamicCardTopCorner"
    )
    val bottomCornerAnimated by animateDpAsState(
        targetValue = bottomCornerTarget,
        label = "dynamicCardBottomCorner"
    )

    val shape = RoundedCornerShape(
        topStart = topCornerAnimated,
        topEnd = topCornerAnimated,
        bottomStart = bottomCornerAnimated,
        bottomEnd = bottomCornerAnimated
    )

    val view = LocalView.current
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        label = "scale"
    )

    val baseColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else (containerColor ?: MaterialTheme.colorScheme.surfaceContainerHighest)

    val containerModifier = modifier
        .fillMaxWidth()
        .padding(
            start = 12.dp,
            end = 12.dp,
            top = if (position == CardPosition.Middle || position == CardPosition.Last) 0.dp else 2.dp,
            bottom = if (position == CardPosition.First || position == CardPosition.Middle) 0.dp else 2.dp
        )
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        }
        .clip(shape)
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = onClick != null || onLongClick != null,
            onClick = {
                onClick?.let {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    it.invoke()
                }
            },
            onLongClick = {
                onLongClick?.let {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    it.invoke()
                }
            }
        )

    if (useBlur) {
        BlurredSurface(
            modifier = containerModifier,
            shape = shape,
            color = baseColor,
            alpha = alpha,
            hazeState = hazeState
        ) {
            content()
        }
    } else {
        Surface(
            modifier = containerModifier,
            shape = shape,
            color = baseColor,
            shadowElevation = if (isSelected) 6.dp else 1.dp,
            border = null
        ) {
            content()
        }
    }
}

@Composable
fun SettingsCardItem(
    icon: Any,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    isSelected: Boolean = false,
    trailingContent: @Composable () -> Unit = {},
    titleContent: @Composable (() -> Unit)? = null,
    position: CardPosition = CardPosition.Single,
    isExpressive: Boolean = false,
    containerColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val secondaryColor = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    DynamicCardContainer(
        onClick = onClick,
        position = position,
        isSelected = isSelected,
        containerColor = containerColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = if (isExpressive) 16.dp else 12.dp,
                    vertical = if (isExpressive) 20.dp else 16.dp
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CollectionIcon(
                icon = icon,
                modifier = Modifier.size(
                    if (isSelected) 44.dp
                    else if (isExpressive) 36.dp
                    else 32.dp
                ),
                tint = if (isSelected) contentColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                if (titleContent != null) {
                    titleContent()
                } else {
                    Text(
                        text = title,
                        style = if (isSelected) {
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide
                            )
                        } else if (isExpressive) {
                            MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                letterSpacing = 0.15.sp
                            )
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryColor
                    )
                }
            }
            Box(
                contentAlignment = Alignment.CenterEnd
            ) {
                trailingContent()
            }
        }
    }
}
