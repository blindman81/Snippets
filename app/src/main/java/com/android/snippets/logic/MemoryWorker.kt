package com.android.snippets.logic

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.android.snippets.data.preferences.UserPreferencesRepository
import com.android.snippets.data.toPhoto
import com.android.snippets.data.toEntity
import com.android.snippets.model.Photo
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import com.ln.android.snippets.R
import java.io.File

/**
 * Handles memory notifications with rich metadata (day & date, favorite status,
 * star rating, location) and displays the memory image inside a 9-sided cookie polygon
 * with a dimmed, blurred background in BigPictureStyle.
 */
class MemoryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        const val CHANNEL_ID = "new_memory_channel"
        const val GROUP_KEY = "com.android.snippets.MEMORIES"
        const val SUMMARY_NOTIFICATION_ID = 0
        const val FOREGROUND_NOTIFICATION_ID = 1001
        const val INPUT_PHOTO_ID = "photo_id"
        const val INPUT_NOTIFICATION_TYPE = "notification_type"
        const val INPUT_IS_CYCLE_TRIGGER = "is_cycle_trigger"
        private val jsonSerializer = Json { ignoreUnknownKeys = true }

        const val TYPE_NEW = "new"
        const val TYPE_UPDATED = "updated"
        const val TYPE_RESURFACED = "resurfaced"

        @Volatile
        var isInMemoryScreen: Boolean = false

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
                var channel = notificationManager.getNotificationChannel(CHANNEL_ID)
                if (channel == null) {
                    val memoryChannel = NotificationChannel(
                        CHANNEL_ID,
                        "New memory",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Notifications when a new memory is ready"
                        enableVibration(true)
                        setShowBadge(true)
                    }
                    notificationManager.createNotificationChannel(memoryChannel)
                    channel = notificationManager.getNotificationChannel(CHANNEL_ID)
                }
                if (channel?.importance == NotificationManager.IMPORTANCE_NONE) return false
            }

            return true
        }

        fun cancelSummaryNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(SUMMARY_NOTIFICATION_ID)
        }

        fun cancelPostedNotification(context: Context, photoId: String) {
            if (photoId.isBlank()) return
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId(photoId))
            clearPostedNotificationState(context, photoId)
            cancelSummaryNotification(context)
        }

        fun updateSummaryNotification(context: Context, currentPostedIds: Set<String> = postedIds(context)) {
            cancelSummaryNotification(context)
        }

        fun wasNotificationPosted(context: Context, photoId: String): Boolean {
            val repo = UserPreferencesRepository.getInstance(context)
            return kotlinx.coroutines.runBlocking { repo.getMemoryPostedIds().contains(photoId) }
        }

        @Synchronized
        fun clearPostedNotificationState(context: Context, photoId: String): Set<String> {
            if (photoId.isBlank()) return postedIds(context)
            val repo = UserPreferencesRepository.getInstance(context)
            return kotlinx.coroutines.runBlocking { repo.removeMemoryPostedId(photoId) }
        }

        internal fun notificationId(photoId: String): Int = photoId.hashCode()

        suspend fun markMemoryAsViewed(context: Context, photoId: String) {
            if (photoId.isBlank()) return
            try {
                val db = com.android.snippets.data.SnippetsDatabase.getInstance(context)
                val entity = db.photoDao().getPhotoById(photoId)
                val now = System.currentTimeMillis()
                if (entity != null) {
                    val safeLastViewed = maxOf(now, entity.snippetsAddedTime)
                    if (!entity.isViewed || entity.lastViewedTime < safeLastViewed) {
                        val updated = entity.copy(isViewed = true, lastViewedTime = safeLastViewed)
                        db.photoDao().updatePhoto(updated)
                    }
                } else {
                    val photos = loadAllPhotos(context)
                    val photo = photos.firstOrNull { it.id == photoId }
                    if (photo != null) {
                        val safeLastViewed = maxOf(now, photo.snippetsAddedTime)
                        val updated = photo.copy(isViewed = true, lastViewedTime = safeLastViewed)
                        saveAllPhotos(context, photos.map { if (it.id == photoId) updated else it })
                    }
                }
                try {
                    android.app.backup.BackupManager(context).dataChanged()
                } catch (_: Exception) {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cancelPostedNotification(context, photoId)
        }

        @Synchronized
        fun markNotificationPosted(context: Context, photoId: String): Set<String> {
            val repo = UserPreferencesRepository.getInstance(context)
            return kotlinx.coroutines.runBlocking { repo.addMemoryPostedId(photoId) }
        }

        @Synchronized
        fun postedIds(context: Context): Set<String> {
            val repo = UserPreferencesRepository.getInstance(context)
            return kotlinx.coroutines.runBlocking { repo.getMemoryPostedIds() }
        }

        fun loadAllPhotos(context: Context): List<Photo> {
            return try {
                val db = com.android.snippets.data.SnippetsDatabase.getInstance(context)
                val roomPhotos = kotlinx.coroutines.runBlocking {
                    db.photoDao().getAllPhotosSync().map { it.toPhoto() }
                }
                if (roomPhotos.isNotEmpty()) {
                    return roomPhotos
                }
                val file = File(context.filesDir, "photos_v2.json")
                if (!file.exists()) return emptyList()
                val json = file.readText()
                jsonSerializer.decodeFromString<List<Photo>>(json)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

        fun saveAllPhotos(context: Context, photos: List<Photo>) {
            try {
                val db = com.android.snippets.data.SnippetsDatabase.getInstance(context)
                kotlinx.coroutines.runBlocking {
                    db.photoDao().insertPhotos(photos.map { it.toEntity() })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun loadPhoto(context: Context, photoId: String): Photo? {
            return loadAllPhotos(context).firstOrNull { it.id == photoId }
        }

        fun buildNotificationMetadata(
            context: Context? = null,
            photo: Photo?,
            notificationType: String
        ): Pair<String, String?> {
            val photoName = photo?.name?.trim()?.takeIf { it.isNotBlank() }
            if (photoName != null) {
                return photoName to null
            }
            return notificationFallbackText(notificationType)
        }

        fun buildSummaryNotification(
            context: Context,
            postedPhotoIds: Set<String>
        ): Notification {
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, com.android.snippets.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("open_memory", true)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val count = postedPhotoIds.size.coerceAtLeast(1)
            val title = if (count == 1) "1 memory waiting" else "$count memories waiting"
            val text = if (count == 1) {
                "You have a memory waiting to be viewed"
            } else {
                "You have $count memories waiting to be viewed"
            }

            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(title)
                .setSummaryText("Memories")

            val allPhotos = loadAllPhotos(context)
            val postedPhotos = allPhotos.filter { postedPhotoIds.contains(it.id) }
            val displayedPhotos = if (postedPhotos.isNotEmpty()) postedPhotos else allPhotos.take(count)

            for (photo in displayedPhotos.take(5)) {
                val (itemTitle, _) = buildNotificationMetadata(context, photo, TYPE_NEW)
                inboxStyle.addLine(itemTitle)
            }

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(inboxStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                .setContentIntent(pendingIntent)
                .build()
        }

        fun notificationFallbackText(notificationType: String): Pair<String, String> {
            return when (notificationType) {
                TYPE_RESURFACED -> "Memory resurfaced" to "A past memory is ready to revisit"
                TYPE_UPDATED -> "Memory refreshed" to "Relive this memory"
                else -> "New memory" to "Relive this memory"
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Preparing memory")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        return ForegroundInfo(FOREGROUND_NOTIFICATION_ID, notification)
    }

    override suspend fun doWork(): Result {
        val isCycleTrigger = inputData.getBoolean(INPUT_IS_CYCLE_TRIGGER, false)
        val photoId = inputData.getString(INPUT_PHOTO_ID).orEmpty()

        if (isCycleTrigger || photoId.isBlank()) {
            return runCycleWork()
        }

        if (!canPostNotifications(applicationContext)) return Result.failure()

        val notificationType = inputData.getString(INPUT_NOTIFICATION_TYPE) ?: TYPE_NEW
        postNotification(photoId, notificationType)
        return Result.success()
    }

    private suspend fun runCycleWork(): Result {
        val now = System.currentTimeMillis()
        val photos = loadAllPhotos(applicationContext)
        val reconciled = MemoryManager.reconcileSurfacedMemories(photos, now)
        val finalPhotos = if (reconciled != null) {
            saveAllPhotos(applicationContext, reconciled.first)
            reconciled.first
        } else {
            photos
        }

        if (canPostNotifications(applicationContext) && !isInMemoryScreen) {
            val curated = MemoryManager.getCuratedMemories(finalPhotos, now).take(MemoryManager.MEMORIES_PER_CYCLE)
            val unviewed = curated.filter { !it.isViewed }
            for (photo in unviewed) {
                clearPostedNotificationState(applicationContext, photo.id)
                postNotification(photo.id, TYPE_NEW)
            }
            RecapManager.checkAndPostRecapNotification(applicationContext, finalPhotos)
        }

        MemoryManager.scheduleNextCycleWork(applicationContext)
        return Result.success()
    }

    private suspend fun postNotification(photoId: String, notificationType: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId(photoId),
            openMemoryIntent(photoId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val photo = loadPhoto(applicationContext, photoId)
        val rawBitmap = photo?.let { loadPhotoBitmap(it) }
        val isDark = try {
            val repo = UserPreferencesRepository.getInstance(applicationContext)
            val themePref = repo.themePreferenceFlow.first()
            val isSystemDark = (applicationContext.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            when (themePref.uppercase()) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemDark
            }
        } catch (_: Exception) {
            MemoryNotificationRenderer.isDarkTheme(applicationContext)
        }
        val memoryCardBitmap = if (photo != null && rawBitmap != null) {
            createMemoryCardBitmap(applicationContext, photo, rawBitmap, isDark)
        } else {
            rawBitmap?.let { createMemoryCardBitmap(it) }
        }

        val (title, text) = buildNotificationMetadata(applicationContext, photo, notificationType)

        val markViewedIntent = Intent(applicationContext, MemoryNotificationActionReceiver::class.java).apply {
            action = "${MemoryNotificationActionReceiver.ACTION_MARK_AS_VIEWED}.$photoId"
            data = Uri.parse("snippets://memory/mark_as_viewed/$photoId")
            putExtra(MemoryNotificationActionReceiver.EXTRA_PHOTO_ID, photoId)
        }
        val markViewedPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationId(photoId),
            markViewedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val markViewedAction = NotificationCompat.Action.Builder(
            0,
            applicationContext.getString(R.string.mark_as_viewed),
            markViewedPendingIntent
        )
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
            .setShowsUserInterface(false)
            .build()

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(rawBitmap)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)

        if (!text.isNullOrBlank()) {
            notificationBuilder.setContentText(text)
        }
        notificationBuilder.addAction(markViewedAction)

        if (memoryCardBitmap != null) {
            val bigPictureStyle = NotificationCompat.BigPictureStyle()
                .bigPicture(memoryCardBitmap)
                .bigLargeIcon(rawBitmap)
                .setBigContentTitle(title)

            if (!text.isNullOrBlank()) {
                bigPictureStyle.setSummaryText(text)
            }

            notificationBuilder.setStyle(bigPictureStyle)
        } else if (!text.isNullOrBlank()) {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        }

        val notification = notificationBuilder.build()

        markNotificationPosted(applicationContext, photoId)

        notificationManager.notify(notificationId(photoId), notification)
        cancelSummaryNotification(applicationContext)
    }

    private suspend fun loadPhotoBitmap(photo: Photo): Bitmap? {
        return try {
            val uri = photo.uri
            if (uri == android.net.Uri.EMPTY) return null
            val request = ImageRequest.Builder(applicationContext)
                .data(uri)
                .allowHardware(false)
                .size(1024, 1024)
                .build()
            val result = applicationContext.imageLoader.execute(request)
            result.image?.toBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    internal fun createMemoryCardBitmap(
        context: Context,
        photo: Photo,
        photoBitmap: Bitmap,
        isDark: Boolean = MemoryNotificationRenderer.isDarkTheme(context)
    ): Bitmap =
        MemoryNotificationRenderer.createMemoryCardBitmap(context, photo, photoBitmap, isDark)

    internal fun createMemoryCardBitmap(photoBitmap: Bitmap): Bitmap =
        MemoryNotificationRenderer.createMemoryCardBitmap(photoBitmap)


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
}