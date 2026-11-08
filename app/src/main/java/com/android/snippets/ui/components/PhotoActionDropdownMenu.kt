package com.android.snippets.ui.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.android.snippets.ui.shapes.CookieShape
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun PhotoActionDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<CookieButtonItem>,
    menuColumns: Int = 2,
    buttonSize: Dp = 56.dp,
    shape: Shape = CookieShape,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }

    PredictiveBackHandler(enabled = expanded) { progress ->
        try {
            progress.collect { backEvent ->
                predictiveBackProgress = backEvent.progress
            }
            predictiveBackProgress = 0f
            onDismissRequest()
        } catch (e: CancellationException) {
            predictiveBackProgress = 0f
        }
    }

    val menuWidth = (buttonSize * menuColumns) + (1.dp * (menuColumns - 1)) + 16.dp

    val scale = 1f - (predictiveBackProgress * 0.12f)
    val alpha = 1f - (predictiveBackProgress * 0.25f)

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = androidx.compose.ui.unit.DpOffset(x = buttonSize - menuWidth, y = 8.dp),
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = false,
            dismissOnClickOutside = true
        ),
        containerColor = Color.Transparent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = null,
        modifier = modifier
            .background(Color.Transparent)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                transformOrigin = TransformOrigin(1f, 0f)
            }
    ) {
        BlurredSurface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 3.dp,
            shadowElevation = 3.dp
        ) {
            val menuItems = items.map { item ->
                item.copy(
                    onClick = item.onClick?.let { origClick ->
                        {
                            onDismissRequest()
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
