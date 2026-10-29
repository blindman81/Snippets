package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.snippets.model.Photo
import com.android.snippets.ui.theme.rememberSnippetBackgroundColor
import com.android.snippets.ui.theme.rememberSnippetTextColor
import com.android.snippets.ui.theme.getSnippetTextStyle
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun SnippetsModal(
    photo: Photo,
    onRemove: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: SnippetsViewModel
) {
    var localSnippets by remember(photo.snippets) { mutableStateOf(photo.snippets) }
    val view = LocalView.current

    BlurredAlertDialog(
        onDismissRequest = onClose,
        onConfirm = onClose,
        title = {
            Text(
                "Snippets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            if (localSnippets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No snippets yet",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    localSnippets.forEach { snippet ->
                        val forcedStyle = viewModel.getSnippetStyle(snippet)
                        val snippetColor = rememberSnippetBackgroundColor()
                        val bgColor = snippetColor
                        val textColor = rememberSnippetTextColor()

                        Surface(shape = CircleShape, color = bgColor, border = null) {
                            Row(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = snippet,
                                    style = getSnippetTextStyle(
                                        forcedStyle,
                                        MaterialTheme.typography.titleMedium,
                                        isCloud = true
                                    ),
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                    onRemove(snippet)
                                    localSnippets = localSnippets - snippet
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Rounded.Close, null, tint = textColor, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
