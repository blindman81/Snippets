package com.android.snippets.logic

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.graphics.shapes.toPath
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import coil.imageLoader
import coil.request.ImageRequest
import com.android.snippets.model.Photo
import com.android.snippets.ui.shapes.CookiePolygon
import com.android.snippets.ui.util.LocationUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ln.android.snippets.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
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
            clearPostedNotificationState(context, photoId)
            val remaining = postedIds(context)
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

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, com.android.snippets.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return ForegroundInfo(SUMMARY_NOTIFICATION_ID, summaryNotification(pendingIntent))
    }

    override suspend fun doWork(): Result {
        val photoId = inputData.getString(INPUT_PHOTO_ID).orEmpty()
        if (photoId.isBlank()) return Result.success()

        val prefs = applicationContext.getSharedPreferences("snippets_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("notification_reminder_enabled", false)) {
            return Result.success()
        }

        if (!canPostNotifications(applicationContext)) return Result.failure()

        val notificationType = inputData.getString(INPUT_NOTIFICATION_TYPE) ?: TYPE_NEW
        postNotification(photoId, notificationType)
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

        val photo = loadPhoto(photoId)
        val (title, text) = buildNotificationMetadata(photo, notificationType)
        val rawBitmap = photo?.let { loadPhotoBitmap(it) }
        val memoryCardBitmap = rawBitmap?.let { createMemoryCardBitmap(it) }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            .setContentIntent(pendingIntent)

        if (memoryCardBitmap != null) {
            val bigPictureStyle = NotificationCompat.BigPictureStyle()
                .bigPicture(memoryCardBitmap)
                .setBigContentTitle(title)
                .setSummaryText(text)

            notificationBuilder
                .setLargeIcon(rawBitmap)
                .setStyle(bigPictureStyle)
        } else {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        }

        val notification = notificationBuilder.build()

        notificationManager.notify(notificationId(photoId), notification)
        notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification(pendingIntent))
        markNotificationPosted(applicationContext, photoId)
    }

    private fun loadPhoto(photoId: String): Photo? {
        return try {
            val file = File(applicationContext.filesDir, "photos_v2.json")
            if (!file.exists()) return null
            val json = file.readText()
            val type = object : TypeToken<List<Photo>>() {}.type
            val photos: List<Photo> = Gson().fromJson(json, type) ?: emptyList()
            photos.firstOrNull { it.id == photoId }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
            (result.drawable as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Renders the memory image matching the exact Memory Screen design:
     * - Full blurred background with a 50% dark tint overlay
     * - Centered 12-sided Cookie shape (CookiePolygon)
     * - Crisp white border around the cookie
     */
    private fun createMemoryCardBitmap(photoBitmap: Bitmap): Bitmap {
        val targetWidth = 1024
        val targetHeight = 576 // 16:9 standard notification banner ratio

        return try {
            val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            // 1. Draw blurred background with 50% dark tint
            val blurredBg = createBlurredBackground(photoBitmap, targetWidth, targetHeight)
            canvas.drawBitmap(blurredBg, 0f, 0f, null)
            blurredBg.recycle()

            val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                alpha = (255 * 0.5f).toInt()
            }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), dimPaint)

            // 2. Calculate dimensions for 12-sided Cookie Shape
            val cookieDiameter = targetHeight * 0.88f
            val cx = targetWidth / 2f
            val cy = targetHeight / 2f
            val radius = cookieDiameter / 2f

            val path = CookiePolygon.toPath()
            val bounds = RectF()
            path.computeBounds(bounds, true)
            val matrix = Matrix().apply {
                postTranslate(-bounds.left, -bounds.top)
                val scaleX = if (bounds.width() > 0f) cookieDiameter / bounds.width() else 1f
                val scaleY = if (bounds.height() > 0f) cookieDiameter / bounds.height() else 1f
                postScale(scaleX, scaleY)
                postTranslate(cx - radius, cy - radius)
            }
            path.transform(matrix)

            // 3. Draw photo clipped to the 12-sided Cookie Shape
            canvas.save()
            canvas.clipPath(path)
            val srcW = photoBitmap.width.toFloat()
            val srcH = photoBitmap.height.toFloat()
            val imgScale = cookieDiameter / minOf(srcW, srcH)
            val scaledW = srcW * imgScale
            val scaledH = srcH * imgScale
            val imgMatrix = Matrix().apply {
                postScale(imgScale, imgScale)
                postTranslate(cx - scaledW * 0.5f, cy - scaledH * 0.5f)
            }
            canvas.drawBitmap(photoBitmap, imgMatrix, null)
            canvas.restore()

            // 4. Draw 4dp equivalent white border around the Cookie Shape
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 10f
                color = Color.WHITE
            }
            canvas.drawPath(path, borderPaint)

            output
        } catch (e: Exception) {
            e.printStackTrace()
            photoBitmap
        }
    }

    private fun createBlurredBackground(src: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val srcWidth = src.width.toFloat()
        val srcHeight = src.height.toFloat()
        val scaleX = targetWidth / srcWidth
        val scaleY = targetHeight / srcHeight
        val scale = maxOf(scaleX, scaleY)
        val scaledW = (srcWidth * scale).toInt().coerceAtLeast(1)
        val scaledH = (srcHeight * scale).toInt().coerceAtLeast(1)

        val cropped = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cropped)
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate((targetWidth - scaledW) * 0.5f, (targetHeight - scaledH) * 0.5f)
        }
        canvas.drawBitmap(src, matrix, null)

        val tinyW = (targetWidth * 0.08f).toInt().coerceAtLeast(8)
        val tinyH = (targetHeight * 0.08f).toInt().coerceAtLeast(8)
        val tiny = Bitmap.createScaledBitmap(cropped, tinyW, tinyH, true)
        val blurredTiny = boxBlur(tiny, 4)
        val blurred = Bitmap.createScaledBitmap(blurredTiny, targetWidth, targetHeight, true)

        cropped.recycle()
        tiny.recycle()
        blurredTiny.recycle()
        return blurred
    }

    private fun boxBlur(src: Bitmap, range: Int): Bitmap {
        val width = src.width
        val height = src.height
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        val size = range * 2 + 1
        val newPixels = IntArray(width * height)

        // Horizontal pass
        for (y in 0 until height) {
            val offset = y * width
            var rSum = 0
            var gSum = 0
            var bSum = 0

            for (i in -range..range) {
                val x = i.coerceIn(0, width - 1)
                val pixel = pixels[offset + x]
                rSum += Color.red(pixel)
                gSum += Color.green(pixel)
                bSum += Color.blue(pixel)
            }

            for (x in 0 until width) {
                newPixels[offset + x] = Color.rgb(rSum / size, gSum / size, bSum / size)
                val prevX = (x - range).coerceIn(0, width - 1)
                val nextX = (x + range + 1).coerceIn(0, width - 1)
                val prevPixel = pixels[offset + prevX]
                val nextPixel = pixels[offset + nextX]
                rSum += Color.red(nextPixel) - Color.red(prevPixel)
                gSum += Color.green(nextPixel) - Color.green(prevPixel)
                bSum += Color.blue(nextPixel) - Color.blue(prevPixel)
            }
        }

        // Vertical pass
        val finalPixels = IntArray(width * height)
        for (x in 0 until width) {
            var rSum = 0
            var gSum = 0
            var bSum = 0

            for (i in -range..range) {
                val y = i.coerceIn(0, height - 1)
                val pixel = newPixels[y * width + x]
                rSum += Color.red(pixel)
                gSum += Color.green(pixel)
                bSum += Color.blue(pixel)
            }

            for (y in 0 until height) {
                finalPixels[y * width + x] = Color.rgb(rSum / size, gSum / size, bSum / size)
                val prevY = (y - range).coerceIn(0, height - 1)
                val nextY = (y + range + 1).coerceIn(0, height - 1)
                val prevPixel = newPixels[prevY * width + x]
                val nextPixel = newPixels[nextY * width + x]
                rSum += Color.red(nextPixel) - Color.red(prevPixel)
                gSum += Color.green(nextPixel) - Color.green(prevPixel)
                bSum += Color.blue(nextPixel) - Color.blue(prevPixel)
            }
        }

        return Bitmap.createBitmap(finalPixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun buildNotificationMetadata(photo: Photo?, notificationType: String): Pair<String, String> {
        if (photo == null) {
            return notificationFallbackText(notificationType)
        }

        // 1. Day and Date
        val dayAndDate = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(photo.date))

        // 2. Favorite badge
        val favoriteStr = if (photo.isFavorite) "❤️ Favorite" else null

        val title = listOfNotNull(dayAndDate, favoriteStr).joinToString(" • ")

        // 3. Star rating
        val ratingStr = if (photo.rating > 0) "★ ${photo.rating}" else null

        // 4. Shortened location name
        val locationStr = LocationUtils.getLocationFromExif(applicationContext, photo)

        val metaParts = listOfNotNull(ratingStr, locationStr)
        val text = if (metaParts.isNotEmpty()) {
            metaParts.joinToString(" • ")
        } else {
            notificationFallbackText(notificationType).second
        }

        return title to text
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

    private fun notificationFallbackText(notificationType: String): Pair<String, String> {
        return when (notificationType) {
            TYPE_RESURFACED -> "A memory resurfaced!" to "A past memory is ready to revisit"
            TYPE_UPDATED -> "A memory was refreshed!" to "A viewed memory has new snippets waiting"
            else -> "A new memory!" to "There's a new memory with snippets for you to see"
        }
    }
}
