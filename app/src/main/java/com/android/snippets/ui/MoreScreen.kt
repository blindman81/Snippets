package com.android.snippets.ui

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.android.snippets.logic.BoxManager
import com.android.snippets.ui.components.AppBar
import com.android.snippets.ui.components.ExpressiveSegmentedListItem
import com.ln.android.snippets.R

private data class MoreItemData(
    val label: String,
    val icon: Any,
    val subtext: String? = null,
    val onClick: () -> Unit
)

@Composable
fun MoreScreen(
    userBoxes: List<String> = emptyList(),
    onNavigateBack: () -> Unit,
    onAction: (String) -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }

    val nextBoxName = remember(userBoxes) {
        BoxManager.generateNextBoxName(userBoxes)
    }

    val moreItems = remember(context, nextBoxName) {
        listOf(
            MoreItemData(
                label = "Create a Box",
                icon = R.drawable.ic_box,
                subtext = nextBoxName,
                onClick = { onAction(MoreActivity.ACTION_CREATE_BOX) }
            ),
            MoreItemData(
                label = "Stats",
                icon = R.drawable.ic_leaderboard_outlined,
                subtext = "Insights, ratings, location",
                onClick = { context.startActivity(Intent(context, StatsActivity::class.java)) }
            ),
            MoreItemData(
                label = "Settings",
                icon = Icons.Outlined.Settings,
                subtext = "Theme, icons, backup and restore",
                onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) }
            ),
            MoreItemData(
                label = "About",
                icon = Icons.Outlined.Info,
                subtext = "Credits, licenses, contact mail",
                onClick = { context.startActivity(Intent(context, AboutActivity::class.java)) }
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            AppBar(
                title = "More",
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
            moreItems.forEachIndexed { index, item ->
                ExpressiveSegmentedListItem(
                    index = index,
                    count = moreItems.size,
                    label = item.label,
                    subtext = item.subtext,
                    icon = item.icon,
                    onClick = item.onClick
                )
            }
        }
    }
}
