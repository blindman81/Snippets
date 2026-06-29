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
import androidx.compose.material.icons.filled.FormatColorFill

import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings

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
import androidx.compose.material.icons.filled.Image
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.android.snippets.ui.components.PremiumSwitch
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import com.android.snippets.ui.shapes.AppShape
import com.android.snippets.ui.shapes.toComposeShape
import androidx.compose.ui.graphics.Color

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
    var showShapeDialog by remember { mutableStateOf(false) }
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
                isSpinning = !(showThemeDialog || showCanvasDialog || showShapeDialog),
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
                    toggleableItem(
                        weight = 1f,
                        checked = isSelected,
                        onCheckedChange = {
                            if (!isSelected) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                viewModel.updateThemePreference(option)
                            }
                        },
                        icon = { Icon(themeIcons[index], null, modifier = Modifier.size(18.dp)) },
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
                position = CardPosition.Single,
                trailingContent = {

                    PremiumSwitch(
                        checked = viewModel.showTimeInMemories,
                        onCheckedChange = { 
                            viewModel.updateShowTimeInMemories(it) 
                        }
                    )
                }
            )

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
                    showShapeDialog = true 
                },
                position = CardPosition.Single
            )
 
             Spacer(modifier = Modifier.height(16.dp))
             
             Spacer(modifier = Modifier.height(32.dp))
         }
     }
 }

     if (showShapeDialog) {
         AlertDialog(
             onDismissRequest = { showShapeDialog = false },
             title = {
                 Text(
                     text = "Choose Shape",
                     style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold
                 )
             },
             text = {
                 Column(
                     modifier = Modifier
                         .fillMaxWidth()
                         .verticalScroll(rememberScrollState())
                         .padding(vertical = 8.dp),
                     verticalArrangement = Arrangement.spacedBy(12.dp)
                 ) {
                     AppShape.values().forEach { shape ->
                         val isSelected = viewModel.selectedShape == shape
                         Surface(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .clickable {
                                     view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                     viewModel.updateSelectedShape(shape)
                                     showShapeDialog = false
                                 },
                             shape = RoundedCornerShape(16.dp),
                             color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                             border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
                         ) {
                             Row(
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .padding(horizontal = 16.dp, vertical = 12.dp),
                                 verticalAlignment = Alignment.CenterVertically,
                                 horizontalArrangement = Arrangement.spacedBy(16.dp)
                             ) {
                                 Box(
                                     modifier = Modifier
                                         .size(48.dp)
                                         .clip(shape.toComposeShape())
                                         .background(
                                             androidx.compose.ui.graphics.Brush.linearGradient(
                                                 colors = listOf(
                                                     MaterialTheme.colorScheme.primary,
                                                     MaterialTheme.colorScheme.secondary
                                                 )
                                             )
                                         )
                                 )
                                 Text(
                                     text = shape.displayName,
                                     style = MaterialTheme.typography.bodyLarge,
                                     fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                     color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                                     modifier = Modifier.weight(1f)
                                 )
                                 if (isSelected) {
                                     Icon(
                                         imageVector = Icons.Default.Check,
                                         contentDescription = "Selected",
                                         tint = MaterialTheme.colorScheme.secondary,
                                         modifier = Modifier.size(24.dp)
                                     )
                                 }
                             }
                         }
                     }
                 }
             },
             confirmButton = {
                 TextButton(onClick = { showShapeDialog = false }) {
                     Text("Close")
                 }
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
