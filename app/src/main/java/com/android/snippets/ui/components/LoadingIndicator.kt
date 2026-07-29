package com.android.snippets.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A wrapper around the official Material 3 LoadingIndicator to handle experimental API opt-ins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    androidx.compose.material3.LoadingIndicator(
        modifier = modifier,
        color = color
    )
}
