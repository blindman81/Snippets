package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TextSnippet
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.model.Photo
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun AddSnippetsModal(
    photo: Photo,
    onAdd: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: SnippetsViewModel
) {
    val view = LocalView.current
    var text by remember { mutableStateOf("") }

    val filteredSuggestions = remember(text, viewModel.allUniqueSnippets, photo.snippets) {
        val existing = photo.snippets.toSet()
        if (text.isBlank()) {
            viewModel.allUniqueSnippets.filter { it !in existing }
        } else {
            viewModel.allUniqueSnippets.filter {
                it !in existing && it.contains(text, ignoreCase = true) && !it.equals(text, ignoreCase = true)
            }
        }
    }

    BlurredAlertDialog(
        onDismissRequest = onClose,
        moveUpOnKeyboardOverlap = true,
        onConfirm = {
            if (text.isNotBlank()) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onAdd(text.trim())
                onClose()
            }
        },
        title = {
            Text(
                text = "Add a Snippet",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            if (it.length <= 15) {
                                text = it
                            }
                        },
                        placeholder = { Text("Enter snippet") },
                        singleLine = true,
                        trailingIcon = if (text.isNotEmpty()) {
                            {
                                IconButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        text = ""
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (text.isNotBlank()) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onAdd(text.trim())
                                onClose()
                            }
                        }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "${text.length}/15",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (text.length >= 15) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 12.dp)
                    )
                }

                if (filteredSuggestions.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredSuggestions) { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    text = suggestion
                                },
                                label = {
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.TextSnippet,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = null
                            )
                        }
                    }
                }
            }
        }
    )
}
