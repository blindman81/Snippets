package com.android.snippets.ui
import com.android.snippets.ui.components.*
import com.ln.android.snippets.R
import androidx.compose.ui.res.vectorResource

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
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext

private data class SettingItemData(
    val title: String,
    val icon: ImageVector,
    val isSelected: Boolean? = null,
    val isDoubleLine: Boolean = false,
    val useMarquee: Boolean = false,
    val onClick: () -> Unit
)

@Composable
private fun SettingItemView(
    item: SettingItemData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.zIndex(10f),
            contentAlignment = Alignment.Center
        ) {
            AnimatedCookieButton(
                onClick = item.onClick,
                icon = item.icon,
                contentDescription = item.title.replace("\n", " "),
                isSelected = item.isSelected,
                size = 84.dp,
                iconSize = 38.dp
            )
        }

        SettingItemPill(
            title = item.title,
            isDoubleLine = item.isDoubleLine,
            useMarquee = item.useMarquee
        )
    }
}

@Composable
private fun SettingItemPill(
    title: String,
    isDoubleLine: Boolean = false,
    useMarquee: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pillModifier = if (isDoubleLine) {
        modifier
            .widthIn(min = 64.dp, max = 104.dp)
            .height(48.dp)
    } else {
        modifier
            .widthIn(min = 60.dp, max = 104.dp)
    }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = pillModifier
    ) {
        Box(
            modifier = if (isDoubleLine) {
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            } else {
                Modifier
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .clipToBounds()
            },
            contentAlignment = Alignment.Center
        ) {
            if (useMarquee) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = GoogleSansFlexRound
                    ),
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE
                    )
                )
            } else if (isDoubleLine) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = GoogleSansFlexRound,
                        lineHeight = 14.sp
                    ),
                    maxLines = 2,
                    softWrap = true,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = GoogleSansFlexRound
                    ),
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    items: List<SettingItemData>,
    modifier: Modifier = Modifier,
    columns: Int = items.size
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        items.forEach { item ->
            key(item.title) {
                SettingItemView(
                    item = item,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (items.size < columns) {
            repeat(columns - items.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(viewModel: SnippetsViewModel) {
    val view = LocalView.current
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
    
    var showAutoBackupDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val hazeState = LocalHazeState.current ?: rememberHazeState()

    CompositionLocalProvider(
        LocalHazeState provides hazeState
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                MainTopBar(
                    title = "Settings",
                    onNavigationClick = { 
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        viewModel.navigateLibrary()
                    },
                    navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                    isSpinning = !showAutoBackupDialog,
                    isScrolled = scrollState.value > 0,
                    leftAlignTitle = true,
                    showDividerOnScroll = false
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
                        .padding(bottom = padding.calculateBottomPadding())
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
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
                        ShapedSectionHeader(icon = ImageVector.vectorResource(R.drawable.ic_mobile_section))
                        Text(
                            text = "Interface",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val androidIcon = ImageVector.vectorResource(R.drawable.ic_android_logo)
                    val themeItems = remember(viewModel.themePreference, androidIcon) {
                        listOf(
                            SettingItemData(
                                title = "System",
                                icon = androidIcon,
                                isSelected = viewModel.themePreference == ThemePreference.SYSTEM,
                                onClick = { viewModel.updateThemePreference(ThemePreference.SYSTEM) }
                            ),
                            SettingItemData(
                                title = "Light",
                                icon = if (viewModel.themePreference == ThemePreference.LIGHT) Icons.Rounded.LightMode else Icons.Outlined.LightMode,
                                isSelected = viewModel.themePreference == ThemePreference.LIGHT,
                                onClick = { viewModel.updateThemePreference(ThemePreference.LIGHT) }
                            ),
                            SettingItemData(
                                title = "Dark",
                                icon = if (viewModel.themePreference == ThemePreference.DARK) Icons.Rounded.DarkMode else Icons.Outlined.DarkMode,
                                isSelected = viewModel.themePreference == ThemePreference.DARK,
                                onClick = { viewModel.updateThemePreference(ThemePreference.DARK) }
                            )
                        )
                    }

                    SettingsRow(items = themeItems)

                    Spacer(modifier = Modifier.height(20.dp))

                    val dynamicColorsIcon = if (viewModel.useDynamicColors) Icons.Rounded.FormatColorFill else Icons.Outlined.FormatColorFill
                    val expandLabelIcon = if (viewModel.expandLabelIcon) Icons.AutoMirrored.Rounded.Label else Icons.AutoMirrored.Outlined.Label
                    val expandCalendarIcon = if (viewModel.expandCalendarIcon) Icons.Rounded.CalendarMonth else Icons.Outlined.CalendarMonth
                    val expandLocationIcon = if (viewModel.expandLocationIcon) Icons.Rounded.LocationOn else Icons.Outlined.LocationOn

                    val interfaceRow1Items = remember(
                        viewModel.useDynamicColors,
                        viewModel.expandLabelIcon,
                        viewModel.expandCalendarIcon,
                        dynamicColorsIcon,
                        expandLabelIcon,
                        expandCalendarIcon
                    ) {
                        listOf(
                            SettingItemData(
                                title = "Dynamic\ncolors",
                                icon = dynamicColorsIcon,
                                isSelected = viewModel.useDynamicColors,
                                isDoubleLine = true,
                                onClick = { viewModel.updateDynamicColors(!viewModel.useDynamicColors) }
                            ),
                            SettingItemData(
                                title = "Expand\nlabel icon",
                                icon = expandLabelIcon,
                                isSelected = viewModel.expandLabelIcon,
                                isDoubleLine = true,
                                onClick = { viewModel.updateExpandLabelIcon(!viewModel.expandLabelIcon) }
                            ),
                            SettingItemData(
                                title = "Expand\ncalendar icon",
                                icon = expandCalendarIcon,
                                isSelected = viewModel.expandCalendarIcon,
                                isDoubleLine = true,
                                onClick = { viewModel.updateExpandCalendarIcon(!viewModel.expandCalendarIcon) }
                            )
                        )
                    }

                    val interfaceRow2Items = remember(viewModel.expandLocationIcon, expandLocationIcon) {
                        listOf(
                            SettingItemData(
                                title = "Expand\nlocation icon",
                                icon = expandLocationIcon,
                                isSelected = viewModel.expandLocationIcon,
                                isDoubleLine = true,
                                onClick = { viewModel.updateExpandLocationIcon(!viewModel.expandLocationIcon) }
                            )
                        )
                    }

                    SettingsRow(items = interfaceRow1Items)

                    Spacer(modifier = Modifier.height(20.dp))

                    SettingsRow(items = interfaceRow2Items, columns = 3)

                    Spacer(modifier = Modifier.height(28.dp))

                    // Actions Section Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ShapedSectionHeader(icon = ImageVector.vectorResource(R.drawable.ic_more_section))
                        Text(
                            text = "Actions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val isAutoBackupOn = viewModel.autoBackupSchedule != "Disabled"
                    val autoBackupIcon = if (isAutoBackupOn) Icons.Rounded.CloudSync else Icons.Outlined.CloudSync

                    val backupIcon = ImageVector.vectorResource(R.drawable.ic_backup_section)
                    val importIcon = ImageVector.vectorResource(R.drawable.ic_import_section)
                    val actionItems = remember(isAutoBackupOn, backupIcon, importIcon) {
                        listOf(
                            SettingItemData(
                                title = "Export",
                                icon = backupIcon,
                                isSelected = null,
                                onClick = { exportLauncher.launch("snippets_backup_${System.currentTimeMillis() / 1000}.json") }
                            ),
                            SettingItemData(
                                title = "Import",
                                icon = importIcon,
                                isSelected = null,
                                onClick = { importLauncher.launch(arrayOf("application/json", "application/zip", "application/octet-stream", "*/*")) }
                            ),
                            SettingItemData(
                                title = "Auto backup",
                                icon = autoBackupIcon,
                                isSelected = isAutoBackupOn,
                                onClick = { showAutoBackupDialog = true }
                            )
                        )
                    }

                    SettingsRow(items = actionItems)

                    Spacer(modifier = Modifier.height(36.dp))
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

