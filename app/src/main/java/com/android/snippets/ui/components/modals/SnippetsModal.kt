package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.snippets.model.Photo
import com.android.snippets.ui.theme.GoogleSansFlexRound
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
                text = "Snippets",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GoogleSansFlexRound,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                fontFamily = GoogleSansFlexRound
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
                FlexBox(
                    modifier = Modifier.fillMaxWidth(),
                    config = {
                        wrap(FlexWrap.Wrap)
                        gap(8.dp)
                        maxItemsInEachLine(4)
                    }
                ) {
                    localSnippets.forEach { snippet ->
                        key(snippet) {
                            val forcedStyle = viewModel.getSnippetStyle(snippet)
                            val snippetColor = rememberSnippetBackgroundColor()
                            val bgColor = snippetColor
                            val textColor = rememberSnippetTextColor()
                            val interactionSource = remember { MutableInteractionSource() }

                            Surface(
                                shape = CircleShape,
                                color = bgColor,
                                border = null,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = ripple(color = MaterialTheme.colorScheme.primary),
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                            onRemove(snippet)
                                            localSnippets = localSnippets - snippet
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = snippet,
                                        style = getSnippetTextStyle(
                                            forcedStyle,
                                            MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = GoogleSansFlexRound,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            isCloud = true
                                        ),
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete $snippet",
                                            tint = textColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
