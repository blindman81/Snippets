package com.android.snippets.ui
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TestButtonGroup() {
    ButtonGroup(
        modifier = Modifier,
        overflowIndicator = {}
    ) {}
}
