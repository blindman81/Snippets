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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.android.snippets.model.Photo
import com.ln.android.snippets.R
import java.io.File
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

        if (!MemoryWorker.canPostNotifications(applicationContext)) {
            rescheduleNext(prefs)
            return Result.success()
        }

        // Always post the summarised notification at the scheduled time.
        postSummaryNotification()

        // Reschedule for next day at the same time.
        rescheduleNext(prefs)

        return Result.success()
    }

    private fun pendingMemoriesCount(): Int {
        return try {
            val file = File(applicationContext.filesDir, "photos_v2.json")
            if (!file.exists()) return 0
            val json = file.readText()
            val type = object : TypeToken<List<Photo>>() {}.type
            val photos: List<Photo> = Gson().fromJson(json, type) ?: emptyList()
            photos.count { photo ->
                photo.isLibraryUpload &&
                    photo.snippets.isNotEmpty() &&
                    (!photo.isViewed || photo.snippetsAddedTime > photo.lastViewedTime)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    private fun postSummaryNotification() {
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

        val count = pendingMemoriesCount()
        val (title, text) = when {
            count == 1 -> "1 memory has surfaced" to "A memory is waiting for you to revisit"
            count > 1  -> "$count memories have surfaced" to "Tap to explore your surfaced memories"
            else       -> "Take a look at your memories" to "Open the app to add new snippets today"
        }

        val notification = NotificationCompat.Builder(applicationContext, MemoryWorker.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
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
        // Always push to the next occurrence (this worker already fired for today).
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
