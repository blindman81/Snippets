package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.theme.GoogleSansFlexRound

/**
 * Material 3 Expressive Segmented List Item.
 * Features a 9-sided cookie surface container for leading icons and smoothly morphs all corners to 16dp when tapped or held.
 * Default containerColor is surface.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveSegmentedListItem(
    index: Int,
    count: Int,
    label: String,
    icon: Any? = null, // ImageVector, Int (@DrawableRes), String/Number, or null for text-only item
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    iconContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    selected: Boolean = false,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    consumeIconClick: Boolean = false,
    onIconClick: (() -> Unit)? = null
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }

    // Segmented list corner shapes: 16dp rounded corners for lists with stable state across recompositions
    val itemShapes = remember(index, count) {
        val baseTopStart = if (index == 0 || count == 1) 16.dp else 4.dp
        val baseTopEnd = if (index == 0 || count == 1) 16.dp else 4.dp
        val baseBottomStart = if (index == count - 1 || count == 1) 16.dp else 4.dp
        val baseBottomEnd = if (index == count - 1 || count == 1) 16.dp else 4.dp

        val baseShape = RoundedCornerShape(
            topStart = baseTopStart,
            topEnd = baseTopEnd,
            bottomEnd = baseBottomEnd,
            bottomStart = baseBottomStart
        )
        val pressedShape = RoundedCornerShape(16.dp)

        ListItemShapes(
            shape = baseShape,
            selectedShape = pressedShape,
            pressedShape = pressedShape,
            focusedShape = pressedShape,
            hoveredShape = pressedShape,
            draggedShape = pressedShape
        )
    }

    val hasLeading = leadingContent != null || icon != null
    // Adjusted padding: 12dp start padding when icon/leading is present, 16dp when null
    val startPadding = if (hasLeading) 12.dp else 16.dp
    val contentPadding = if (subtext.isNullOrBlank()) {
        PaddingValues(start = startPadding, end = 16.dp, top = 10.dp, bottom = 10.dp)
    } else {
        PaddingValues(start = startPadding, end = 16.dp, top = 12.dp, bottom = 12.dp)
    }

    val animatedIconContainerColor by animateColorAsState(
        targetValue = iconContainerColor,
        label = "segmented_list_icon_container_color"
    )
    val animatedIconColor by animateColorAsState(
        targetValue = iconColor,
        label = "segmented_list_icon_color"
    )

    val leadingContentComposable: (@Composable () -> Unit)? = leadingContent ?: if (icon != null) {
        {
            val iconClickModifier = if (consumeIconClick || onIconClick != null) {
                Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onIconClick?.invoke() }
                )
            } else {
                Modifier
            }

            Surface(
                shape = CookieShape,
                color = animatedIconContainerColor,
                modifier = Modifier
                    .size(44.dp)
                    .then(iconClickModifier)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (icon) {
                        is ImageVector -> Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = animatedIconColor
                        )
                        is Int -> Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = animatedIconColor
                        )
                        is String -> Text(
                            text = icon,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = GoogleSansFlexRound,
                                fontSize = if (icon.length > 3) 12.sp else 15.sp
                            ),
                            color = animatedIconColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        is Number -> Text(
                            text = icon.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = GoogleSansFlexRound,
                                fontSize = if (icon.toString().length > 3) 12.sp else 15.sp
                            ),
                            color = animatedIconColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    } else null

    val subtextColor by animateColorAsState(
        targetValue = if (selected) selectedContentColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "segmented_list_subtext_color"
    )

    val textContentComposable: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GoogleSansFlexRound
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtext.isNullOrBlank()) {
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = GoogleSansFlexRound
                    ),
                    color = subtextColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    SegmentedListItem(
        selected = selected,
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onClick?.invoke()
        },
        shapes = itemShapes,
        interactionSource = interactionSource,
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (subtext.isNullOrBlank()) 64.dp else 72.dp),
        leadingContent = leadingContentComposable,
        trailingContent = trailingContent,
        content = textContentComposable,
        colors = ListItemDefaults.segmentedColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            supportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = selectedContainerColor,
            selectedContentColor = selectedContentColor,
            selectedSupportingContentColor = selectedContentColor
        )
    )
}
