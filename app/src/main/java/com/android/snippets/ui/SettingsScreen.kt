package com.android.snippets.ui
import com.android.snippets.ui.components.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.FormatColorFill

import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Keyboard

import androidx.compose.material3.*
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.android.snippets.ui.components.LoadingIndicator
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import com.android.snippets.viewmodel.SnippetsViewModel
import com.android.snippets.viewmodel.ThemePreference
import com.android.snippets.viewmodel.Screen
import androidx.compose.material.icons.filled.Image
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.android.snippets.ui.components.PremiumSwitch
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.android.snippets.ui.shapes.AppShape
import com.android.snippets.ui.shapes.toComposeShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(viewModel: SnippetsViewModel) {
    AppPredictiveBackHandler {
        viewModel.navigateLibrary()
    }
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupToFile(context, uri)
        }
    }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackupFromFile(context, uri)
        }
    }
    val systemDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = when (viewModel.themePreference) {
        ThemePreference.SYSTEM -> systemDarkTheme
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCanvasDialog by remember { mutableStateOf(false) }
    var showAutoBackupDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            LargeMainTopBar(
                title = "Settings",
                onNavigationClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    viewModel.navigateLibrary()
                },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                isSpinning = !(showThemeDialog || showCanvasDialog),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = padding.calculateBottomPadding())
                    .animateContentSize(spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Spacer(modifier = Modifier.height(padding.calculateTopPadding()))

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShapedSectionHeader(icon = SettingsSectionIcon())
                Text(
                    text = "Palette",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }


            val themeOptions = listOf(ThemePreference.SYSTEM, ThemePreference.LIGHT, ThemePreference.DARK)
            val themeLabels = listOf("System", "Light", "Dark")
            val themeIcons = listOf(AndroidIcon(), Icons.Default.LightMode, Icons.Default.DarkMode)

            ButtonGroup(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                overflowIndicator = {}
            ) {
                themeOptions.forEachIndexed { index, option ->
                    val isSelected = viewModel.themePreference == option
                    themeToggleableItem(
                        weight = 1f,
                        checked = isSelected,
                        onCheckedChange = {
                            if (!isSelected) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.updateThemePreference(option)
                            }
                        },
                        icon = { Icon(themeIcons[index], null, modifier = Modifier.size(if (isSelected) 24.dp else 18.dp)) },
                        label = themeLabels[index]
                    )
                }
            }

            SettingsCardItem(
                icon = Icons.Default.FormatColorFill,
                title = "Dynamic Colors",
                onClick = { 
                    view.performHapticFeedback(if (!viewModel.useDynamicColors) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT)
                    viewModel.updateDynamicColors(!viewModel.useDynamicColors) 
                },
                position = CardPosition.Single,
                trailingContent = {

                    PremiumSwitch(
                        checked = viewModel.useDynamicColors,
                        onCheckedChange = { 
                            viewModel.updateDynamicColors(it) 
                        }
                    )
                }
            )



            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShapedSectionHeader(icon = NotificationSectionIcon())
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }


            var showTimePicker by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val is24Hour = android.text.format.DateFormat.is24HourFormat(context)

            val reminderTimeStr = if (viewModel.notificationReminderEnabled) {
                val isPm = viewModel.notificationReminderHour >= 12
                val displayHour = when {
                    viewModel.notificationReminderHour == 0 -> 12
                    viewModel.notificationReminderHour > 12 -> viewModel.notificationReminderHour - 12
                    else -> viewModel.notificationReminderHour
                }
                String.format("Daily at %02d:%02d %s", displayHour, viewModel.notificationReminderMinute, if (isPm) "PM" else "AM")
            } else {
                "Disabled"
            }

            SettingsCardItem(
                icon = com.ln.android.snippets.R.drawable.ic_daily_reminder,
                title = "Memory notification scheduling",
                subtitle = reminderTimeStr,
                onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    showTimePicker = true
                },
                position = CardPosition.Single,
                trailingContent = {
                    PremiumSwitch(
                        checked = viewModel.notificationReminderEnabled,
                        onCheckedChange = { checked ->
                            view.performHapticFeedback(if (checked) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT)
                            if (checked) {
                                viewModel.updateNotificationReminder(
                                    enabled = true,
                                    hour = viewModel.notificationReminderHour,
                                    minute = viewModel.notificationReminderMinute
                                )
                                showTimePicker = true
                            } else {
                                viewModel.updateNotificationReminder(
                                    enabled = false,
                                    hour = viewModel.notificationReminderHour,
                                    minute = viewModel.notificationReminderMinute
                                )
                            }
                        }
                    )
                }
            )

            val timePickerState = rememberTimePickerState(
                initialHour = viewModel.notificationReminderHour,
                initialMinute = viewModel.notificationReminderMinute,
                is24Hour = is24Hour
            )

            if (showTimePicker) {
                AdvancedTimePickerDialog(
                    title = "Set reminder time",
                    onDismiss = { showTimePicker = false },
                    onConfirm = {
                        showTimePicker = false
                        viewModel.updateNotificationReminder(
                            enabled = true,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                    },
                    content = { TimePicker(state = timePickerState) }
                )
            }


            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShapedSectionHeader(icon = ShapesSectionIcon())
                Text(
                    text = "Shape",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }


            SettingsCardItem(
                icon = Icons.Default.Palette,
                title = "App Shape",
                subtitle = viewModel.selectedShape.displayName,
                onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    viewModel.navigateChooseShape()
                },
                position = CardPosition.First
            )

            val isCardView = viewModel.customGridColumns == -1
            SettingsCardItem(
                icon = Icons.Default.Image,
                title = "Make photos follow the shape",
                onClick = if (isCardView) null else { 
                    {
                        view.performHapticFeedback(if (!viewModel.makePhotosFollowShape) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT)
                        viewModel.updateMakePhotosFollowShape(!viewModel.makePhotosFollowShape) 
                    }
                },
                position = CardPosition.Last,
                modifier = Modifier.alpha(if (isCardView) 0.5f else 1f),
                trailingContent = {
                    PremiumSwitch(
                        checked = if (isCardView) true else viewModel.makePhotosFollowShape,
                        enabled = !isCardView,
                        onCheckedChange = { 
                            if (!isCardView) viewModel.updateMakePhotosFollowShape(it) 
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShapedSectionHeader(icon = CollectionsSectionIcon())
                Text(
                    text = "Collections",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }


            SettingsCardItem(
                icon = Icons.Default.Collections,
                title = "Show Eatlist",
                onClick = { 
                    view.performHapticFeedback(if (!viewModel.showEatlist) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT)
                    viewModel.updateShowEatlist(!viewModel.showEatlist) 
                },
                position = CardPosition.Single,
                trailingContent = {
                    PremiumSwitch(
                        checked = viewModel.showEatlist,
                        onCheckedChange = { 
                            viewModel.updateShowEatlist(it) 
                        }
                    )
                }
            )
 
             Spacer(modifier = Modifier.height(16.dp))

             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(horizontal = 20.dp, vertical = 8.dp),
                 verticalAlignment = Alignment.CenterVertically,
                 horizontalArrangement = Arrangement.spacedBy(12.dp)
             ) {
                 ShapedSectionHeader(icon = BackupSectionIcon())
                 Text(
                     text = "Backup",
                     style = MaterialTheme.typography.titleMedium,
                     color = MaterialTheme.colorScheme.onSurface,
                     fontWeight = FontWeight.Bold
                 )
             }

             SettingsCardItem(
                 icon = BackupSectionIcon(),
                 title = "Export",
                 subtitle = "Save snippets, collections, and photos to a file",
                 onClick = {
                     view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                     exportLauncher.launch("snippets_backup_${System.currentTimeMillis() / 1000}.zip")
                 },
                 position = CardPosition.First
             )

             SettingsCardItem(
                 icon = ImportSectionIcon(),
                 title = "Import",
                 subtitle = "Restore snippets, collections, and photos from a file",
                 onClick = {
                     view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                     importLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                 },
                 position = CardPosition.Middle
             )

             val autoBackupSubtitle = when (viewModel.autoBackupSchedule) {
                 "Daily" -> "Back up automatically every day"
                 "Weekly" -> "Back up automatically every Sunday"
                 "Monthly" -> "Back up automatically on 1st of month"
                 else -> "Automatic backups are turned off"
             }
             SettingsCardItem(
                 icon = BackupSectionIcon(),
                 title = "Auto Backup",
                 subtitle = autoBackupSubtitle,
                 onClick = {
                     view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                     showAutoBackupDialog = true
                 },
                 position = CardPosition.Last
             )

             Spacer(modifier = Modifier.height(16.dp))

             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(horizontal = 20.dp, vertical = 8.dp),
                 verticalAlignment = Alignment.CenterVertically,
                 horizontalArrangement = Arrangement.spacedBy(12.dp)
             ) {
                 ShapedSectionHeader(icon = MoreSectionIcon())
                 Text(
                     text = "More",
                     style = MaterialTheme.typography.titleMedium,
                     color = MaterialTheme.colorScheme.onSurface,
                     fontWeight = FontWeight.Bold
                 )
             }


             SettingsCardItem(
                 icon = Icons.Default.Schedule,
                 title = "Show time in memories and saved photos",
                 onClick = {
                     view.performHapticFeedback(if (!viewModel.showTimeInMemories) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT)
                     viewModel.updateShowTimeInMemories(!viewModel.showTimeInMemories)
                 },
                 position = CardPosition.First,
                 trailingContent = {
                     PremiumSwitch(
                         checked = viewModel.showTimeInMemories,
                         onCheckedChange = {
                             viewModel.updateShowTimeInMemories(it)
                         }
                     )
                 }
             )

             SettingsCardItem(
                 icon = Icons.Default.Image,
                 title = "Photo takes full width and height in section",
                 onClick = {
                     view.performHapticFeedback(if (!viewModel.fullSizePhotoInCard) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT)
                     viewModel.updateFullSizePhotoInCard(!viewModel.fullSizePhotoInCard)
                 },
                 position = CardPosition.Last,
                 trailingContent = {
                     PremiumSwitch(
                         checked = viewModel.fullSizePhotoInCard,
                         onCheckedChange = {
                             viewModel.updateFullSizePhotoInCard(it)
                         }
                     )
                 }
             )

             Spacer(modifier = Modifier.height(16.dp))
             
             Spacer(modifier = Modifier.height(32.dp))
         }
     }
 }

 if (showAutoBackupDialog) {
     AutoBackupScheduleDialog(
         currentSchedule = viewModel.autoBackupSchedule,
         onDismiss = { showAutoBackupDialog = false },
         onConfirm = { selected ->
             viewModel.updateAutoBackupSchedule(selected)
             showAutoBackupDialog = false
         }
     )
 }
}

