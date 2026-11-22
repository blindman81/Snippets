package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.viewmodel.SnippetsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSnippetsModal(
    photo: Photo,
    onAdd: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: SnippetsViewModel
) {
    val view = LocalView.current
    val textState = rememberTextFieldState()

    val currentText = textState.text.toString()
    val filteredSuggestions = remember(currentText, viewModel.allUniqueSnippets, photo.snippets) {
        val existing = photo.snippets.toSet()
        if (currentText.isBlank()) {
            viewModel.allUniqueSnippets.filter { it !in existing }
        } else {
            viewModel.allUniqueSnippets.filter {
                it !in existing && it.contains(currentText, ignoreCase = true) && !it.equals(currentText, ignoreCase = true)
            }
        }
    }

    BlurredAlertDialog(
        onDismissRequest = onClose,
        moveUpOnKeyboardOverlap = true,
        onConfirm = {
            val trimmed = textState.text.toString().trim()
            if (trimmed.isNotBlank()) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onAdd(trimmed)
                onClose()
            }
        },
        title = {
            Text(
                text = "Add a Snippet",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GoogleSansFlexRound,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                fontFamily = GoogleSansFlexRound
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        state = textState,
                        inputTransformation = InputTransformation.maxLength(15),
                        placeholder = { Text("Enter snippet") },
                        lineLimits = TextFieldLineLimits.SingleLine,
                        trailingIcon = if (textState.text.isNotEmpty()) {
                            {
                                IconButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        textState.clearText()
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
                        onKeyboardAction = {
                            val trimmed = textState.text.toString().trim()
                            if (trimmed.isNotBlank()) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onAdd(trimmed)
                                onClose()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "${textState.text.length}/15",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (textState.text.length >= 15) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
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
                                    textState.setTextAndPlaceCursorAtEnd(suggestion)
                                },
                                label = {
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = com.android.snippets.ui.theme.GoogleSansFlexRound,
                                            fontWeight = FontWeight.SemiBold
                                        ),
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
