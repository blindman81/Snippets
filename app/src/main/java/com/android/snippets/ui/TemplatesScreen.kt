package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.components.MainTopBar
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(viewModel: SnippetsViewModel) {
    androidx.activity.compose.BackHandler {
        viewModel.navigateLibrary()
    }
    val view = LocalView.current
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    var showActionDialogForSnippet by remember { mutableStateOf<String?>(null) }
    var showCustomSnippetDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            MainTopBar(
                title = "Add Snippets",
                onNavigationClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    viewModel.navigateLibrary()
                },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                isSpinning = true,
                isScrolled = isScrolled,
                leftAlignTitle = true
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Create Custom Snippet Button
                SettingsCardItem(
                    icon = Icons.Default.Add,
                    title = "Create custom snippet",
                    subtitle = "Write a new snippet and add to photo",
                    position = CardPosition.Single,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        showCustomSnippetDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CHOOSE A TEMPLATE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontFamily = com.android.snippets.ui.theme.GoogleSans
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                val uniqueSnippets = viewModel.allUniqueSnippets
                if (uniqueSnippets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No snippets created yet.\nCreate a custom one above!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = com.android.snippets.ui.theme.GoogleSans
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        uniqueSnippets.forEachIndexed { index, snippet ->
                            val position = when {
                                uniqueSnippets.size == 1 -> CardPosition.Single
                                index == 0 -> CardPosition.First
                                index == uniqueSnippets.size - 1 -> CardPosition.Last
                                else -> CardPosition.Middle
                            }
                            
                            val snippetStyle = viewModel.getSnippetStyle(snippet)
                            val snippetColorInt = viewModel.snippetColors[snippet] ?: android.graphics.Color.WHITE
                            val snippetColor = Color(snippetColorInt)
                            val photosCount = viewModel.photos.count { it.snippets.contains(snippet) }
                            
                            SettingsCardItem(
                                icon = Icons.Default.Tag,
                                title = snippet,
                                subtitle = "Used in $photosCount photo",
                                position = position,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    showActionDialogForSnippet = snippet
                                },
                                trailingContent = {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = snippetColor.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, snippetColor.copy(alpha = 0.3f)),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = snippet,
                                            style = getSnippetTextStyle(snippetStyle, MaterialTheme.typography.labelMedium),
                                            fontWeight = FontWeight.Bold,
                                            color = if (snippetColorInt == android.graphics.Color.WHITE) MaterialTheme.colorScheme.onSurface else snippetColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showActionDialogForSnippet != null) {
        val snippet = showActionDialogForSnippet!!
        AlertDialog(
            onDismissRequest = { showActionDialogForSnippet = null },
            title = { Text("Add snippet '$snippet'") },
            text = { Text("Where would you like to add this snippet?") },
            confirmButton = {
                TextButton(onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.pendingSnippetToApply = snippet
                    viewModel.pendingAddPhotoIntentToken = System.currentTimeMillis()
                    showActionDialogForSnippet = null
                    viewModel.navigateLibrary()
                }) {
                    Text("Import New Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.pendingSnippetToApply = snippet
                    viewModel.forceSelectionMode = true
                    showActionDialogForSnippet = null
                    viewModel.navigateLibrary()
                }) {
                    Text("Select From Library")
                }
            }
        )
    }

    if (showCustomSnippetDialog) {
        var customText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomSnippetDialog = false },
            title = { Text("Create Custom Snippet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { if (it.length <= 10) customText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Snippet Text (Max 10 chars)") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = customText.isNotBlank(),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.pendingSnippetToApply = customText.trim()
                        showCustomSnippetDialog = false
                        showActionDialogForSnippet = customText.trim()
                    }
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomSnippetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
