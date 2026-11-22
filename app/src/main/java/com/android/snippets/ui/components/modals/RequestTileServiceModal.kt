package com.android.snippets.ui.components.modals

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.components.BlurredAlertDialog
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.ln.android.snippets.R

@Composable
fun RequestTileServiceModal(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val view = LocalView.current
    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_qs_add_photo),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.request_tile_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GoogleSansFlexRound,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                fontFamily = GoogleSansFlexRound
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.request_tile_description),
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = GoogleSansFlexRound),
                fontFamily = GoogleSansFlexRound,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        onConfirm = {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            onConfirm()
        }
    )
}
