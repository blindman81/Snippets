package com.android.snippets.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.exifinterface.media.ExifInterface
import com.android.snippets.MainActivity
import com.android.snippets.model.Photo
import com.android.snippets.ui.shapes.AppShape
import com.android.snippets.ui.shapes.PolygonDrawable
import com.android.snippets.ui.shapes.getNormalizedPolygon
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.ln.android.snippets.R
import java.io.File
import java.io.InputStream
import java.util.Calendar

class HistoryWidgetProvider : AppWidgetProvider() {

    companion object {
        const val PREFS_NAME = "history_widget_prefs"
        const val KEY_CURRENT_INDEX = "current_photo_index"
        const val KEY_LAST_ROTATE_TIME = "last_rotate_time"

        const val ACTION_ROTATE_WIDGET = "com.android.snippets.action.ROTATE_WIDGET"
        const val ACTION_WIDGET_TAP = "com.android.snippets.action.WIDGET_TAP"

        const val ROTATE_INTERVAL_MS = 6 * 60 * 60 * 1000L // 6 hours

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, HistoryWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, HistoryWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }

        fun isQuietHours(currentTime: Long = System.currentTimeMillis()): Boolean {
            val calendar = Calendar.getInstance().apply { timeInMillis = currentTime }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            return hour in 0..5 // 12:00 AM (0) up to 5:59 AM (5). Starts again at 6:00 AM (6)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastRotateTime = prefs.getLong(KEY_LAST_ROTATE_TIME, 0L)
        val now = System.currentTimeMillis()

        var shouldRotate = false
        if (!isQuietHours(now) && (now - lastRotateTime >= ROTATE_INTERVAL_MS || lastRotateTime == 0L)) {
            shouldRotate = true
        }

        val photos = loadPhotos(context)
        var currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)

