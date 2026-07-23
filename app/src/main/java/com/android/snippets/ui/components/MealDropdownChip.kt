package com.android.snippets.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.android.snippets.MainActivity
import com.android.snippets.logic.DailyReminderWorker
import com.android.snippets.logic.MemoryWorker
import com.ln.android.snippets.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Material 3 Meal Selection Filter & Reminder Chip with Dropdown Menu.
 *
 * Specifications:
 * 1. M3 Chip Specs:
 *    - Unfilled dropdown icon when closed: R.drawable.ic_arrow_dropdown
 *    - Filled dropdown icon when expanded: R.drawable.ic_arrow_dropdown_filled
 * 2. Dropdown Menu M3 Specs:
 *    - Options: "Breakfast", "Lunch", "Dinner", "Snacks"
 *    - Shows checkmark icon next to the selected option
 * 3. Date & Time Picker Flow:
 *    - Selecting an option opens a DatePicker, then a TimePicker
 *    - Schedules/sends notification: "You have food to eat today"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDropdownChip(
    modifier: Modifier = Modifier,
    selectedMeal: String? = null,
    onMealSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current

    var isExpanded by remember { mutableStateOf(false) }
    var currentSelectedMeal by remember(selectedMeal) { mutableStateOf(selectedMeal) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingMealType by remember { mutableStateOf("") }
    var chosenDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val options = remember { listOf("Breakfast", "Lunch", "Dinner", "Snacks") }

    Box(modifier = modifier) {
        // Official Material 3 Filter Chip
        FilterChip(
            selected = currentSelectedMeal != null,
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                isExpanded = !isExpanded
            },
            label = {
                Text(
                    text = currentSelectedMeal ?: "Select Meal",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (currentSelectedMeal != null) FontWeight.Bold else FontWeight.Medium
                )
            },
            leadingIcon = if (currentSelectedMeal != null) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null,
            trailingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (isExpanded) R.drawable.ic_arrow_dropdown_filled else R.drawable.ic_arrow_dropdown
                    ),
                    contentDescription = if (isExpanded) "Collapse meal menu" else "Expand meal menu",
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(8.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = currentSelectedMeal != null,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                selectedBorderColor = Color.Transparent
            )
        )

        // Official M3 Dropdown Menu
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            options.forEach { option ->
                val isOptionSelected = currentSelectedMeal == option
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isOptionSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isOptionSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected option",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        {
                            Spacer(modifier = Modifier.size(20.dp))
                        }
                    },
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        currentSelectedMeal = option
                        onMealSelected(option)
                        isExpanded = false
                        pendingMealType = option
                        showDatePicker = true
                    }
                )
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        chosenDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        showDatePicker = false
                        showTimePicker = true
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Material 3 Time Picker Dialog
    if (showTimePicker) {
        val currentTime = remember { Calendar.getInstance() }
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentTime.get(Calendar.MINUTE),
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        showTimePicker = false

                        // Post / schedule notification for selected meal
                        sendMealReminderNotification(
                            context = context,
                            mealType = pendingMealType,
                            dateMillis = chosenDateMillis,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text(
                    text = "Select Time for $pendingMealType",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

/**
 * Triggers notification "You have food to eat today" for the selected meal date and time.
 */
fun sendMealReminderNotification(
    context: Context,
    mealType: String,
    dateMillis: Long,
    hour: Int,
    minute: Int
) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Ensure Notification Channel exists
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            MemoryWorker.CHANNEL_ID,
            "Meal & Memory Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for meal schedules and surfaced memories"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        1001,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_eatlist", true)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notificationTitle = if (mealType.isNotBlank()) "$mealType Reminder" else "Meal Reminder"
    val notificationText = "You have food to eat today"

    val notification = NotificationCompat.Builder(context, MemoryWorker.CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(notificationTitle)
        .setContentText(notificationText)
        .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    // Immediately post notification for prompt feedback & test verification
    notificationManager.notify(DailyReminderWorker.NOTIFICATION_ID + mealType.hashCode(), notification)

    // Also schedule via WorkManager if time is in the future
    val scheduledCal = Calendar.getInstance().apply {
        timeInMillis = dateMillis
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val now = System.currentTimeMillis()
    val delayMs = scheduledCal.timeInMillis - now
    if (delayMs > 0) {
        val workData = workDataOf(
            "meal_type" to mealType
        )

        val workRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInputData(workData)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag("meal_reminder_$mealType")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "meal_reminder_$mealType",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