@Composable
private fun settingsSwitchColors(useDarkTheme: Boolean): SwitchColors {
    val colors = MaterialTheme.colorScheme
    
    return SwitchDefaults.colors(
        checkedThumbColor = if (useDarkTheme) colors.surfaceContainerHighest else colors.background,
        checkedTrackColor = colors.secondary,
        checkedBorderColor = colors.secondary,
        checkedIconColor = colors.secondary,
        uncheckedThumbColor = colors.outline,
        uncheckedTrackColor = colors.surfaceContainer,
        uncheckedBorderColor = colors.outline,
        uncheckedIconColor = colors.surfaceContainerHighest
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ButtonGroupScope.themeToggleableItem(
    weight: Float,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit,
    label: String
) {
    val itemModifier = Modifier.weight(weight)
    customItem(
        buttonGroupContent = {
            ToggleButton(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = itemModifier
            ) {
                icon()
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = label,
                    style = if (checked) {
                        MaterialTheme.typography.labelLarge.copy(
                            fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide,
                            fontSize = 11.sp
                        )
                    } else {
                        MaterialTheme.typography.labelLarge
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        menuContent = { state ->
            DropdownMenuItem(
                leadingIcon = icon,
                text = {
                    Text(
                        text = label,
                        style = if (checked) {
                            MaterialTheme.typography.labelLarge.copy(
                                fontFamily = com.android.snippets.ui.theme.GoogleSansFlexWide,
                                fontSize = 11.sp
                            )
                        } else {
                            MaterialTheme.typography.labelLarge
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                onClick = {
                    onCheckedChange(!checked)
                    state.dismiss()
                }
            )
        }
    )
}

@Composable
fun AdvancedTimePickerDialog(
    title: String = "Select Time",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    toggle()
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

@Composable
fun AutoBackupScheduleDialog(
    currentSchedule: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedSchedule by remember { mutableStateOf(currentSchedule) }
    val options = listOf("Disabled", "Daily", "Weekly", "Monthly")
    val labels = listOf("Disabled", "Daily", "Weekly (Sunday)", "Monthly (1st of month)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Auto Backup Schedule",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (selectedSchedule == option),
                                onClick = { selectedSchedule = option },
                                role = androidx.compose.ui.semantics.Role.RadioButton
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedSchedule == option),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = labels[index],
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedSchedule) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = true)
    )
}

