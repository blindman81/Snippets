package com.android.snippets.ui.util

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import coil.imageLoader
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.compose.ui.graphics.asAndroidPath
import androidx.core.content.FileProvider
import com.android.snippets.model.Photo
import com.ln.android.snippets.R
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import kotlinx.coroutines.Dispatchers
import com.android.snippets.ui.shapes.CookiePolygon
import com.android.snippets.ui.shapes.AppShape
import com.android.snippets.ui.theme.getAppColorScheme
import com.android.snippets.ui.theme.getSnippetBackgroundColorInt
import com.android.snippets.ui.theme.getSnippetTextColorInt
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

object MediaSaver {

    private val cachedFonts = mutableMapOf<String, Typeface>()

    private fun getCustomTypeface(context: Context, variationSettings: String): Typeface? {
        val cached = cachedFonts[variationSettings]
        if (cached != null) return cached

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val cacheFile = File(context.cacheDir, "google_sans_flex_temp.ttf")
                if (!cacheFile.exists()) {
                    context.resources.openRawResource(com.ln.android.snippets.R.font.google_sans_flex).use { input ->
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
            androidx.core.content.res.ResourcesCompat.getFont(context, com.ln.android.snippets.R.font.google_sans_flex)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveSnippetToGallery(context: Context, photo: Photo, snippets: List<String>, isDark: Boolean = false, bgColor: Int = Color.WHITE, appShape: AppShape = AppShape.COOKIE_9_SIDED, showTime: Boolean = true, locationText: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val bitmap = createSnippetBitmap(context, photo, snippets, isDark, bgColor, appShape, showTime, locationText) ?: return@withContext false
            
            val fileName = "Snippet_Card_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Snippets")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                try {
                    val coords = LocationUtils.extractCoordinates(context, photo)
                    if (coords != null) {
                        @Suppress("DEPRECATION")
                        put(MediaStore.Images.ImageColumns.LATITUDE, coords.first)
                        @Suppress("DEPRECATION")
                        put(MediaStore.Images.ImageColumns.LONGITUDE, coords.second)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            val resolver = context.contentResolver
            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            imageUri?.let { uri ->
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
                }
                
                try {
                    val coords = LocationUtils.extractCoordinates(context, photo)
                    if (coords != null) {
                        resolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                            val exif = ExifInterface(pfd.fileDescriptor)
                            exif.setLatLong(coords.first, coords.second)
                            exif.saveAttributes()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                bitmap.recycle()
                return@withContext true
            }
            
            bitmap.recycle()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getShareableUri(context: Context, photo: Photo, snippets: List<String>, isDark: Boolean = false, bgColor: Int = Color.WHITE, appShape: AppShape = AppShape.COOKIE_9_SIDED, showTime: Boolean = true, locationText: String? = null): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = createSnippetBitmap(context, photo, snippets, isDark, bgColor, appShape, showTime, locationText) ?: return@withContext null
            
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val safeId = photo.id.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val file = File(cachePath, "share_snippet_$safeId.jpg")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            stream.close()

            try {
                val coords = LocationUtils.extractCoordinates(context, photo)
                if (coords != null) {
                    val exif = ExifInterface(file.absolutePath)
                    exif.setLatLong(coords.first, coords.second)
                    exif.saveAttributes()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            bitmap.recycle()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun createSnippetBitmap(context: Context, photo: Photo, snippets: List<String>, isDark: Boolean, bgColor: Int, appShape: AppShape = AppShape.COOKIE_9_SIDED, showTime: Boolean = true, overrideLocationText: String? = null): Bitmap? {
        val width = 1440
        val height = 2560
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val colorScheme = getAppColorScheme(context, isDark)
        
        val photoBitmap = runCoil(context, photo.uri)

        // 1. Draw Background (Fallback solid color first, then blurred photo if loaded)
        canvas.drawColor(bgColor)
        
        val blurredBackground = if (photoBitmap != null) {
            val bg = createBlurredBackground(photoBitmap, width, height)
            val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                isDither = true
            }
            canvas.drawBitmap(bg, 0f, 0f, blurPaint)
            
            // Draw Dim/Tint Overlay (always dark overlay)
            val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                alpha = (255 * 0.5f).toInt()
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
            bg
        } else null
        
        // 2. Draw Photo in the selected Shape
        val cx = width / 2f
        val centerX = cx
        val cy = 924f
        val radius = 624f
        val targetSize = 2 * radius // 1248f
        
        val normalizedPolygon = CookiePolygon
        val path = normalizedPolygon.toPath()
        val bounds = RectF()
        path.computeBounds(bounds, true)
        val matrix = android.graphics.Matrix()
        matrix.postTranslate(-bounds.left, -bounds.top)
        val scaleX = if (bounds.width() > 0f) targetSize / bounds.width() else 1f
        val scaleY = if (bounds.height() > 0f) targetSize / bounds.height() else 1f
        matrix.postScale(scaleX, scaleY)
        matrix.postTranslate(cx - radius, cy - radius)
        path.transform(matrix)
        
        val photoPath = path
        
        if (photoBitmap != null) {
            canvas.save()
            canvas.clipPath(photoPath)
            
            val srcWidth = photoBitmap.width.toFloat()
            val srcHeight = photoBitmap.height.toFloat()
            
            val scale = targetSize / minOf(srcWidth, srcHeight)
            
            val scaledWidth = srcWidth * scale
            val scaledHeight = srcHeight * scale
            
            val focusOffsetX = cx - scaledWidth * 0.5f
            val focusOffsetY = cy - scaledHeight * 0.5f
            
            val matrix = android.graphics.Matrix()
            matrix.postScale(scale, scale)
            matrix.postTranslate(focusOffsetX, focusOffsetY)
            
            canvas.drawBitmap(photoBitmap, matrix, null)
            canvas.restore()
        } else {
            // Draw a fallback placeholder rounded rect if photo load fails
            val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isDark) Color.parseColor("#303030") else Color.parseColor("#E0E0E0")
            }
            canvas.drawPath(photoPath, placeholderPaint)
        }

        // Draw a thick border around the Photo Rectangle
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 16f // 4.dp equivalent
            color = Color.WHITE
        }
        canvas.drawPath(photoPath, borderPaint)

        // 3. Draw Header: Label & Photo Name (Left) | Calendar & Date/Time (Center) | Location on & Location (Right)
        val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
        val timeFormat = if (is24Hour) "HH:mm" else "h:mm a"
        val datePattern = if (showTime) "EEE, d MMM • $timeFormat" else "EEE, d MMM"
        val dateString = SimpleDateFormat(datePattern, Locale.getDefault()).format(Date(photo.date)).uppercase()
        val locationText = overrideLocationText ?: LocationUtils.getLocationFromExif(context, photo)
        val photoName = photo.name?.takeIf { it.isNotBlank() }

        val hasRating = photo.rating > 0
        val headerCenterY = if (hasRating) 220f else 180f

        if (hasRating) {
            val scaleFactor = width / 360f
            val starSize = 28f * scaleFactor
            val starLeft = (width - starSize) / 2f
            val starTop = 30f

            val starDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_star_rating,
                null
            )
            if (starDrawable != null) {
                starDrawable.setBounds(starLeft.toInt(), starTop.toInt(), (starLeft + starSize).toInt(), (starTop + starSize).toInt())
                val tintedDrawable = starDrawable.mutate()
                androidx.core.graphics.drawable.DrawableCompat.setTint(tintedDrawable, Color.WHITE)
                tintedDrawable.draw(canvas)

                // Draw the rating number inside the star
                val ratingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#1F1F1F")
                    textSize = 10f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val textX = starLeft + starSize / 2f
                val textY = starTop + starSize / 2f + (ratingPaint.textSize / 3f)
                canvas.drawText(photo.rating.toString(), textX, textY, ratingPaint)
            }
        }

        val paddingX = 48f
        val pillH = 52f
        val pillRadius = pillH / 2f

        val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = (255 * 0.25f).toInt()
            style = Paint.Style.FILL
        }
        val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = (255 * 0.4f).toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }

        // --- Center: Calendar month icon and day, date month and time in pill ---
        val datePaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 240
            textSize = 26f
            typeface = getCustomTypeface(context, "'wght' 600, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val calIconSize = 28f
        val calItemSpacing = 10f
        val calHorizontalPadding = 20f
        val dateTextWidth = datePaint.measureText(dateString)
        val centerPillW = calHorizontalPadding * 2 + calIconSize + calItemSpacing + dateTextWidth
        val centerPillLeft = (width - centerPillW) / 2f
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
        canvas.drawText(dateString, calIconX + calIconSize + calItemSpacing, dateTextY, datePaint)

        // --- Left: Label icon and Photo name (2dp gap from calendar pill) ---
        if (!photoName.isNullOrBlank()) {
            val namePaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 240
                textSize = 26f
                typeface = getCustomTypeface(context, "'wght' 600, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val labelIconSize = 28f
            val itemSpacing = 10f
            val horizontalPadding = 20f
            val gapFromCalendar = 2f * (width / 360f) // 2dp in px

            val pillRight = centerPillRect.left - gapFromCalendar
            val maxLeftPillWidth = (pillRight - paddingX).coerceAtLeast(10f)
            val maxTextWidth = (maxLeftPillWidth - horizontalPadding * 2 - labelIconSize - itemSpacing).coerceAtLeast(10f)
            val ellipsizedName = android.text.TextUtils.ellipsize(photoName, namePaint, maxTextWidth, android.text.TextUtils.TruncateAt.END).toString()
            val textWidth = namePaint.measureText(ellipsizedName)

            val pillW = horizontalPadding * 2 + labelIconSize + itemSpacing + textWidth
            val pillLeft = pillRight - pillW
            val pillTop = headerCenterY - (pillH / 2f)
            val pillRect = RectF(pillLeft, pillTop, pillRight, pillTop + pillH)

            canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBgPaint)
            canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBorderPaint)

            val labelDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, R.drawable.ic_label, null)
            val iconX = pillLeft + horizontalPadding
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

        // --- Right: Location on icon to show location (2dp gap from calendar pill) ---
        if (!locationText.isNullOrBlank()) {
            val locTextPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 240
                textSize = 26f
                typeface = getCustomTypeface(context, "'wght' 600, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val locIconSize = 28f
            val itemSpacing = 10f
            val horizontalPadding = 20f
            val gapFromCalendar = 2f * (width / 360f) // 2dp in px

            val locPillLeft = centerPillRect.right + gapFromCalendar
            val maxRightPillWidth = (width - paddingX - locPillLeft).coerceAtLeast(10f)
            val maxTextWidth = (maxRightPillWidth - horizontalPadding * 2 - locIconSize - itemSpacing).coerceAtLeast(10f)
            val ellipsizedLoc = android.text.TextUtils.ellipsize(locationText, locTextPaint, maxTextWidth, android.text.TextUtils.TruncateAt.END).toString()
            val textWidth = locTextPaint.measureText(ellipsizedLoc)

            val pillW = horizontalPadding * 2 + locIconSize + itemSpacing + textWidth
            val pillLeft = locPillLeft
            val pillRight = pillLeft + pillW
            val pillTop = headerCenterY - (pillH / 2f)
            val pillRect = RectF(pillLeft, pillTop, pillRight, pillTop + pillH)

            canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBgPaint)
            canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBorderPaint)

            val locDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, R.drawable.ic_location_on, null)
            val iconX = pillLeft + horizontalPadding
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

        // 4. Draw Snippets (Synchronized with ExpressiveShareSheet logic)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        val scaleFactor = width / 360f // Baseline for DP to PX scaling
        
        data class PlacedPill(val text: String, val w: Float, val h: Float, val boundingW: Float, val boundingH: Float, val textSize: Float, val color: Int, val rot: Float, val textBounds: Rect, val typeface: Typeface, val spacing: Float, val isFilled: Boolean)
        val maxRowWidth = 1100f
        val spacingX = 48f
        val spacingY = 48f
        
        val rows = mutableListOf<MutableList<PlacedPill>>()
        val rowHeights = mutableListOf<Float>()
        var currentRow = mutableListOf<PlacedPill>()
        var currentRowWidth = 0f
        var currentRowHeight = 0f
        
        snippets.forEachIndexed { index, text ->
            val trimmedText = text.trim()
            val colorInt = getSnippetBackgroundColorInt(context, isDark)
            val stableRandom = Random(trimmedText.hashCode().toLong())
            val personality = stableRandom.nextInt(0, 5)
            val isFilled = true
            
            val rotation = (stableRandom.nextFloat() * 16f - 8f)
            
            val total = snippets.size
            val scalingFactor = com.android.snippets.ui.util.DistributionMath.getCloudScalingFactor(total)
            
            val typeface = getCustomTypeface(context, "'wght' 700, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val letterSpacingVal = 0f

            val baseFontSize = when (personality) {
                0, 1 -> 24f // Large
                2, 3 -> 18f // Medium
                else -> 14f // Small
            }
            var size = (baseFontSize * scalingFactor) * scaleFactor
            
            textPaint.typeface = typeface
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                textPaint.letterSpacing = letterSpacingVal
            }
            
            textPaint.textSize = size
            val bounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, bounds)
            var pillW = bounds.width() + (textPaint.textSize * 1.5f)
            val pillH = bounds.height() + (textPaint.textSize * 1.2f)
            
            if (pillW > maxRowWidth) {
                val downscaleRatio = maxRowWidth / pillW
                size *= downscaleRatio
                textPaint.textSize = size
                textPaint.getTextBounds(text, 0, text.length, bounds)
                pillW = bounds.width() + (textPaint.textSize * 1.5f)
            }
            
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
            
            currentRow.add(PlacedPill(text, pillW, pillH, boundingW, boundingH, size, colorInt, rotation, bounds, typeface, letterSpacingVal, isFilled))
            currentRowWidth += boundingW + (if (currentRow.size > 1) spacingX else 0f)
            currentRowHeight = maxOf(currentRowHeight, boundingH)
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowHeights.add(currentRowHeight)
        }
        
