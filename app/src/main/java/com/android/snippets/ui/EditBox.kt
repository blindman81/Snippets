package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.android.snippets.logic.BoxManager
import com.android.snippets.ui.components.AppBar
import com.android.snippets.ui.components.ExpressiveSegmentedListItem

private data class EditBoxActionData(
    val id: String,
    val label: String,
    val subtext: String? = null,
    val icon: Any,
    val onClick: () -> Unit
)

@Composable
fun EditBoxScreen(
    box: String,
    userBoxes: List<String> = emptyList(),
    onNavigateBack: () -> Unit,
    onAction: (String) -> Unit
) {
    val view = LocalView.current
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    val isLibrary = box == "Box" || box == "Library"
    val isBox1 = box == "Box 1" || box == "Favorites"

    val boxDisplayName = remember(box, userBoxes) {
        BoxManager.getBoxDisplayName(box, userBoxes)
    }

    val actions = remember(box, boxDisplayName, isLibrary, isBox1) {
        val addSubtext = when {
            isLibrary -> "To Box"
            isBox1 -> "To Box 1"
            else -> "To $boxDisplayName"
        }
        val removeSubtext = when {
            isBox1 -> "From Box 1"
            else -> "From $boxDisplayName"
        }

        listOfNotNull(
            EditBoxActionData(
                id = EditBoxActivity.ACTION_ADD_PHOTOS,
                label = "Add photos",
                subtext = addSubtext,
                icon = Icons.Outlined.AddPhotoAlternate,
                onClick = { onAction(EditBoxActivity.ACTION_ADD_PHOTOS) }
            ),
            if (!isLibrary) EditBoxActionData(
                id = EditBoxActivity.ACTION_REMOVE_PHOTOS,
                label = "Remove photos",
                subtext = removeSubtext,
                icon = Icons.Outlined.Remove,
                onClick = { onAction(EditBoxActivity.ACTION_REMOVE_PHOTOS) }
            ) else null,
            if (!isLibrary && !isBox1) EditBoxActionData(
                id = EditBoxActivity.ACTION_EDIT_EMOJI,
                label = "Edit emoji",
                subtext = "Of $boxDisplayName",
                icon = Icons.Outlined.Edit,
                onClick = { onAction(EditBoxActivity.ACTION_EDIT_EMOJI) }
            ) else null,
            if (!isLibrary && !isBox1) EditBoxActionData(
                id = EditBoxActivity.ACTION_DELETE_BOX,
                label = "Delete $boxDisplayName",
                subtext = "Your photos will still be in Box",
                icon = Icons.Outlined.Delete,
                onClick = { onAction(EditBoxActivity.ACTION_DELETE_BOX) }
            ) else null
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            AppBar(
                title = "Edit box",
                onNavigationClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    onNavigateBack()
                },
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                isSpinning = true,
                isScrolled = isScrolled,
                leftAlignTitle = true,
                showDividerOnScroll = false,
                scrolledColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            actions.forEachIndexed { index, action ->
                ExpressiveSegmentedListItem(
                    index = index,
                    count = actions.size,
                    label = action.label,
                    subtext = action.subtext,
                    icon = action.icon,
                    onClick = action.onClick
                )
            }
        }
    }
}
