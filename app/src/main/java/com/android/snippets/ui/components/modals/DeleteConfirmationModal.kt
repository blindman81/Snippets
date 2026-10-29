package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeleteConfirmationModal(
    count: Int = 1,
    hasPublicPhotos: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    val view = LocalView.current
    var unpublish by remember { mutableStateOf(true) }
    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (count > 1) "Delete $count photos?" else "Delete photo?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (count > 1) "Deleting these photos also deletes the snippets you created for them." else "Deleting this photo also deletes the snippets you created for this photo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (hasPublicPhotos) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Checkbox(
                            checked = unpublish,
                            onCheckedChange = { unpublish = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Also remove from public community feed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onConfirm(unpublish)
        }
    )
}
