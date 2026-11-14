package com.android.snippets.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Larger version of the official Material 3 Expressive loading indicator, without a container.
 * Indicator color defaults to [ColorScheme.primaryContainer].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    indicatorSize: Dp = 72.dp
) {
    androidx.compose.material3.LoadingIndicator(
        color = color,
        modifier = modifier.size(indicatorSize)
    )
}

/**
 * Global Busy Overlay used for bulk operations and imports.
 * Uses Haze backdrop blur for background blur behind the loading indicator.
 */
@Composable
fun BusyOverlay(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = LocalHazeState.current
) {
    val hazeStyle = remember {
        HazeStyle(
            tint = HazeTint(Color.Black.copy(alpha = 0.20f)),
            blurRadius = 24.dp,
            noiseFactor = HazeDefaults.noiseFactor
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(9999f)
            .background(Color.Black.copy(alpha = 0.16f))
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(state = hazeState, style = hazeStyle)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}
