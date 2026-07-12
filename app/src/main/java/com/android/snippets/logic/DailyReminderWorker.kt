package com.android.snippets.logic

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.ln.android.snippets.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val NOTIFICATION_ID = 9999
    }

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("snippets_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("notification_reminder_enabled", false)

        if (!enabled) {
            return Result.success()
        }

        // Post notification
        postReminderNotification()

        // Reschedule for next day
        rescheduleNext(prefs)

        return Result.success()
    }

    private fun postReminderNotification() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, com.android.snippets.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, MemoryWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Review your memories")
            .setContentText("Take a look at your photos and add new snippets today!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun rescheduleNext(prefs: android.content.SharedPreferences) {
        val hour = prefs.getInt("notification_reminder_hour", 9)
        val minute = prefs.getInt("notification_reminder_minute", 0)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Force it to be in the future (next day)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val delay = calendar.timeInMillis - System.currentTimeMillis()

        val workRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder_work")
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "daily_reminder_work",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