        if (shouldRotate && photos.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % photos.size
            prefs.edit()
                .putInt(KEY_CURRENT_INDEX, currentIndex)
                .putLong(KEY_LAST_ROTATE_TIME, now)
                .apply()
        }

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, photos, currentIndex)
        }

        scheduleNextRotation(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextRotation(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelRotationAlarm(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_ROTATE_WIDGET -> {
                val now = System.currentTimeMillis()
                if (!isQuietHours(now)) {
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val photos = loadPhotos(context)
                    if (photos.isNotEmpty()) {
                        val nextIndex = (prefs.getInt(KEY_CURRENT_INDEX, 0) + 1) % photos.size
                        prefs.edit()
                            .putInt(KEY_CURRENT_INDEX, nextIndex)
                            .putLong(KEY_LAST_ROTATE_TIME, now)
                            .apply()
                    }
                }
                updateAllWidgets(context)
                scheduleNextRotation(context)
            }

            ACTION_WIDGET_TAP -> {
                val tappedPhotoId = intent.getStringExtra("photo_id")
                
                // 1. Open app to Detail screen for tapped photo
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("open_photo_id", tappedPhotoId)
                }
                context.startActivity(launchIntent)

                // 2. Rotate photo to next one on tap
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val photos = loadPhotos(context)
                if (photos.isNotEmpty()) {
                    val nextIndex = (prefs.getInt(KEY_CURRENT_INDEX, 0) + 1) % photos.size
                    prefs.edit()
                        .putInt(KEY_CURRENT_INDEX, nextIndex)
                        .putLong(KEY_LAST_ROTATE_TIME, System.currentTimeMillis())
                        .apply()
                }

                updateAllWidgets(context)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        photos: List<Photo>,
        currentIndex: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_history)

        if (photos.isEmpty()) {
            views.setViewVisibility(R.id.widget_image, View.GONE)
            views.setViewVisibility(R.id.widget_snippets_container, View.GONE)
            views.setViewVisibility(R.id.widget_empty_text, View.VISIBLE)

            // Tap opens app library
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingAppIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingAppIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }

        val validIndex = if (currentIndex in photos.indices) currentIndex else 0
        val currentPhoto = photos[validIndex]

        // Load image bitmap safely with left-only rounded corners (matching PhotoCardListItem)
        val rawBitmap = loadScaledBitmap(context, currentPhoto.uriString, 800, 800)
        val density = context.resources.displayMetrics.density
        val bitmap = if (rawBitmap != null) getLeftRoundedCornerBitmap(rawBitmap, 16f * density) else null

        if (bitmap != null) {
            views.setImageViewBitmap(R.id.widget_image, bitmap)
            views.setViewVisibility(R.id.widget_image, View.VISIBLE)
            views.setViewVisibility(R.id.widget_empty_text, View.GONE)
        } else {
            views.setViewVisibility(R.id.widget_image, View.GONE)
            views.setViewVisibility(R.id.widget_empty_text, View.VISIBLE)
        }

        // Render Snippets & Badges
        val snippets = currentPhoto.snippets ?: emptyList()

        val selectedShape = getSelectedShape(context)
        val badgeColor = android.graphics.Color.parseColor("#D96B27")

        // 1. Star Rating Badge
        if (currentPhoto.rating > 0) {
            views.setTextViewText(R.id.widget_star_text, currentPhoto.rating.toString())
            views.setTextColor(R.id.widget_star_text, android.graphics.Color.parseColor("#2A180E"))
            
            // Set the background bitmap of the selected shape
            val starBgBitmap = getShapeBitmap(context, selectedShape, badgeColor, 28)
            views.setImageViewBitmap(R.id.widget_star_badge_background, starBgBitmap)
            
            // Set the star icon tinted to white
            views.setInt(R.id.widget_star_icon, "setColorFilter", android.graphics.Color.WHITE)
            
            views.setViewVisibility(R.id.widget_star_badge_container, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_star_badge_container, View.GONE)
        }

        // 2. Favorite Heart Icon
        if (currentPhoto.isFavorite) {
            // Set the background bitmap of the selected shape
            val favBgBitmap = getShapeBitmap(context, selectedShape, badgeColor, 28)
            views.setImageViewBitmap(R.id.widget_favorite_background, favBgBitmap)
            
            // Set the heart icon tinted to white
            views.setInt(R.id.widget_favorite_icon, "setColorFilter", android.graphics.Color.WHITE)
            
            views.setViewVisibility(R.id.widget_favorite_container, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_favorite_container, View.GONE)
        }

        // 3. Render Snippet Pills with Custom Shapes and Colors (matching PhotoCardListItem)
        val defaultColors = intArrayOf(
            android.graphics.Color.parseColor("#FFCA28"), // Yellow
            android.graphics.Color.parseColor("#66BB6A"), // Green
            android.graphics.Color.parseColor("#29B6F6")  // Blue
        )
        val snippetColorsMap = loadSnippetColors(context)

        if (snippets.isNotEmpty()) {
            views.setViewVisibility(R.id.widget_snippets_container, View.VISIBLE)
            views.setViewVisibility(R.id.widget_no_snippets_text, View.GONE)

            val isSingle = snippets.size == 1

            // Snippet 1
            val s1 = snippets[0]
            val c1 = snippetColorsMap[s1]
            val color1 = c1 ?: defaultColors[0 % defaultColors.size]
            val bgRes1 = getSnippetDrawableRes(getClosestColorIndex(c1), isFirst = true, isSingle = isSingle)
            views.setTextViewText(R.id.widget_snippet_1, s1)
            views.setTextColor(R.id.widget_snippet_1, color1)
            views.setInt(R.id.widget_snippet_1, "setBackgroundResource", bgRes1)
            views.setViewVisibility(R.id.widget_snippet_1, View.VISIBLE)

            // Snippet 2
            if (snippets.size >= 2) {
                val s2 = snippets[1]
                val text2 = if (snippets.size > 2) "$s2  +${snippets.size - 2}" else s2
                val c2 = snippetColorsMap[s2]
                val color2 = c2 ?: defaultColors[1 % defaultColors.size]
                val bgRes2 = getSnippetDrawableRes(getClosestColorIndex(c2), isFirst = false, isSingle = false)
                views.setTextViewText(R.id.widget_snippet_2, text2)
                views.setTextColor(R.id.widget_snippet_2, color2)
                views.setInt(R.id.widget_snippet_2, "setBackgroundResource", bgRes2)
                views.setViewVisibility(R.id.widget_snippet_2, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_snippet_2, View.GONE)
            }

            views.setViewVisibility(R.id.widget_snippet_3, View.GONE)
        } else {
            views.setViewVisibility(R.id.widget_snippet_1, View.GONE)
            views.setViewVisibility(R.id.widget_snippet_2, View.GONE)
            views.setViewVisibility(R.id.widget_snippet_3, View.GONE)
            views.setViewVisibility(R.id.widget_no_snippets_text, View.VISIBLE)
        }

        // Tap action: Broadcast ACTION_WIDGET_TAP with photo_id
        val tapIntent = Intent(context, HistoryWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_TAP
            putExtra("photo_id", currentPhoto.id)
            putExtra("widget_id", appWidgetId)
        }
        val pendingTapIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingTapIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)

        // Android 15+ dynamic widget preview update
        if (Build.VERSION.SDK_INT >= 35) {
            try {
                appWidgetManager.setWidgetPreview(
                    ComponentName(context, HistoryWidgetProvider::class.java),
                    AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                    views
                )
            } catch (e: Exception) {
                // Ignore any rate limits or exceptions
            }
        }
    }

    private fun loadPhotos(context: Context): List<Photo> {
        val photosFile = File(context.filesDir, "photos_v2.json")
        if (!photosFile.exists()) return emptyList()

        return try {
            val json = photosFile.readText()
            val gson = Gson()
            val jsonArray = gson.fromJson(json, JsonArray::class.java)
            val list = mutableListOf<Photo>()
            jsonArray.forEach { element ->
                try {
                    val photo = gson.fromJson(element, Photo::class.java)
                    if (!photo.uriString.isNullOrEmpty() && !photo.collections.contains("Eatlist")) {
                        list.add(photo)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun loadScaledBitmap(context: Context, uriString: String, maxW: Int, maxH: Int): Bitmap? {
        val uri = Uri.parse(uriString)
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri) ?: return null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            var sampleSize = 1
            if (options.outHeight > maxH || options.outWidth > maxW) {
                val halfH = options.outHeight / 2
                val halfW = options.outWidth / 2
                while ((halfH / sampleSize) >= maxH && (halfW / sampleSize) >= maxW) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            inputStream = context.contentResolver.openInputStream(uri) ?: return null
            var bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions) ?: return null
            inputStream.close()

            // Handle EXIF orientation
            try {
                context.contentResolver.openInputStream(uri)?.use { exifStream ->
                    val exif = ExifInterface(exifStream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED
                    )
                    bitmap = rotateBitmapIfNeeded(bitmap, orientation)
                }
            } catch (e: Exception) {
                // Ignore EXIF errors
            }

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                inputStream?.close()
            } catch (ignored: Exception) {}
        }
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun getLeftRoundedCornerBitmap(bitmap: Bitmap, cornerRadiusPx: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        val rect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = android.graphics.RectF(rect)
        
        val path = android.graphics.Path()
        val radii = floatArrayOf(
            cornerRadiusPx, cornerRadiusPx, // Top-left
            0f, 0f,                         // Top-right
            0f, 0f,                         // Bottom-right
            cornerRadiusPx, cornerRadiusPx  // Bottom-left
        )
        path.addRoundRect(rectF, radii, android.graphics.Path.Direction.CW)
        
        canvas.drawPath(path, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    private fun loadSnippetColors(context: Context): Map<String, Int> {
        val file = File(context.filesDir, "snippet_colors_v2.json")
        if (!file.exists()) return emptyMap()
        return try {
            val json = file.readText()
            val gson = Gson()
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    private fun getClosestColorIndex(colorInt: Int?): Int {
        if (colorInt == null) return 0
        
        val r = (colorInt shr 16) and 0xFF
        val g = (colorInt shr 8) and 0xFF
        val b = colorInt and 0xFF
        
        val distYellow = (r - 255) * (r - 255) + (g - 202) * (g - 202) + (b - 40) * (b - 40)
        val distGreen = (r - 102) * (r - 102) + (g - 187) * (g - 187) + (b - 106) * (b - 106)
        val distBlue = (r - 41) * (r - 41) + (g - 182) * (g - 182) + (b - 246) * (b - 246)
        
        val minDist = minOf(distYellow, distGreen, distBlue)
        return when (minDist) {
            distYellow -> 0
            distGreen -> 1
            else -> 2
        }
    }

    private fun getSnippetDrawableRes(colorIndex: Int, isFirst: Boolean, isSingle: Boolean): Int {
        val col = colorIndex % 3
        return when (col) {
            0 -> { // Yellow
                if (isSingle) R.drawable.bg_snippet_yellow_single
                else if (isFirst) R.drawable.bg_snippet_yellow_first
                else R.drawable.bg_snippet_yellow_last
            }
            1 -> { // Green
                if (isSingle) R.drawable.bg_snippet_green_single
                else if (isFirst) R.drawable.bg_snippet_green_first
                else R.drawable.bg_snippet_green_last
            }
            else -> { // Blue
                if (isSingle) R.drawable.bg_snippet_blue_single
                else if (isFirst) R.drawable.bg_snippet_blue_first
                else R.drawable.bg_snippet_blue_last
            }
        }
    }

    private fun scheduleNextRotation(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, HistoryWidgetProvider::class.java).apply {
            action = ACTION_ROTATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextTime = System.currentTimeMillis() + ROTATE_INTERVAL_MS
        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                nextTime,
                ROTATE_INTERVAL_MS,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelRotationAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, HistoryWidgetProvider::class.java).apply {
            action = ACTION_ROTATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getSelectedShape(context: Context): AppShape {
        val prefs = context.getSharedPreferences("snippets_prefs", Context.MODE_PRIVATE)
        val savedShape = prefs.getString("selected_shape", AppShape.COOKIE_12_SIDED.name)
        return try {
            AppShape.valueOf(savedShape!!)
        } catch (e: Exception) {
            AppShape.COOKIE_12_SIDED
        }
    }

    private fun getShapeBitmap(context: Context, shape: AppShape, fillColor: Int, sizeDp: Int): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        val polygon = shape.getNormalizedPolygon()
        val drawable = PolygonDrawable(polygon, fillColor)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(canvas)
        
        return bitmap
    }
}
