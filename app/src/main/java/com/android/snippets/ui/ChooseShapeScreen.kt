package com.android.snippets.ui

import com.android.snippets.ui.components.*
import com.android.snippets.ui.shapes.AppShape
import com.android.snippets.viewmodel.SnippetsViewModel

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseShapeScreen(viewModel: SnippetsViewModel) {
    androidx.activity.compose.BackHandler {
        viewModel.currentScreen = viewModel.previousScreen
    }
    val view = LocalView.current
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }
    var animationMode by remember { mutableStateOf("memory") } // "memory" or "icon button"

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

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            MainTopBar(
                title = "Choose Shape",
                onNavigationClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    viewModel.currentScreen = viewModel.previousScreen
                },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                isSpinning = true,
                isScrolled = isScrolled,
                leftAlignTitle = true,
                actions = {
                    var showDropdown by remember { mutableStateOf(false) }
                    Box {
                        AnimatedCookieButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                showDropdown = true
                            },
                            icon = Icons.Default.FilterList,
                            contentDescription = "Animation Style",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            size = 40.dp
                        )
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    if (animationMode == "memory") {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                    } else {
                                        Spacer(modifier = Modifier.size(18.dp))
                                    }
                                },
                                text = { Text("Memory style") },
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    animationMode = "memory"
                                    showDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    if (animationMode == "icon button") {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                    } else {
                                        Spacer(modifier = Modifier.size(18.dp))
                                    }
                                },
                                text = { Text("Icon Button style") },
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    animationMode = "icon button"
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
            color = MaterialTheme.colorScheme.surface
        ) {
            CompositionLocalProvider(LocalShapeAnimationMode provides animationMode) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val shapes = AppShape.values().toList()
                    shapes.forEachIndexed { index, shape ->
                        val isSelected = viewModel.selectedShape == shape
                        val position = when (index) {
                            0 -> if (shapes.size == 1) CardPosition.Single else CardPosition.First
                            shapes.size - 1 -> CardPosition.Last
                            else -> CardPosition.Middle
                        }

                        SettingsCardItem(
                            icon = shape,
                            title = shape.displayName,
                            isSelected = isSelected,
                            position = position,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.updateSelectedShape(shape)
                            },
                            trailingContent = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
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
