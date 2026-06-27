package com.android.snippets.logic

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ln.android.snippets.R

import androidx.work.ForegroundInfo

class MemoryWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        const val CHANNEL_ID = "new_memory_channel"
        const val GROUP_KEY = "com.android.snippets.MEMORIES"
        const val SUMMARY_NOTIFICATION_ID = 0
        const val INPUT_PHOTO_ID = "photo_id"
        const val INPUT_NOTIFICATION_TYPE = "notification_type"
        private const val PREFS = "memory_notification_state"
        private const val KEY_POSTED_IDS = "posted_ids"

        const val TYPE_NEW = "new"
        const val TYPE_UPDATED = "updated"
        const val TYPE_RESURFACED = "resurfaced"

        fun canPostNotifications(context: Context): Boolean {
            val hasRuntimePermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            if (!hasRuntimePermission || !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                return false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
                if (channel?.importance == NotificationManager.IMPORTANCE_NONE) return false
            }

            return true
        }

        fun cancelPostedNotification(context: Context, photoId: String) {
            if (photoId.isBlank()) return
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId(photoId))
            val remaining = postedIds(context) - photoId
            if (remaining.isEmpty()) {
                notificationManager.cancel(SUMMARY_NOTIFICATION_ID)
            }
        }

        fun wasNotificationPosted(context: Context, photoId: String): Boolean {
            return postedIds(context).contains(photoId)
        }

        fun clearPostedNotificationState(context: Context, photoId: String) {
            if (photoId.isBlank()) return
            val updated = postedIds(context) - photoId
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_POSTED_IDS, updated)
                .apply()
        }

        private fun notificationId(photoId: String): Int = photoId.hashCode()

        private fun markNotificationPosted(context: Context, photoId: String) {
            val updated = postedIds(context) + photoId
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_POSTED_IDS, updated)
                .apply()
        }

        private fun postedIds(context: Context): Set<String> {
            val rawSet = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getStringSet(KEY_POSTED_IDS, null)
            return if (rawSet != null) HashSet(rawSet) else emptySet()
        }
    }

    override fun getForegroundInfo(): ForegroundInfo {
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, com.android.snippets.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return ForegroundInfo(SUMMARY_NOTIFICATION_ID, summaryNotification(pendingIntent))
    }

    override fun doWork(): Result {
        val photoId = inputData.getString(INPUT_PHOTO_ID).orEmpty()
        if (photoId.isBlank()) return Result.success()
        if (!canPostNotifications(applicationContext)) return Result.failure()

        val notificationType = inputData.getString(INPUT_NOTIFICATION_TYPE) ?: TYPE_NEW
        postNotification(photoId, notificationType)
        return Result.success()
    }

    private fun postNotification(photoId: String, notificationType: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId(photoId),
            openMemoryIntent(photoId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val (title, text) = notificationText(notificationType)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId(photoId), notification)
        notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification(pendingIntent))
        markNotificationPosted(applicationContext, photoId)
    }

    private fun summaryNotification(pendingIntent: PendingIntent) =
        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Memories waiting")
            .setContentText("You have memories waiting to be viewed")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setContentIntent(pendingIntent)
            .build()

    private fun openMemoryIntent(photoId: String): Intent {
        return Intent(applicationContext, com.android.snippets.MainActivity::class.java).apply {
            action = "com.android.snippets.OPEN_MEMORY.$photoId"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_memory", true)
            putExtra("photo_id", photoId)
        }
    }

    private fun notificationText(notificationType: String): Pair<String, String> {
        return when (notificationType) {
            TYPE_RESURFACED -> "A memory resurfaced!" to "A past memory is ready to revisit"
            TYPE_UPDATED -> "A memory was refreshed!" to "A viewed memory has new snippets waiting"
            else -> "A new memory!" to "There's a new memory with snippets for you to see"
        }
    }
}
