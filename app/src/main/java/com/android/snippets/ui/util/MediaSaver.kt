package com.android.snippets.ui.util

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
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
import com.android.snippets.ui.shapes.AppShape
import com.android.snippets.ui.shapes.getNormalizedPolygon
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

    suspend fun saveSnippetToGallery(
        context: Context,
        photo: Photo,
        snippets: List<String>,
        isDark: Boolean = false,
        bgColor: Int = Color.WHITE,
        snippetColors: Map<String, Int> = emptyMap(),
        snippetStyles: Map<String, com.android.snippets.viewmodel.SnippetStyle> = emptyMap(),
        appShape: AppShape = AppShape.COOKIE_12_SIDED,
        showTime: Boolean = false,
        locationText: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val photoBitmap = runBlockingCoil(context, photo.uri)
            val tempFile = File(context.cacheDir, "temp_snippet_${System.currentTimeMillis()}.gif")
            val fos = FileOutputStream(tempFile)

            val numFrames = 15
            val frameDelayMs = 100 // 10 fps -> 1.5s loop
            val encoder = AnimatedGifEncoder()
            encoder.start(fos)
            encoder.setDelay(frameDelayMs)
            encoder.setRepeat(0) // 0 = loop forever
            encoder.setQuality(10)

            for (i in 0 until numFrames) {
                val progress = i.toFloat() / numFrames
                val bitmap = createSnippetBitmap(
                    context, photo, snippets, isDark, bgColor, snippetColors, snippetStyles,
                    appShape, showTime, locationText,
                    width = 720, height = 1280, frameProgress = progress, preloadedPhotoBitmap = photoBitmap
                ) ?: break
                encoder.addFrame(bitmap)
                bitmap.recycle()
            }
            encoder.finish()
            fos.close()
            photoBitmap?.recycle()

            val fileName = "Snippet_Card_${System.currentTimeMillis()}.gif"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/gif")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Snippets")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            imageUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outStream ->
                    tempFile.inputStream().use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                tempFile.delete()
                return@withContext true
            }

            tempFile.delete()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getShareableUri(
        context: Context,
        photo: Photo,
        snippets: List<String>,
        isDark: Boolean = false,
        bgColor: Int = Color.WHITE,
        snippetColors: Map<String, Int> = emptyMap(),
        snippetStyles: Map<String, com.android.snippets.viewmodel.SnippetStyle> = emptyMap(),
        appShape: AppShape = AppShape.COOKIE_12_SIDED,
        showTime: Boolean = false,
        locationText: String? = null
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val photoBitmap = runBlockingCoil(context, photo.uri)
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "share_snippet.gif")
            if (file.exists()) file.delete()
            val fos = FileOutputStream(file)

            val numFrames = 15
            val frameDelayMs = 100
            val encoder = AnimatedGifEncoder()
            encoder.start(fos)
            encoder.setDelay(frameDelayMs)
            encoder.setRepeat(0)
            encoder.setQuality(10)

            for (i in 0 until numFrames) {
                val progress = i.toFloat() / numFrames
                val bitmap = createSnippetBitmap(
                    context, photo, snippets, isDark, bgColor, snippetColors, snippetStyles,
                    appShape, showTime, locationText,
                    width = 720, height = 1280, frameProgress = progress, preloadedPhotoBitmap = photoBitmap
                ) ?: break
                encoder.addFrame(bitmap)
                bitmap.recycle()
            }
            encoder.finish()
            fos.flush()
            fos.close()
            photoBitmap?.recycle()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createSnippetBitmap(
        context: Context,
        photo: Photo,
        snippets: List<String>,
        isDark: Boolean,
        bgColor: Int,
        snippetColors: Map<String, Int>,
        snippetStyles: Map<String, com.android.snippets.viewmodel.SnippetStyle>,
        appShape: AppShape = AppShape.COOKIE_12_SIDED,
        showTime: Boolean = false,
        overrideLocationText: String? = null,
        width: Int = 1440,
        height: Int = 2560,
        frameProgress: Float = 0f,
        preloadedPhotoBitmap: Bitmap? = null
    ): Bitmap? {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val photoBitmap = preloadedPhotoBitmap ?: runBlockingCoil(context, photo.uri)

        // 1. Draw Background (Fallback solid color first, then blurred photo if loaded)
        canvas.drawColor(bgColor)
        
        if (photoBitmap != null) {
            val blurredBackground = createBlurredBackground(photoBitmap, width, height)
            val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                isDither = true
            }
            canvas.drawBitmap(blurredBackground, 0f, 0f, blurPaint)
            blurredBackground.recycle()
            
            // Draw Dim/Tint Overlay (always dark overlay)
            val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                alpha = (255 * 0.5f).toInt()
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
        }
        
        // 2. Draw Photo in the selected Shape
        val cx = width / 2f
        val centerX = cx
        val cy = height * 0.38f
        val targetSize = minOf(width * 0.72f, height * 0.38f)
        val radius = targetSize / 2f

        // Shape animation matching MemoryScreen logic (gentle & slow)
        val fraction = (frameProgress * 2f * Math.PI).toFloat()
        var shapeRotation = 0f
        var pulseScale = 1f
        var transX = 0f
        var transY = 0f

        when (appShape) {
            AppShape.COOKIE_12_SIDED, AppShape.PILL, AppShape.VERY_SUNNY -> {
                // Slow rotation: 9 degrees across 1.5s loop (matches 360 deg / 60s rate in MemoryScreen)
                shapeRotation = frameProgress * 9f
            }
            AppShape.GEM, AppShape.SQUARE -> {
                shapeRotation = 3f * Math.sin(fraction.toDouble()).toFloat()
            }
            AppShape.PENTAGON, AppShape.COOKIE_4_SIDED -> {
                pulseScale = 1f + 0.05f * Math.sin(fraction.toDouble()).toFloat()
            }
            AppShape.CLOVER_4_LEAF -> {
                transY = 12f * (width / 720f) * Math.sin(fraction.toDouble()).toFloat()
            }
            AppShape.CLOVER_8_LEAF -> {
                transX = 12f * (width / 720f) * Math.sin(fraction.toDouble()).toFloat()
            }
        }
        
        val normalizedPolygon = appShape.getNormalizedPolygon()
        val path = normalizedPolygon.toPath()
        val bounds = RectF()
        path.computeBounds(bounds, true)
        val matrix = android.graphics.Matrix()
        matrix.postTranslate(-bounds.left, -bounds.top)
        val scaleX = (if (bounds.width() > 0f) targetSize / bounds.width() else 1f) * pulseScale
        val scaleY = (if (bounds.height() > 0f) targetSize / bounds.height() else 1f) * pulseScale
        matrix.postScale(scaleX, scaleY)
        matrix.postTranslate(cx - radius + transX, cy - radius + transY)

        if (shapeRotation != 0f) {
            matrix.postRotate(shapeRotation, cx + transX, cy + transY)
        }

        path.transform(matrix)
        
        val photoPath = path
        
        if (photoBitmap != null) {
            canvas.save()
            canvas.clipPath(photoPath)
            
            val srcWidth = photoBitmap.width.toFloat()
            val srcHeight = photoBitmap.height.toFloat()
            
            val scale = (targetSize / minOf(srcWidth, srcHeight)) * pulseScale
            
            val scaledWidth = srcWidth * scale
            val scaledHeight = srcHeight * scale
            
            val focusOffsetX = (cx + transX) - scaledWidth * 0.5f
            val focusOffsetY = (cy + transY) - scaledHeight * 0.5f
            
            val imgMatrix = android.graphics.Matrix()
            imgMatrix.postScale(scale, scale)
            imgMatrix.postTranslate(focusOffsetX, focusOffsetY)
            
            canvas.drawBitmap(photoBitmap, imgMatrix, null)
            canvas.restore()
        } else {
            val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isDark) Color.parseColor("#303030") else Color.parseColor("#E0E0E0")
            }
            canvas.drawPath(photoPath, placeholderPaint)
        }

        // Draw a thick border around the Photo Shape
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f * (width / 360f) // 4.dp equivalent
            color = Color.WHITE
        }
        canvas.drawPath(photoPath, borderPaint)

        // 3. Draw Date & Location Header (Exact scale to match MemoryScreen: 12.sp & 10.sp)
        val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
        val timeFormat = if (is24Hour) "HH:mm" else "h:mm a"
        val datePattern = if (showTime) "EEE, d MMM • $timeFormat" else "EEE, d MMM"
        val dateString = SimpleDateFormat(datePattern, Locale.getDefault()).format(Date(photo.date)).uppercase()
        val locationText = overrideLocationText ?: LocationUtils.getLocationFromExif(context, photo)
        
        val scaleFactor = width / 360f

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 12f * scaleFactor // Matching 12.sp in MemoryScreen
            typeface = getCustomTypeface(context, "'wght' 700, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                letterSpacing = 0.08f
            }
        }

        val hasRating = photo.rating > 0
        val dateY = if (hasRating) 140f * scaleFactor else 120f * scaleFactor
        val pillTop = if (hasRating) 114f * scaleFactor else 94f * scaleFactor

        if (hasRating) {
            val starSize = 24f * scaleFactor
            val starLeft = (width - starSize) / 2f
            val starTop = 20f * scaleFactor

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

                val ratingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#1F1F1F")
                    textSize = 9f * scaleFactor
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val textX = starLeft + starSize / 2f
                val textY = starTop + starSize / 2f + (ratingPaint.textSize / 3f)
                canvas.drawText(photo.rating.toString(), textX, textY, ratingPaint)
            }
        }

        if (locationText.isNullOrBlank()) {
            datePaint.textAlign = Paint.Align.CENTER
            canvas.drawText(dateString, width / 2f, dateY, datePaint)
        } else {
            datePaint.textAlign = Paint.Align.LEFT
            val dateWidth = datePaint.measureText(dateString)

            val locTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 10f * scaleFactor // Matching 10.sp in MemoryScreen
                typeface = getCustomTypeface(context, "'wght' 600, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val locTextWidth = locTextPaint.measureText(locationText)

            val pillH = 22f * scaleFactor
            val pillPaddingH = 8f * scaleFactor
            val pillW = locTextWidth + (pillPaddingH * 2) + (14f * scaleFactor)
            val spacing = 8f * scaleFactor
            val totalHeaderWidth = dateWidth + spacing + pillW
            val startX = (width - totalHeaderWidth) / 2f

            canvas.drawText(dateString, startX, dateY, datePaint)

            val pillLeft = startX + dateWidth + spacing
            val pillRight = pillLeft + pillW
            val pillBottom = pillTop + pillH
            val pillRect = RectF(pillLeft, pillTop, pillRight, pillBottom)
            val pillRadius = pillH / 2f

            val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = (255 * 0.25f).toInt()
                style = Paint.Style.FILL
            }
            val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = (255 * 0.40f).toInt()
                style = Paint.Style.STROKE
                strokeWidth = 1f * scaleFactor
            }
            canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBgPaint)
            canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBorderPaint)

            val dotX = pillLeft + pillPaddingH + (4f * scaleFactor)
            val dotY = pillRect.centerY()
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(dotX, dotY, 3f * scaleFactor, dotPaint)

            val textCenterX = dotX + (6f * scaleFactor) + (locTextWidth / 2f)
            locTextPaint.textAlign = Paint.Align.CENTER
            val fontMetrics = locTextPaint.fontMetrics
            val locTextY = pillRect.centerY() - (fontMetrics.descent + fontMetrics.ascent) / 2f
            canvas.drawText(locationText, textCenterX, locTextY, locTextPaint)
        }

        // 4. Draw Snippets (Static layout matching MemoryScreen FloatingSnippet)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        data class PlacedPill(
            val text: String, 
            val w: Float, 
            val h: Float, 
            val boundingW: Float, 
            val boundingH: Float, 
            val textSize: Float, 
            val color: Int, 
            val rot: Float, 
            val textBounds: Rect, 
            val typeface: Typeface, 
            val spacing: Float, 
            val isFilled: Boolean
        )
        val maxRowWidth = width * 0.86f
        val spacingX = 8f * scaleFactor
        val spacingY = 8f * scaleFactor
        
        val rows = mutableListOf<MutableList<PlacedPill>>()
        val rowHeights = mutableListOf<Float>()
        var currentRow = mutableListOf<PlacedPill>()
        var currentRowWidth = 0f
        var currentRowHeight = 0f
        
        snippets.forEachIndexed { index, text ->
            val trimmedText = text.trim()
            val stableRandom = Random(trimmedText.hashCode().toLong())
            val forcedColor = snippetColors[trimmedText]
            val baseColor = if (forcedColor != null) {
                forcedColor
            } else {
                val colorStrategy = (index + stableRandom.nextInt(10)) % 3
                when (colorStrategy) {
                    0 -> Color.parseColor(if (isDark) "#D0BCFF" else "#6750A4")
                    1 -> {
                        val themes = if (isDark) intArrayOf(
                            Color.parseColor("#D0BCFF"),
                            Color.parseColor("#CCC2DC"),
                            Color.parseColor("#EFB8C8")
                        ) else intArrayOf(
                            Color.parseColor("#6750A4"),
                            Color.parseColor("#625b71"),
                            Color.parseColor("#7D5260")
                        )
                        themes[stableRandom.nextInt(themes.size)]
                    }
                    else -> {
                        val vivids = if (isDark) intArrayOf(
                            Color.parseColor("#FF8A65"), Color.parseColor("#F06292"), 
                            Color.parseColor("#BA68C8"), Color.parseColor("#4DD0E1"),
                            Color.parseColor("#81C784"), Color.parseColor("#FFD54F")
                        ) else intArrayOf(
                            Color.parseColor("#D84315"), Color.parseColor("#C2185B"), 
                            Color.parseColor("#7B1FA2"), Color.parseColor("#0097A7"),
                            Color.parseColor("#388E3C"), Color.parseColor("#FFA000")
                        )
                        vivids[stableRandom.nextInt(vivids.size)]
                    }
                }
            }

            val personality = stableRandom.nextInt(0, 5)
            val isFilled = true
            
            val rotation = (stableRandom.nextFloat() * 16f - 8f) // Tilt between -8 and +8 deg
            
            val total = snippets.size
            val scalingFactor = com.android.snippets.ui.util.DistributionMath.getCloudScalingFactor(total)
            
            val forcedStyle = snippetStyles[trimmedText] ?: com.android.snippets.viewmodel.SnippetStyle.Default
            
            val typeface = when(forcedStyle) {
                com.android.snippets.viewmodel.SnippetStyle.Thin ->
                    getCustomTypeface(context, "'wght' 100, 'ROND' 100") ?: Typeface.create("sans-serif-thin", Typeface.NORMAL)
                com.android.snippets.viewmodel.SnippetStyle.Cursive ->
                    Typeface.create("cursive", Typeface.NORMAL)
                com.android.snippets.viewmodel.SnippetStyle.Mono ->
                    Typeface.MONOSPACE
                com.android.snippets.viewmodel.SnippetStyle.Serif ->
                    Typeface.SERIF
                com.android.snippets.viewmodel.SnippetStyle.Bold ->
                    Typeface.create("sans-serif-black", Typeface.NORMAL)
                com.android.snippets.viewmodel.SnippetStyle.Spaced ->
                    getCustomTypeface(context, "'wght' 700, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                com.android.snippets.viewmodel.SnippetStyle.FlexHeavy ->
                    getCustomTypeface(context, "'wght' 1000, 'ROND' 100") ?: Typeface.create("sans-serif-black", Typeface.NORMAL)
                com.android.snippets.viewmodel.SnippetStyle.FlexWide ->
                    getCustomTypeface(context, "'wdth' 151, 'ROND' 100") ?: Typeface.create("sans-serif", Typeface.NORMAL)
                com.android.snippets.viewmodel.SnippetStyle.FlexSlant ->
                    getCustomTypeface(context, "'slnt' -10, 'ROND' 100") ?: Typeface.create("sans-serif", Typeface.ITALIC)
                com.android.snippets.viewmodel.SnippetStyle.FlexGrade ->
                    getCustomTypeface(context, "'GRAD' 100, 'ROND' 100") ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)
                else ->
                    getCustomTypeface(context, "'wght' 700, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            
            val letterSpacingVal = when(forcedStyle) {
                com.android.snippets.viewmodel.SnippetStyle.Spaced -> 0.12f
                com.android.snippets.viewmodel.SnippetStyle.FlexWide -> 0.05f
                else -> 0f
            }

            val baseFontSize = when (personality) {
                0, 1 -> 16f
                2, 3 -> 13f
                else -> 11f
            }
            var size = (baseFontSize * scalingFactor) * scaleFactor
            
            textPaint.typeface = typeface
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                textPaint.letterSpacing = letterSpacingVal
            }
            
            textPaint.textSize = size
            val bounds = Rect()
            textPaint.getTextBounds(trimmedText, 0, trimmedText.length, bounds)
            var pillW = bounds.width() + (textPaint.textSize * 1.4f)
            val pillH = bounds.height() + (textPaint.textSize * 1.0f)
            
            if (pillW > maxRowWidth) {
                val downscaleRatio = maxRowWidth / pillW
                size *= downscaleRatio
                textPaint.textSize = size
                textPaint.getTextBounds(trimmedText, 0, trimmedText.length, bounds)
                pillW = bounds.width() + (textPaint.textSize * 1.4f)
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
            
            currentRow.add(PlacedPill(trimmedText, pillW, pillH, boundingW, boundingH, size, baseColor, rotation, bounds, typeface, letterSpacingVal, isFilled))
            currentRowWidth += boundingW + (if (currentRow.size > 1) spacingX else 0f)
            currentRowHeight = maxOf(currentRowHeight, boundingH)
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowHeights.add(currentRowHeight)
        }
        
        val snippetAreaTop = cy + radius + (24f * scaleFactor)
        val snippetAreaBottom = height - (36f * scaleFactor)
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
                
                // Draw Pill Background
                val pillPath = android.graphics.Path().apply {
                    val r = scaledH / 2f
                    addRoundRect(
                        android.graphics.RectF(0f, 0f, scaledW, scaledH),
                        r, r,
                        android.graphics.Path.Direction.CW
                    )
                    offset(x - (scaledW / 2f), y - (scaledH / 2f))
                }
                
                pillPaint.style = Paint.Style.FILL
                pillPaint.color = pill.color
                pillPaint.alpha = (255 * 0.25f).toInt()
                canvas.drawPath(pillPath, pillPaint)

                // Draw Text with clean color and no persistent shader corruption
                textPaint.shader = null // Clear any shader
                textPaint.color = pill.color
                textPaint.textSize = pill.textSize * fitScale
                textPaint.typeface = pill.typeface
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    textPaint.letterSpacing = pill.spacing
                }
                
                val currentBounds = Rect()
                textPaint.getTextBounds(pill.text, 0, pill.text.length, currentBounds)
                val fontMetrics = textPaint.fontMetrics
                val textY = y - (fontMetrics.descent + fontMetrics.ascent) / 2f
                canvas.drawText(pill.text, x, textY, textPaint)
                
                canvas.restore()
                
                currentX += scaledBoundingW + scaledSpacingX
            }
            currentY += rowHeight + scaledSpacingY
        }
        
        return bitmap
    }

    private fun runBlockingCoil(context: Context, uri: Uri): Bitmap? {
        return try {
            val loader = coil.ImageLoader(context)
            val request = coil.request.ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false) // Required for Canvas drawing
                .build()
            val result = kotlinx.coroutines.runBlocking { loader.execute(request) }
            (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
        val matrix = android.graphics.Matrix()
        matrix.postScale(scale, scale)
        matrix.postTranslate((targetWidth - scaledW) * 0.5f, (targetHeight - scaledH) * 0.5f)
        canvas.drawBitmap(src, matrix, null)
        
        // Downsample to 8% for smooth interpolation
        val tinyW = (targetWidth * 0.08f).toInt().coerceAtLeast(8)
        val tinyH = (targetHeight * 0.08f).toInt().coerceAtLeast(8)
        val tiny = Bitmap.createScaledBitmap(cropped, tinyW, tinyH, true)
        
        // Apply box blur on the tiny image to make it silky smooth
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
        
        val dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        dest.setPixels(finalPixels, 0, width, 0, 0, width, height)
        return dest
    }
}
