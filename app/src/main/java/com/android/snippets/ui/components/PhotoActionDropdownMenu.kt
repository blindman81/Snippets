package com.android.snippets.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.shapes.CookieShape

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

    val menuWidth = (buttonSize * menuColumns) + (1.dp * (menuColumns - 1)) + 16.dp

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = androidx.compose.ui.unit.DpOffset(x = buttonSize - menuWidth, y = 8.dp),
        containerColor = Color.Transparent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = null,
        modifier = modifier.background(Color.Transparent)
    ) {
        BlurredSurface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
