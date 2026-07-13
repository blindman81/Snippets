package com.android.snippets.ui
import com.android.snippets.ui.components.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(viewModel: SnippetsViewModel) {
    androidx.activity.compose.BackHandler {
        viewModel.navigateLibrary()
    }
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val systemDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = when (viewModel.themePreference) {
        ThemePreference.SYSTEM -> systemDarkTheme
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCanvasDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val isScrolled by remember { derivedStateOf<Boolean> { scrollState.value > 0 } }
    
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            MainTopBar(
                title = "Settings",
                onNavigationClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    viewModel.navigateLibrary()
                },
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                isSpinning = !(showThemeDialog || showCanvasDialog),
                isScrolled = isScrolled,
                leftAlignTitle = true
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()),
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .animateContentSize(spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Theme",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 8.dp)
            )

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


            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Color",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 8.dp)
            )

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
            
            Text(
                text = "Management",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 8.dp)
            )

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
                title = "Daily notification reminder",
                subtitle = reminderTimeStr,
                onClick = { 
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    showTimePicker = true
                },
                position = CardPosition.Last,
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

            if (showTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = viewModel.notificationReminderHour,
                    initialMinute = viewModel.notificationReminderMinute,
                    is24Hour = is24Hour
                )
                var showDial by remember { mutableStateOf(true) }
                val toggleIcon = if (showDial) Icons.Filled.Keyboard else Icons.Filled.Schedule

                AdvancedTimePickerDialog(
                    title = "Select Reminder Time",
                    onDismiss = {
                        showTimePicker = false
                    },
                    onConfirm = {
                        viewModel.updateNotificationReminder(
                            enabled = true,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                        showTimePicker = false
                    },
                    toggle = {
                        IconButton(onClick = { showDial = !showDial }) {
                            Icon(
                                imageVector = toggleIcon,
                                contentDescription = "Time picker type toggle"
                            )
                        }
                    }
                ) {
                    if (showDial) {
                        TimePicker(state = timePickerState)
                    } else {
                        TimeInput(state = timePickerState)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Shape",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 8.dp)
            )

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

            SettingsCardItem(
                icon = Icons.Default.Image,
                title = "Make photos follow the shape",
                onClick = { 
                    view.performHapticFeedback(if (!viewModel.makePhotosFollowShape) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT)
                    viewModel.updateMakePhotosFollowShape(!viewModel.makePhotosFollowShape) 
                },
                position = CardPosition.Last,
                trailingContent = {
                    PremiumSwitch(
                        checked = viewModel.makePhotosFollowShape,
                        onCheckedChange = { 
                            viewModel.updateMakePhotosFollowShape(it) 
                        }
                    )
                }
            )
 
             Spacer(modifier = Modifier.height(16.dp))
             
             Spacer(modifier = Modifier.height(32.dp))
         }
     }
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
