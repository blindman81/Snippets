package com.android.snippets.ui.components

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.android.snippets.ui.theme.GoogleSansFlexRound
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamePhotoModal(
    initialName: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val nameState = rememberTextFieldState(initialName ?: "")

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        moveUpOnKeyboardOverlap = true,
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            val trimmed = nameState.text.toString().trim()
            onSave(trimmed.takeIf { it.isNotBlank() })
        },
        title = {
            Text(
                text = "Name photo",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GoogleSansFlexRound,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                fontFamily = GoogleSansFlexRound
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    state = nameState,
                    inputTransformation = InputTransformation.maxLength(25),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    textStyle = LocalTextStyle.current.copy(fontFamily = GoogleSansFlexRound),
                    label = { Text("Photo name", fontFamily = GoogleSansFlexRound) },
                    placeholder = { Text("Photo name", fontFamily = GoogleSansFlexRound) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    onKeyboardAction = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        val trimmed = nameState.text.toString().trim()
                        onSave(trimmed.takeIf { it.isNotBlank() })
                    },
                    leadingIcon = {
                        IconButton(onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            coroutineScope.launch {
                                val clipEntry = clipboard.getClipEntry()
                                val textToPaste = clipEntry?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                                    ?: run {
                                        val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        sysClipboard?.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                                    }
                                if (!textToPaste.isNullOrBlank()) {
                                    nameState.setTextAndPlaceCursorAtEnd(
                                        textToPaste.trim().replace("\n", " ").replace("\r", "").take(25)
                                    )
                                }
                            }
                        }) {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste")
                        }
                    },
                    trailingIcon = if (nameState.text.isNotEmpty()) {
                        {
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    nameState.clearText()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = "${nameState.text.length}/25",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = GoogleSansFlexRound),
                    fontFamily = GoogleSansFlexRound,
                    color = if (nameState.text.length >= 25) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = if (nameState.text.isNotEmpty()) 36.dp else 12.dp)
                )
            }
        }
    )
}