        val snippetAreaTop = height - 1000f
        val snippetAreaBottom = height - 200f
        val snippetAreaHeight = (snippetAreaBottom - snippetAreaTop).coerceAtLeast(1f)
        val totalHeight = rowHeights.sum() + (rows.size - 1) * spacingY
        val fitScale = (snippetAreaHeight / totalHeight.coerceAtLeast(1f)).coerceAtMost(1f)
        val scaledSpacingX = spacingX * fitScale
        val scaledSpacingY = spacingY * fitScale
        val scaledTotalHeight = (rowHeights.sum() * fitScale) + (rows.size - 1) * scaledSpacingY
        var currentY = snippetAreaTop + ((snippetAreaHeight - scaledTotalHeight) / 2f).coerceAtLeast(0f)
        
        rows.forEachIndexed { rowIndex, row ->
            val rowHeight = rowHeights[rowIndex] * fitScale
            val rowWidth = row.sumOf { (it.boundingW * fitScale).toDouble() }.toFloat() + (row.size - 1) * scaledSpacingX
            var currentX = centerX - (rowWidth / 2f)
            
            val centerY = currentY + (rowHeight / 2f)
            
            row.forEach { pill ->
                val scaledBoundingW = pill.boundingW * fitScale
                val scaledW = pill.w * fitScale
                val scaledH = pill.h * fitScale
                val x = currentX + (scaledBoundingW / 2f)
                val y = centerY
                
                canvas.save()
                canvas.rotate(pill.rot, x, y)
                textPaint.color = getSnippetTextColorInt(context, isDark)
                textPaint.textSize = pill.textSize * fitScale
                textPaint.typeface = pill.typeface
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    textPaint.letterSpacing = pill.spacing
                }
                
                val pillPath = android.graphics.Path().apply {
                    val radius = scaledH / 2f
                    addRoundRect(
                        android.graphics.RectF(0f, 0f, scaledW, scaledH),
                        radius, radius,
                        android.graphics.Path.Direction.CW
                    )
                    offset(x - (scaledW / 2f), y - (scaledH / 2f))
                }
                
                // Frosted haze blur in pill container (matching MemoryScreen primaryContainer)
                pillPaint.style = Paint.Style.FILL
                pillPaint.color = pill.color
                pillPaint.alpha = if (blurredBackground != null) (255 * 0.70f).toInt() else 255
                canvas.drawPath(pillPath, pillPaint)

                val currentBounds = Rect()
                textPaint.getTextBounds(pill.text, 0, pill.text.length, currentBounds)
                val textY = y - currentBounds.exactCenterY()
                canvas.drawText(pill.text, x, textY, textPaint)
                canvas.restore()
                
                currentX += scaledBoundingW + scaledSpacingX
            }
            currentY += rowHeight + scaledSpacingY
        }
        
        blurredBackground?.recycle()

        return bitmap
    }

    private suspend fun runCoil(context: Context, uri: Uri): Bitmap? {
        return try {
            val loader = context.imageLoader
            val request = coil.request.ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false) // Required for Canvas drawing
                .build()
            val result = loader.execute(request)
            (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createBlurredBackground(src: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap =
        BitmapBlurHelper.createBlurredBackground(src, targetWidth, targetHeight)
}
