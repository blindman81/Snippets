package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.snippets.model.ThemePreference
import com.android.snippets.ui.components.AppBar
import com.android.snippets.ui.components.ExpressiveSegmentedListItem
import com.android.snippets.ui.components.LocalHazeState
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.viewmodel.SettingsViewModel
import com.ln.android.snippets.R
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val view = LocalView.current
    val context = LocalContext.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupToFile(context, uri)
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreBackupFromFile(context, uri)
        }
    }

    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }
    val hazeState = LocalHazeState.current ?: rememberHazeState()

    val themeItems = remember(viewModel.themePreference) {
        listOf(
            Triple(
                "System",
                R.drawable.ic_android_logo,
                viewModel.themePreference == ThemePreference.SYSTEM
            ),
            Triple(
                "Light",
                if (viewModel.themePreference == ThemePreference.LIGHT) Icons.Rounded.LightMode else Icons.Outlined.LightMode,
                viewModel.themePreference == ThemePreference.LIGHT
            ),
            Triple(
                "Dark",
                if (viewModel.themePreference == ThemePreference.DARK) Icons.Rounded.DarkMode else Icons.Outlined.DarkMode,
                viewModel.themePreference == ThemePreference.DARK
            )
        )
    }

    val expandLabelIcon = if (viewModel.expandLabelIcon) Icons.AutoMirrored.Rounded.Label else Icons.AutoMirrored.Outlined.Label
    val expandCalendarIcon = if (viewModel.expandCalendarIcon) Icons.Rounded.CalendarMonth else Icons.Outlined.CalendarMonth
    val expandLocationIcon = if (viewModel.expandLocationIcon) {
        R.drawable.ic_location_on_filled
    } else {
        R.drawable.ic_location_on_outlined
    }

    val expandItems = remember(
        viewModel.expandLabelIcon,
        viewModel.expandCalendarIcon,
        viewModel.expandLocationIcon,
        expandLabelIcon,
        expandCalendarIcon,
        expandLocationIcon
    ) {
        listOf(
            Triple("Expand label icon", expandLabelIcon, viewModel.expandLabelIcon),
            Triple("Expand calendar icon", expandCalendarIcon, viewModel.expandCalendarIcon),
            Triple("Expand location icon", expandLocationIcon, viewModel.expandLocationIcon)
        )
    }

    val isAutoBackupOn = viewModel.autoBackupSchedule != "Disabled"
    val autoBackupIcon = if (isAutoBackupOn) {
        R.drawable.ic_backup_filled
    } else {
        R.drawable.ic_backup_outlined
    }
    val exportIcon = R.drawable.ic_file_export
    val restoreIcon = R.drawable.ic_settings_backup_restore

    CompositionLocalProvider(
        LocalHazeState provides hazeState
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                topBar = {
                    AppBar(
                        title = "Settings",
                        onNavigationClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                            backDispatcher?.onBackPressed() ?: (view.context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed() ?: (view.context as? ComponentActivity)?.finish()
                        },
                        navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                        isSpinning = true,
                        isScrolled = isScrolled,
                        leftAlignTitle = true,
                        showDividerOnScroll = false,
                        scrolledColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                    // --- THEME SECTION ---
                    SectionHeader(
                        title = "Theme",
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )

                    themeItems.forEachIndexed { index, (name, icon, isSelected) ->
                        val pref = when (index) {
                            0 -> ThemePreference.SYSTEM
                            1 -> ThemePreference.LIGHT
                            else -> ThemePreference.DARK
                        }
                        ExpressiveSegmentedListItem(
                            index = index,
                            count = themeItems.size,
                            label = name,
                            icon = icon,
                            iconContainerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                            iconColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            selected = isSelected,
                            onClick = {
                                viewModel.updateThemePreference(pref)
                            }
                        )
                    }

                    // --- ICON CUSTOMISATION SECTION ---
                    SectionHeader(
                        title = "Icon customisation",
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )

                    expandItems.forEachIndexed { index, (label, icon, isSelected) ->
                        ExpressiveSegmentedListItem(
                            index = index,
                            count = expandItems.size,
                            label = label,
                            icon = icon,
                            iconContainerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                            iconColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            selected = isSelected,
                            onClick = {
                                val nextEnabled = !isSelected
                                when (index) {
                                    0 -> viewModel.updateExpandLabelIcon(nextEnabled)
                                    1 -> viewModel.updateExpandCalendarIcon(nextEnabled)
                                    2 -> viewModel.updateExpandLocationIcon(nextEnabled)
                                }
                            }
                        )
                    }

                    // --- BACKUP AND RESTORE SECTION ---
                    SectionHeader(
                        title = "Backup and restore",
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )

                    ExpressiveSegmentedListItem(
                        index = 0,
                        count = 3,
                        label = "Auto backup",
                        subtext = viewModel.autoBackupSchedule,
                        icon = autoBackupIcon,
                        iconContainerColor = if (isAutoBackupOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                        iconColor = if (isAutoBackupOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        selected = isAutoBackupOn,
                        onClick = {
                            val nextSchedule = if (isAutoBackupOn) "Disabled" else "Daily"
                            viewModel.updateAutoBackupSchedule(nextSchedule)
                            if (nextSchedule == "Daily") {
                                val currentTime = android.text.format.DateFormat.getTimeFormat(context).format(java.util.Date())
                                Toast.makeText(
                                    context,
                                    "Auto backup occurs at $currentTime",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )

                    ExpressiveSegmentedListItem(
                        index = 1,
                        count = 3,
                        label = "Export",
                        icon = exportIcon,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        iconColor = MaterialTheme.colorScheme.onSurface,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = {
                            exportLauncher.launch("snippets_backup_${System.currentTimeMillis() / 1000}.zip")
                        }
                    )

                    ExpressiveSegmentedListItem(
                        index = 2,
                        count = 3,
                        label = "Restore",
                        icon = restoreIcon,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        iconColor = MaterialTheme.colorScheme.onSurface,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = {
                            restoreLauncher.launch(arrayOf("application/json", "application/zip", "application/octet-stream", "*/*"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontFamily = GoogleSansFlexRound,
            fontWeight = FontWeight.SemiBold
        ),
        color = color,
        modifier = modifier.padding(start = 8.dp)
    )
}
