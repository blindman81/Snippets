package com.android.snippets.logic

import android.Manifest
import android.app.Notification
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
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
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
import com.android.snippets.ui.theme.getSnippetBackgroundColorInt
import com.android.snippets.ui.theme.getSnippetTextColorInt
import com.android.snippets.ui.util.LocationUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ln.android.snippets.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

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
        const val NOTIFICATION_DATE_FORMAT = "EEE, d MMM"
        private const val PREFS = "memory_notification_state"
        private const val KEY_POSTED_IDS = "posted_ids"

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
                val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
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
            return postedIds(context).contains(photoId)
        }

        @Synchronized
        fun clearPostedNotificationState(context: Context, photoId: String): Set<String> {
            if (photoId.isBlank()) return postedIds(context)
            val updated = postedIds(context) - photoId
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_POSTED_IDS, updated)
                .commit()
            return updated
        }

        private fun notificationId(photoId: String): Int = photoId.hashCode()

        @Synchronized
        fun markNotificationPosted(context: Context, photoId: String): Set<String> {
            val updated = postedIds(context) + photoId
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_POSTED_IDS, updated)
                .commit()
            return updated
        }

        @Synchronized
        fun postedIds(context: Context): Set<String> {
            val rawSet = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getStringSet(KEY_POSTED_IDS, null)
            return if (rawSet != null) HashSet(rawSet) else emptySet()
        }

        fun loadAllPhotos(context: Context): List<Photo> {
            return try {
                val file = File(context.filesDir, "photos_v2.json")
                if (!file.exists()) return emptyList()
                val json = file.readText()
                val type = object : TypeToken<List<Photo>>() {}.type
                Gson().fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

        fun loadPhoto(context: Context, photoId: String): Photo? {
            return loadAllPhotos(context).firstOrNull { it.id == photoId }
        }

        fun buildNotificationMetadata(
            context: Context? = null,
            photo: Photo?,
            notificationType: String
        ): Pair<String, String> {
            if (photo == null) {
                return notificationFallbackText(notificationType)
            }

            // Notification date format: "Wed, 18 Oct"
            val dayAndDate = SimpleDateFormat(NOTIFICATION_DATE_FORMAT, Locale.getDefault())
                .format(Date(photo.date))

            // Photo name in notification title (fallback: day and date)
            val nameOrDate = photo.name?.takeIf { it.isNotBlank() } ?: dayAndDate

            // plus location
            val locationStr = LocationUtils.getLocationFromExif(context, photo)

            val title = listOfNotNull(nameOrDate, locationStr).joinToString(" • ")

            // Metadata: date in body when custom name is used, favorite status, star rating
            val dateInBody = if (!photo.name.isNullOrBlank()) dayAndDate else null
            val favoriteStr = if (photo.isFavorite) "❤️ Favorite" else null
            val ratingStr = if (photo.rating > 0) "★ ${photo.rating}" else null

            val metaParts = listOfNotNull(dateInBody, favoriteStr, ratingStr)
            val text = if (metaParts.isNotEmpty()) {
                metaParts.joinToString(" • ")
            } else {
                notificationFallbackText(notificationType).second
            }

            return title to text
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

        private val cachedFonts = mutableMapOf<String, Typeface>()

        private fun getCustomTypeface(context: Context, variationSettings: String): Typeface? {
            val cached = cachedFonts[variationSettings]
            if (cached != null) return cached

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val cacheFile = File(context.cacheDir, "google_sans_flex_temp.ttf")
                    if (!cacheFile.exists()) {
                        context.resources.openRawResource(R.font.google_sans_flex).use { input ->
                            FileOutputStream(cacheFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    val typeface = Typeface.Builder(cacheFile)
                        .setFontVariationSettings(variationSettings)
                        .build()
                    if (typeface != null) {
                        cachedFonts[variationSettings] = typeface
                        return typeface
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return try {
                androidx.core.content.res.ResourcesCompat.getFont(context, R.font.google_sans_flex)
            } catch (e: Exception) {
                null
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
        val photoId = inputData.getString(INPUT_PHOTO_ID).orEmpty()
        if (photoId.isBlank()) return Result.success()

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

        val photo = loadPhoto(applicationContext, photoId)
        val (title, text) = buildNotificationMetadata(applicationContext, photo, notificationType)
        val rawBitmap = photo?.let { loadPhotoBitmap(it) }
        val memoryCardBitmap = if (photo != null && rawBitmap != null) {
            createMemoryCardBitmap(applicationContext, photo, rawBitmap)
        } else {
            rawBitmap?.let { createMemoryCardBitmap(it) }
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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
            (result.drawable as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Renders the memory image preview matching the Memory Screen / MediaSaver design:
     * - Full blurred background with a 50% dark tint overlay
     * - Top Header: Star rating (top center), Calendar & Date (center pill),
     *   Photo Name (left pill), Location (right pill)
     * - Centered 9-sided Cookie shape (CookiePolygon) with crisp white border
     * - Snippets cloud placed directly beneath the cookie shape (cutting vertical empty space)
     */
    private fun createMemoryCardBitmap(context: Context, photo: Photo, photoBitmap: Bitmap): Bitmap {
        val targetWidth = 1024
        val targetHeight = 576 // 16:9 standard notification banner ratio

        return try {
            val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            // 1. Draw blurred background with 50% dark tint overlay
            val blurredBg = createBlurredBackground(photoBitmap, targetWidth, targetHeight)
            canvas.drawBitmap(blurredBg, 0f, 0f, null)
            blurredBg.recycle()

            val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                alpha = (255 * 0.5f).toInt()
            }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), dimPaint)

            // 2. Draw Top Header: Rating star (top center), Calendar & Date (center pill),
            //    Photo Name (left pill), Location (right pill)
            val hasRating = photo.rating > 0
            val headerCenterY = if (hasRating) 64f else 38f
            val pillH = 34f
            val pillRadius = pillH / 2f
            val paddingX = 24f

            if (hasRating) {
                val starSize = 30f
                val starLeft = (targetWidth - starSize) / 2f
                val starTop = 10f

                val starDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.ic_star_rating,
                    null
                )
                if (starDrawable != null) {
                    starDrawable.setBounds(starLeft.toInt(), starTop.toInt(), (starLeft + starSize).toInt(), (starTop + starSize).toInt())
                    val tinted = starDrawable.mutate()
                    androidx.core.graphics.drawable.DrawableCompat.setTint(tinted, Color.WHITE)
                    tinted.draw(canvas)

                    val ratingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#1F1F1F")
                        textSize = 12f
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    val textX = starLeft + starSize / 2f
                    val textY = starTop + starSize / 2f + (ratingPaint.textSize / 3f)
                    canvas.drawText(photo.rating.toString(), textX, textY, ratingPaint)
                }
            }

            val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = (255 * 0.25f).toInt()
                style = Paint.Style.FILL
            }
            val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = (255 * 0.4f).toInt()
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }

            // Center Pill: Calendar month icon + Day, Date
            val dateString = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(photo.date)).uppercase()
            val datePaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 240
                textSize = 17f
                typeface = getCustomTypeface(context, "'wght' 600, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val calIconSize = 18f
            val itemSpacing = 6f
            val calHorizontalPadding = 14f
            val dateTextWidth = datePaint.measureText(dateString)
            val centerPillW = calHorizontalPadding * 2 + calIconSize + itemSpacing + dateTextWidth
            val centerPillLeft = (targetWidth - centerPillW) / 2f
            val centerPillTop = headerCenterY - (pillH / 2f)
            val centerPillRect = RectF(centerPillLeft, centerPillTop, centerPillLeft + centerPillW, centerPillTop + pillH)

            canvas.drawRoundRect(centerPillRect, pillRadius, pillRadius, pillBgPaint)
            canvas.drawRoundRect(centerPillRect, pillRadius, pillRadius, pillBorderPaint)

            val calDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, R.drawable.ic_calendar_month, null)
            val calIconX = centerPillLeft + calHorizontalPadding
            val calIconY = headerCenterY - (calIconSize / 2f)
            if (calDrawable != null) {
                calDrawable.setBounds(calIconX.toInt(), calIconY.toInt(), (calIconX + calIconSize).toInt(), (calIconY + calIconSize).toInt())
                val tinted = calDrawable.mutate()
                androidx.core.graphics.drawable.DrawableCompat.setTint(tinted, Color.WHITE)
                tinted.draw(canvas)
            }
            val dateTextY = headerCenterY - ((datePaint.descent() + datePaint.ascent()) / 2f)
            datePaint.textAlign = Paint.Align.LEFT
            canvas.drawText(dateString, calIconX + calIconSize + itemSpacing, dateTextY, datePaint)

            // Left Pill: Label icon + Photo name
            val photoName = photo.name?.takeIf { it.isNotBlank() }
            if (photoName != null) {
                val namePaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    alpha = 240
                    textSize = 17f
                    typeface = getCustomTypeface(context, "'wght' 600, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                val labelIconSize = 18f
                val maxLeftPillWidth = (centerPillLeft - paddingX - 12f).coerceAtLeast(60f)
                val maxTextWidth = (maxLeftPillWidth - calHorizontalPadding * 2 - labelIconSize - itemSpacing).coerceAtLeast(10f)
                val ellipsizedName = android.text.TextUtils.ellipsize(photoName, namePaint, maxTextWidth, android.text.TextUtils.TruncateAt.END).toString()
                val textWidth = namePaint.measureText(ellipsizedName)

                val pillW = calHorizontalPadding * 2 + labelIconSize + itemSpacing + textWidth
                val pillLeft = paddingX
                val pillTop = headerCenterY - (pillH / 2f)
                val pillRect = RectF(pillLeft, pillTop, pillLeft + pillW, pillTop + pillH)

                canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBgPaint)
                canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBorderPaint)

                val labelDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, R.drawable.ic_label, null)
                val iconX = pillLeft + calHorizontalPadding
                val iconY = headerCenterY - (labelIconSize / 2f)
                if (labelDrawable != null) {
                    labelDrawable.setBounds(iconX.toInt(), iconY.toInt(), (iconX + labelIconSize).toInt(), (iconY + labelIconSize).toInt())
                    val tinted = labelDrawable.mutate()
                    androidx.core.graphics.drawable.DrawableCompat.setTint(tinted, Color.WHITE)
                    tinted.draw(canvas)
                }
                val textY = headerCenterY - ((namePaint.descent() + namePaint.ascent()) / 2f)
                namePaint.textAlign = Paint.Align.LEFT
                canvas.drawText(ellipsizedName, iconX + labelIconSize + itemSpacing, textY, namePaint)
            }

            // Right Pill: Location icon + Location
            val locationText = LocationUtils.getLocationFromExif(context, photo) ?: photo.locationName
            if (!locationText.isNullOrBlank()) {
                val locTextPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    alpha = 240
                    textSize = 17f
                    typeface = getCustomTypeface(context, "'wght' 600, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                val locIconSize = 18f
                val maxRightPillWidth = (targetWidth - paddingX - (centerPillLeft + centerPillW + 12f)).coerceAtLeast(60f)
                val maxTextWidth = (maxRightPillWidth - calHorizontalPadding * 2 - locIconSize - itemSpacing).coerceAtLeast(10f)
                val ellipsizedLoc = android.text.TextUtils.ellipsize(locationText, locTextPaint, maxTextWidth, android.text.TextUtils.TruncateAt.END).toString()
                val textWidth = locTextPaint.measureText(ellipsizedLoc)

                val pillW = calHorizontalPadding * 2 + locIconSize + itemSpacing + textWidth
                val pillRight = targetWidth - paddingX
                val pillLeft = pillRight - pillW
                val pillTop = headerCenterY - (pillH / 2f)
                val pillRect = RectF(pillLeft, pillTop, pillRight, pillTop + pillH)

                canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBgPaint)
                canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBorderPaint)

                val locDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, R.drawable.ic_location_on, null)
                val iconX = pillLeft + calHorizontalPadding
                val iconY = headerCenterY - (locIconSize / 2f)
                if (locDrawable != null) {
                    locDrawable.setBounds(iconX.toInt(), iconY.toInt(), (iconX + locIconSize).toInt(), (iconY + locIconSize).toInt())
                    val tinted = locDrawable.mutate()
                    androidx.core.graphics.drawable.DrawableCompat.setTint(tinted, Color.WHITE)
                    tinted.draw(canvas)
                }
                val textY = headerCenterY - ((locTextPaint.descent() + locTextPaint.ascent()) / 2f)
                locTextPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(ellipsizedLoc, iconX + locIconSize + itemSpacing, textY, locTextPaint)
            }

            // 3. Central 9-sided Cookie Shape (with scaled photo and white border)
            val hasSnippets = photo.snippets.isNotEmpty()
            val headerBottom = headerCenterY + pillRadius
            val topMargin = 14f
            val cookieTop = headerBottom + topMargin

            val cookieDiameter = if (hasSnippets) {
                280f
            } else {
                (targetHeight - cookieTop - 24f).coerceAtMost(targetHeight * 0.85f)
            }
            val cx = targetWidth / 2f
            val cy = cookieTop + cookieDiameter / 2f
            val radius = cookieDiameter / 2f
            val cookieBottom = cy + radius

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

            // Draw photo clipped inside cookie
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

            // Draw white border around cookie
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 8f
                color = Color.WHITE
            }
            canvas.drawPath(path, borderPaint)

            // 4. Draw Snippets directly beneath the cookie shape (cutting vertical empty space)
            if (hasSnippets) {
                val snippetGap = 14f // Tight gap right below cookie shape
                val snippetAreaTop = cookieBottom + snippetGap
                val snippetPillH = 36f
                val snippetPillRadius = snippetPillH / 2f
                val snippetTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textAlign = Paint.Align.CENTER
                    textSize = 17f
                    typeface = getCustomTypeface(context, "'wght' 700, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }

                data class NotificationSnippetPill(
                    val text: String,
                    val w: Float,
                    val h: Float,
                    val boundingW: Float,
                    val boundingH: Float,
                    val rotation: Float,
                    val bgColor: Int,
                    val textColor: Int
                )

                val maxRowWidth = 920f
                val spacingX = 14f
                val spacingY = 10f
                val rows = mutableListOf<MutableList<NotificationSnippetPill>>()
                val rowHeights = mutableListOf<Float>()
                var currentRow = mutableListOf<NotificationSnippetPill>()
                var currentRowWidth = 0f
                var currentRowHeight = 0f

                photo.snippets.forEach { snippetText ->
                    val trimmed = snippetText.trim()
                    if (trimmed.isNotBlank()) {
                        val stableRandom = Random(trimmed.hashCode().toLong())
                        val rotation = (stableRandom.nextFloat() * 12f - 6f)
                        val textBounds = Rect()
                        snippetTextPaint.getTextBounds(trimmed, 0, trimmed.length, textBounds)
                        val pillW = textBounds.width() + 32f
                        val pillH = snippetPillH

                        val rad = Math.toRadians(rotation.toDouble())
                        val absCos = Math.abs(Math.cos(rad)).toFloat()
                        val absSin = Math.abs(Math.sin(rad)).toFloat()
                        val boundingW = pillW * absCos + pillH * absSin
                        val boundingH = pillH * absCos + pillW * absSin

                        if (currentRow.isNotEmpty() && currentRowWidth + spacingX + boundingW > maxRowWidth) {
                            rows.add(currentRow)
                            rowHeights.add(currentRowHeight)
                            currentRow = mutableListOf()
                            currentRowWidth = 0f
                            currentRowHeight = 0f
                        }

                        val bgColor = getSnippetBackgroundColorInt(context, isDark = true)
                        val textColor = getSnippetTextColorInt(context, isDark = true)
                        currentRow.add(NotificationSnippetPill(trimmed, pillW, pillH, boundingW, boundingH, rotation, bgColor, textColor))
                        currentRowWidth += boundingW + (if (currentRow.size > 1) spacingX else 0f)
                        currentRowHeight = maxOf(currentRowHeight, boundingH)
                    }
                }
                if (currentRow.isNotEmpty()) {
                    rows.add(currentRow)
                    rowHeights.add(currentRowHeight)
                }

                var currentY = snippetAreaTop
                val snippetFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
                val snippetStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    color = Color.WHITE
                    alpha = (255 * 0.35f).toInt()
                }

                rows.forEachIndexed { rowIndex, row ->
                    val rowHeight = rowHeights[rowIndex]
                    val rowWidth = row.sumOf { it.boundingW.toDouble() }.toFloat() + (row.size - 1) * spacingX
                    var currentX = cx - (rowWidth / 2f)
                    val centerY = currentY + (rowHeight / 2f)

                    row.forEach { pill ->
                        val px = currentX + (pill.boundingW / 2f)
                        val py = centerY

                        canvas.save()
                        canvas.rotate(pill.rotation, px, py)

                        snippetFillPaint.color = pill.bgColor
                        val pillRect = RectF(px - pill.w / 2f, py - pill.h / 2f, px + pill.w / 2f, py + pill.h / 2f)
                        canvas.drawRoundRect(pillRect, snippetPillRadius, snippetPillRadius, snippetFillPaint)
                        canvas.drawRoundRect(pillRect, snippetPillRadius, snippetPillRadius, snippetStrokePaint)

                        snippetTextPaint.color = pill.textColor
                        val textY = py - ((snippetTextPaint.descent() + snippetTextPaint.ascent()) / 2f)
                        canvas.drawText(pill.text, px, textY, snippetTextPaint)

                        canvas.restore()
                        currentX += pill.boundingW + spacingX
                    }
                    currentY += rowHeight + spacingY
                }
            }

            output
        } catch (e: Exception) {
            e.printStackTrace()
            createMemoryCardBitmap(photoBitmap)
        }
    }

    /**
     * Fallback that renders the basic memory image:
     * - Full blurred background with a 50% dark tint overlay
     * - Centered 9-sided Cookie shape (CookiePolygon)
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

            // 2. Calculate dimensions for 9-sided Cookie Shape
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

            // 3. Draw photo clipped to the 9-sided Cookie Shape
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

        val tinyW = (targetWidth * 0.08f).toInt().coerceAtLeast(8)
        val tinyH = (targetHeight * 0.08f).toInt().coerceAtLeast(8)
        val scaleX = tinyW / srcWidth
        val scaleY = tinyH / srcHeight
        val scale = maxOf(scaleX, scaleY)
        val scaledW = (srcWidth * scale).toInt().coerceAtLeast(1)
        val scaledH = (srcHeight * scale).toInt().coerceAtLeast(1)

        val tiny = Bitmap.createBitmap(tinyW, tinyH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(tiny)
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate((tinyW - scaledW) * 0.5f, (tinyH - scaledH) * 0.5f)
        }
        canvas.drawBitmap(src, matrix, null)

        val blurredTiny = boxBlur(tiny, 4)
        val blurred = Bitmap.createScaledBitmap(blurredTiny, targetWidth, targetHeight, true)

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
                rSum += (pixel ushr 16) and 0xFF
                gSum += (pixel ushr 8) and 0xFF
                bSum += pixel and 0xFF
            }

            for (x in 0 until width) {
                newPixels[offset + x] = (0xFF shl 24) or ((rSum / size) shl 16) or ((gSum / size) shl 8) or (bSum / size)
                val prevX = (x - range).coerceIn(0, width - 1)
                val nextX = (x + range + 1).coerceIn(0, width - 1)
                val prevPixel = pixels[offset + prevX]
                val nextPixel = pixels[offset + nextX]
                rSum += ((nextPixel ushr 16) and 0xFF) - ((prevPixel ushr 16) and 0xFF)
                gSum += ((nextPixel ushr 8) and 0xFF) - ((prevPixel ushr 8) and 0xFF)
                bSum += (nextPixel and 0xFF) - (prevPixel and 0xFF)
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
                rSum += (pixel ushr 16) and 0xFF
                gSum += (pixel ushr 8) and 0xFF
                bSum += pixel and 0xFF
            }

            for (y in 0 until height) {
                finalPixels[y * width + x] = (0xFF shl 24) or ((rSum / size) shl 16) or ((gSum / size) shl 8) or (bSum / size)
                val prevY = (y - range).coerceIn(0, height - 1)
                val nextY = (y + range + 1).coerceIn(0, height - 1)
                val prevPixel = newPixels[prevY * width + x]
                val nextPixel = newPixels[nextY * width + x]
                rSum += ((nextPixel ushr 16) and 0xFF) - ((prevPixel ushr 16) and 0xFF)
                gSum += ((nextPixel ushr 8) and 0xFF) - ((prevPixel ushr 8) and 0xFF)
                bSum += (nextPixel and 0xFF) - (prevPixel and 0xFF)
            }
        }

        return Bitmap.createBitmap(finalPixels, width, height, Bitmap.Config.ARGB_8888)
    }

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
