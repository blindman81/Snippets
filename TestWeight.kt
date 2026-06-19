package com.android.snippets.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TestWeight() {
    ButtonGroup {
        customItem(
            buttonGroupContent = {
                Button(modifier = Modifier.weight(1f), onClick = {}) {}
            },
            menuContent = {}
        )
    }
}
