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
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.Check
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.rounded.Info

import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Keyboard

import androidx.compose.material3.*
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import com.android.snippets.ui.theme.GoogleSansFlexWide
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.ui.components.LoadingIndicator
import androidx.compose.material.icons.automirrored.rounded.Sort
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
import androidx.compose.material.icons.rounded.Image
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.android.snippets.ui.components.PremiumSwitch
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext

private data class PillInfo(
    val text: String,
    val icon: @Composable () -> Unit,
    val id: Long = System.currentTimeMillis()
)

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
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
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

    var activeInfo by remember { mutableStateOf<PillInfo?>(null) }
    var displayedInfo by remember { mutableStateOf<PillInfo?>(null) }
    var infoJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun showButtonInfo(name: String, icon: @Composable () -> Unit) {
        infoJob?.cancel()
        val info = PillInfo(name, icon, System.currentTimeMillis())
        displayedInfo = info
        activeInfo = info
        infoJob = scope.launch {
            kotlinx.coroutines.delay(3500)
            activeInfo = null
        }
    }

    fun showButtonInfo(name: String, iconVector: androidx.compose.ui.graphics.vector.ImageVector) {
        showButtonInfo(name) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    fun showButtonInfo(name: String, iconRes: Int) {
        showButtonInfo(name) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    val hazeState = LocalHazeState.current ?: rememberHazeState()

    CompositionLocalProvider(
        LocalHazeState provides hazeState
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                LargeMainTopBar(
                    title = "Settings",
                    onNavigationClick = { 
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        viewModel.navigateLibrary()
                    },
                    navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                    isSpinning = !(showThemeDialog || showCanvasDialog)
                )
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Spacer(modifier = Modifier.height(padding.calculateTopPadding()))

                Spacer(modifier = Modifier.height(16.dp))

                // Interface Section Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShapedSectionHeader(icon = MobileSectionIcon())
                    Text(
                        text = "Interface",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val themeOptions = listOf(ThemePreference.SYSTEM, ThemePreference.LIGHT, ThemePreference.DARK)
                val themeLabels = listOf("System", "Light", "Dark")
                val themeIcons = listOf(
                    AndroidIcon(),
                    if (viewModel.themePreference == ThemePreference.LIGHT) Icons.Rounded.LightMode else Icons.Outlined.LightMode,
                    if (viewModel.themePreference == ThemePreference.DARK) Icons.Rounded.DarkMode else Icons.Outlined.DarkMode
                )

                val selectedThemeIndex = themeOptions.indexOf(viewModel.themePreference).coerceAtLeast(0)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Box(modifier = Modifier.padding(6.dp)) {
                            CookieButtonGroup(
                                selectedIndex = selectedThemeIndex,
                                onSelect = { index ->
                                    val option = themeOptions[index]
                                    if (viewModel.themePreference != option) {
                                        viewModel.updateThemePreference(option)
                                    }
                                },
                                onItemClick = { index ->
                                    showButtonInfo(themeLabels[index], themeIcons[index])
                                },
                                icons = themeIcons,
                                contentDescriptions = themeLabels,
                                tooltips = themeLabels,
                                columns = 1,
                                cookieSize = 72.dp,
                                iconSize = 28.dp,
                                overlap = 0.dp,
                                horizontalAlignment = Alignment.Start
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val dynamicColorsIcon = if (viewModel.useDynamicColors) Icons.Rounded.FormatColorFill else Icons.Outlined.FormatColorFill
                val showTimeIcon = if (viewModel.showTimeInMemories) Icons.Rounded.Schedule else Icons.Outlined.Schedule
                val motionModeIcon = MotionModeIcon()

                val interfaceItems = listOf(
                    cookieButtonItem(
                        icon = dynamicColorsIcon,
                        contentDescription = "Dynamic colors",
                        tooltip = "Dynamic colors",
                        isSelected = viewModel.useDynamicColors,
                        iconSize = 28.dp,
                        onClick = {
                            val next = !viewModel.useDynamicColors
                            showButtonInfo("Dynamic colors", if (next) Icons.Rounded.FormatColorFill else Icons.Outlined.FormatColorFill)
                            viewModel.updateDynamicColors(next)
                        }
                    ),
                    cookieButtonItem(
                        icon = showTimeIcon,
                        contentDescription = "Time in memories and saved photos",
                        tooltip = "Time in memories and saved photos",
                        isSelected = viewModel.showTimeInMemories,
                        iconSize = 28.dp,
                        onClick = {
                            val next = !viewModel.showTimeInMemories
                            showButtonInfo("Time in memories and saved photos", if (next) Icons.Rounded.Schedule else Icons.Outlined.Schedule)
                            viewModel.updateShowTimeInMemories(next)
                        }
                    ),
                    cookieButtonItem(
                        icon = motionModeIcon,
                        contentDescription = "Motion sensor for button rotation",
                        tooltip = "Motion sensor for button rotation",
                        isSelected = viewModel.motionSensorForButtonRotation,
                        iconSize = 28.dp,
                        onClick = {
                            showButtonInfo("Motion sensor for button rotation", motionModeIcon)
                            viewModel.updateMotionSensorForButtonRotation(!viewModel.motionSensorForButtonRotation)
                        }
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Box(modifier = Modifier.padding(6.dp)) {
                            CookieButtonGroup(
                                items = interfaceItems,
                                columns = 1,
                                cookieSize = 72.dp,
                                overlap = 0.dp,
                                horizontalAlignment = Alignment.Start,
                                wiggleNeighborOnToggleOn = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val expandLabelIcon = if (viewModel.expandLabelIcon) Icons.AutoMirrored.Rounded.Label else Icons.AutoMirrored.Outlined.Label
                val expandCalendarIcon = if (viewModel.expandCalendarIcon) Icons.Rounded.CalendarMonth else Icons.Outlined.CalendarMonth
                val expandLocationIcon = if (viewModel.expandLocationIcon) Icons.Rounded.LocationOn else Icons.Outlined.LocationOn

                val expandItems = listOf(
                    cookieButtonItem(
                        icon = expandLabelIcon,
                        contentDescription = "Expand label icon",
                        tooltip = "Expand label icon",
                        isSelected = viewModel.expandLabelIcon,
                        iconSize = 28.dp,
                        onClick = {
                            val next = !viewModel.expandLabelIcon
                            showButtonInfo("Expand label icon", if (next) Icons.AutoMirrored.Rounded.Label else Icons.AutoMirrored.Outlined.Label)
                            viewModel.updateExpandLabelIcon(next)
                        }
                    ),
                    cookieButtonItem(
                        icon = expandCalendarIcon,
                        contentDescription = "Expand calendar icon",
                        tooltip = "Expand calendar icon",
                        isSelected = viewModel.expandCalendarIcon,
                        iconSize = 28.dp,
                        onClick = {
                            val next = !viewModel.expandCalendarIcon
                            showButtonInfo("Expand calendar icon", if (next) Icons.Rounded.CalendarMonth else Icons.Outlined.CalendarMonth)
                            viewModel.updateExpandCalendarIcon(next)
                        }
                    ),
                    cookieButtonItem(
                        icon = expandLocationIcon,
                        contentDescription = "Expand location icon",
                        tooltip = "Expand location icon",
                        isSelected = viewModel.expandLocationIcon,
                        iconSize = 28.dp,
                        onClick = {
                            val next = !viewModel.expandLocationIcon
                            showButtonInfo("Expand location icon", if (next) Icons.Rounded.LocationOn else Icons.Outlined.LocationOn)
                            viewModel.updateExpandLocationIcon(next)
                        }
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Box(modifier = Modifier.padding(6.dp)) {
                            CookieButtonGroup(
                                items = expandItems,
                                columns = 1,
                                cookieSize = 72.dp,
                                overlap = 0.dp,
                                horizontalAlignment = Alignment.Start,
                                wiggleNeighborOnToggleOn = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Actions Section Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShapedSectionHeader(icon = MoreSectionIcon())
                    Text(
                        text = "Actions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val backupIcon = BackupSectionIcon()
                val importIcon = ImportSectionIcon()

                val isAutoBackupOn = viewModel.autoBackupSchedule != "Disabled"
                val autoBackupIcon = if (isAutoBackupOn) Icons.Rounded.CloudSync else Icons.Outlined.CloudSync

                val actionItems = listOf(
                    cookieButtonItem(
                        icon = backupIcon,
                        contentDescription = "Export",
                        tooltip = "Export",
                        isSelected = false,
                        iconSize = 28.dp,
                        onClick = {
                            showButtonInfo("Export", backupIcon)
                            exportLauncher.launch("snippets_backup_${System.currentTimeMillis() / 1000}.json")
                        }
                    ),
                    cookieButtonItem(
                        icon = importIcon,
                        contentDescription = "Import",
                        tooltip = "Import",
                        isSelected = false,
                        iconSize = 28.dp,
                        onClick = {
                            showButtonInfo("Import", importIcon)
                            importLauncher.launch(arrayOf("application/json", "application/zip", "application/octet-stream", "*/*"))
                        }
                    ),
                    cookieButtonItem(
                        icon = autoBackupIcon,
                        contentDescription = "Auto backup",
                        tooltip = "Auto backup",
                        isSelected = isAutoBackupOn,
                        iconSize = 28.dp,
                        onClick = {
                            showButtonInfo("Auto backup", autoBackupIcon)
                            showAutoBackupDialog = true
                        }
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Box(modifier = Modifier.padding(6.dp)) {
                            CookieButtonGroup(
                                items = actionItems,
                                columns = 1,
                                cookieSize = 72.dp,
                                overlap = 0.dp,
                                horizontalAlignment = Alignment.Start,
                                enableWiggle = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = activeInfo != null,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(animationSpec = tween(200)),
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut(animationSpec = tween(200))
                ) {
                    displayedInfo?.let { currentInfo ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .heightIn(min = 36.dp)
                                .animateContentSize(),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                currentInfo.icon()
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = currentInfo.text,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = GoogleSansFlexWide
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }


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
    label: String,
    isFirst: Boolean,
    isLast: Boolean
) {
    customItem(
        buttonGroupContent = {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val targetWeight = when {
                checked && isPressed -> 0.85f
                checked -> 1.15f
                isPressed -> 0.75f
                else -> 1.0f
            }
            val animatedWeight by animateFloatAsState(
                targetValue = targetWeight,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                ),
                label = "settings_weight_$label"
            )

            val shapes = when {
                isFirst && isLast -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                isFirst -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                isLast -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }

            ToggleButton(
                checked = checked,
                onCheckedChange = onCheckedChange,
                interactionSource = interactionSource,
                modifier = Modifier.weight(animatedWeight),
                shapes = shapes,
                colors = ToggleButtonDefaults.colors(
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
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
                        MaterialTheme.typography.labelLarge.copy(
                            fontSize = 11.sp
                        )
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
                            MaterialTheme.typography.labelLarge.copy(
                                fontSize = 11.sp
                            )
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
fun AutoBackupScheduleDialog(
    currentSchedule: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedSchedule by remember { mutableStateOf(currentSchedule) }
    val options = listOf("Disabled", "Daily", "Weekly", "Monthly")
    val labels = listOf("Disabled", "Daily", "Weekly (Sunday)", "Monthly (1st of month)")

    BlurredAlertDialog(
        onDismissRequest = onDismiss,
        onConfirm = { onConfirm(selectedSchedule) },
        title = {
            Text(
                text = "Auto backup",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                            .clip(RoundedCornerShape(12.dp))
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
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = true)
    )
}

