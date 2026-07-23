package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.SnippetStyle
import com.android.snippets.ui.getSnippetTextStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BulkEditSnippetsDialog(
    viewModel: SnippetsViewModel,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    var step by remember { mutableStateOf(0) } // 0: Text Entry & Templates, 1: Style & Color
    var text by remember { mutableStateOf("") }
    
    val snippetColorsPalette = remember {
        listOf(
            0xFFEF5350.toInt(), // Red
            0xFFEC407A.toInt(), // Pink
            0xFFAB47BC.toInt(), // Purple
            0xFF42A5F5.toInt(), // Blue
            0xFF26A69A.toInt(), // Teal
            0xFF66BB6A.toInt(), // Green
            0xFFFFEE58.toInt(), // Yellow
            0xFFFFA726.toInt(), // Orange
            0xFF8D6E63.toInt(), // Brown
            0xFF78909C.toInt(), // Slate
            0xFFD4E157.toInt()  // Lime
        )
    }

    var selectedColor by remember { mutableStateOf<Int?>(null) } // null means random, otherwise selected color
    var selectedStyle by remember { mutableStateOf(SnippetStyle.Default) }

    val photoCount = viewModel.selectedPhotoIds.size

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bulk Edit Snippets",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Modifying $photoCount selected photo${if (photoCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (step == 0) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { if (it.length <= 15) text = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        label = { Text("Enter Snippet", style = MaterialTheme.typography.labelMedium) },
                        placeholder = { Text("Type tag here...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        supportingText = {
                            Text(
                                text = "${text.length}/15",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (text.length >= 15) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    val suggestions = remember(text, viewModel.allUniqueSnippets) {
                        if (text.isEmpty()) {
                            viewModel.allUniqueSnippets.take(9)
                        } else {
                            viewModel.allUniqueSnippets.filter {
                                it.contains(text, ignoreCase = true) && !it.equals(text, ignoreCase = true)
                            }.take(9)
                        }
                    }

                    if (suggestions.isNotEmpty()) {
                        Text(
                            text = "TAP A TEMPLATE TO FILL:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            suggestions.chunked(3).take(3).forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowItems.forEach { suggestion ->
                                        val snippetColorInt = viewModel.snippetColors[suggestion] ?: android.graphics.Color.WHITE
                                        val snippetColor = Color(snippetColorInt)
                                        val snippetStyle = viewModel.getSnippetStyle(suggestion)

                                        SuggestionChip(
                                            onClick = {
                                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                                text = suggestion
                                                selectedColor = snippetColorInt
                                                selectedStyle = snippetStyle
                                            },
                                            label = {
                                                Text(
                                                    text = suggestion,
                                                    style = getSnippetTextStyle(snippetStyle, MaterialTheme.typography.labelMedium),
                                                    maxLines = 1,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            shape = CircleShape,
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (snippetColorInt == android.graphics.Color.WHITE) MaterialTheme.colorScheme.surfaceContainerHigh else snippetColor.copy(alpha = 0.15f),
                                                labelColor = if (snippetColorInt == android.graphics.Color.WHITE) MaterialTheme.colorScheme.onSurfaceVariant else snippetColor
                                            ),
                                            border = BorderStroke(1.dp, if (snippetColorInt == android.graphics.Color.WHITE) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else snippetColor.copy(alpha = 0.3f))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                step = 1
                            },
                            enabled = text.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Next (Choose Style & Add)")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                    viewModel.bulkRemoveSnippet(viewModel.selectedPhotoIds, text)
                                    onDismiss()
                                },
                                enabled = text.isNotBlank(),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            ) {
                                Text("Remove from All", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                    viewModel.bulkClearSnippets(viewModel.selectedPhotoIds)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Clear All", maxLines = 1)
                            }
                        }
                    }

                } else {
                    val displayColor = if (selectedColor == null || selectedColor == -1) Color(snippetColorsPalette.random()) else Color(selectedColor!!)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerLowest,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = displayColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, displayColor.copy(alpha = 0.3f)),
                        ) {
                            Text(
                                text = text,
                                style = getSnippetTextStyle(selectedStyle, MaterialTheme.typography.titleLarge),
                                fontWeight = FontWeight.Bold,
                                color = displayColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = 0,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        Tab(
                            selected = true,
                            onClick = {},
                            text = { Text("Style & Color Selection", fontWeight = FontWeight.Bold) }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "COLOR",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            userScrollEnabled = true
                        ) {
                            item {
                                val isRandomSelected = selectedColor == -1 || selectedColor == null
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                            selectedColor = -1
                                        }
                                ) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = CircleShape,
                                        color = if (isRandomSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                                        border = if (isRandomSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Shuffle,
                                                contentDescription = "Random Color",
                                                tint = if (isRandomSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            items(snippetColorsPalette) { colorInt ->
                                val isSelected = selectedColor == colorInt
                                val color = Color(colorInt)
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                            selectedColor = colorInt
                                        }
                                ) {
                                    Surface(
                                        modifier = Modifier.size(if (isSelected) 48.dp else 42.dp),
                                        shape = CircleShape,
                                        color = color,
                                        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null
                                    ) {}
                                }
                            }
                        }

                        Text(
                            text = "FONT STYLE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SnippetStyle.values().forEach { style ->
                                val isSelected = selectedStyle == style
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        selectedStyle = style
                                    },
                                    label = { Text(style.name) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                step = 0
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Back")
                        }

                        Button(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                val finalColor = if (selectedColor == null || selectedColor == -1) snippetColorsPalette.random() else selectedColor!!
                                viewModel.bulkAddSnippet(viewModel.selectedPhotoIds, text, finalColor, selectedStyle)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Add to All")
                        }
                    }
                }
            }
        }
    }
}
