package com.android.snippets.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TestToggleableItemColorsFull() {
    ButtonGroup {
        toggleableItem(
            weight = 1f,
            checked = true,
            onCheckedChange = {},
            label = { Text("Label") },
            icon = { Icon(Icons.Default.Add, null) },
            colors = ToggleButtonDefaults.toggleButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                checkedContainerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                checkedContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}
