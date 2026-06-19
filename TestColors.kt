package com.android.snippets.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TestToggleable() {
    ButtonGroup {
        toggleableItem(
            checked = true,
            onCheckedChange = {},
            label = "Test",
            colors = ToggleButtonDefaults.toggleButtonColors()
        )
    }
}
