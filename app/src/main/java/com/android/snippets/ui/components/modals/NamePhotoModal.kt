package com.android.snippets.ui.components

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
    var nameValue by remember { mutableStateOf(initialName ?: "") }

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        moveUpOnKeyboardOverlap = true,
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onSave(nameValue.trim().takeIf { it.isNotBlank() })
        },
        title = {
            Text(
                text = "Name photo",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nameValue,
                    onValueChange = {
                        if (it.length <= 25) {
                            nameValue = it
                        }
                    },
                    placeholder = { Text("Photo name") },
                    singleLine = true,
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
                                    nameValue = textToPaste.trim().replace("\n", " ").replace("\r", "").take(25)
                                }
                            }
                        }) {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste")
                        }
                    },
                    trailingIcon = if (nameValue.isNotEmpty()) {
                        {
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    nameValue = ""
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
                    text = "${nameValue.length}/25",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (nameValue.length >= 25) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = if (nameValue.isNotEmpty()) 36.dp else 12.dp)
                )
            }
        }
    )
}
