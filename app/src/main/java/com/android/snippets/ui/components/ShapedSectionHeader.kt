package com.android.snippets.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.shapes.LocalAppShape

@Composable
fun ShapedSectionHeader(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    usePrimary: Boolean = false
) {
    val containerColor = if (usePrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val iconColor = if (usePrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(LocalAppShape.current)
            .background(color = containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
