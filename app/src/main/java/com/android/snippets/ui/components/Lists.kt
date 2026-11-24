package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    icon: Any, // ImageVector or Int (@DrawableRes)
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    iconContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }

    // Segmented list corner shapes: 16dp rounded corners for lists
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
    val itemShapes = ListItemDefaults.shapes(
        shape = baseShape,
        pressedShape = RoundedCornerShape(16.dp)
    )

    // Adjusted padding for 9-sided cookie container: 12dp start padding keeps container balanced with text
    val contentPadding = if (subtext.isNullOrBlank()) {
        PaddingValues(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
    } else {
        PaddingValues(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    }

    SegmentedListItem(
        selected = false,
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onClick()
        },
        shapes = itemShapes,
        interactionSource = interactionSource,
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (subtext.isNullOrBlank()) 64.dp else 72.dp),
        leadingContent = {
            Surface(
                shape = CookieShape,
                color = iconContainerColor,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (icon) {
                        is ImageVector -> Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        is Int -> Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        content = {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GoogleSansFlexRound
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtext.isNullOrBlank()) {
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = GoogleSansFlexRound
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = ListItemDefaults.segmentedColors(
            containerColor = containerColor
        )
    )
}
