package com.android.snippets.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TestToggleableItemColors() {
    ButtonGroup {
        toggleableItem(
            checked = true,
            onCheckedChange = {},
            label = "Test",
            colors = ToggleButtonDefaults.toggleButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedContainerColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
