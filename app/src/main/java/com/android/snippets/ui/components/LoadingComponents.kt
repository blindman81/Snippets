package com.android.snippets.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.android.snippets.ui.components.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Global Busy Overlay used for bulk operations and imports.
 * Uses official Material 3 LoadingIndicator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusyOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(9999f)
            .background(Color.Black.copy(alpha = 0.32f)),
        contentAlignment = Alignment.Center
    ) {
        BlurredSurface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        }
    }
}
