package com.android.snippets.ui.util

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.asAndroidPath
import androidx.core.content.FileProvider
import com.android.snippets.model.Photo
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

    suspend fun saveSnippetToGallery(context: Context, photo: Photo, snippets: List<String>, isDark: Boolean = false, bgColor: Int = Color.WHITE, snippetColors: Map<String, Int> = emptyMap(), snippetStyles: Map<String, com.android.snippets.viewmodel.SnippetStyle> = emptyMap(), appShape: AppShape = AppShape.COOKIE_12_SIDED, showTime: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        try {
            val bitmap = createSnippetBitmap(context, photo, snippets, isDark, bgColor, snippetColors, snippetStyles, appShape, showTime) ?: return@withContext false
            
            val fileName = "Snippet_Card_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Snippets")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            imageUri?.let { uri ->
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
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

    suspend fun getShareableUri(context: Context, photo: Photo, snippets: List<String>, isDark: Boolean = false, bgColor: Int = Color.WHITE, snippetColors: Map<String, Int> = emptyMap(), snippetStyles: Map<String, com.android.snippets.viewmodel.SnippetStyle> = emptyMap(), appShape: AppShape = AppShape.COOKIE_12_SIDED, showTime: Boolean = false): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = createSnippetBitmap(context, photo, snippets, isDark, bgColor, snippetColors, snippetStyles, appShape, showTime) ?: return@withContext null
            
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "share_snippet.jpg")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            stream.close()
            bitmap.recycle()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createSnippetBitmap(context: Context, photo: Photo, snippets: List<String>, isDark: Boolean, bgColor: Int, snippetColors: Map<String, Int>, snippetStyles: Map<String, com.android.snippets.viewmodel.SnippetStyle>, appShape: AppShape = AppShape.COOKIE_12_SIDED, showTime: Boolean = false): Bitmap? {
        val width = 1440
        val height = 2560
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val photoBitmap = runBlockingCoil(context, photo.uri)

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
        val cy = 924f
        val radius = 624f
        val targetSize = 2 * radius // 1248f
        
        val normalizedPolygon = appShape.getNormalizedPolygon()
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

        // 3. Draw Date & Location Header
        val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
        val timeFormat = if (is24Hour) "HH:mm" else "h:mm a"
        val datePattern = if (showTime) "EEE, d MMM • $timeFormat" else "EEE, d MMM"
        val dateString = SimpleDateFormat(datePattern, Locale.getDefault()).format(Date(photo.date)).uppercase()
        val locationText = LocationUtils.getLocationFromExif(context, photo)
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 230
            textSize = 34f
            typeface = getCustomTypeface(context, "'wght' 700, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                letterSpacing = 0.15f
            }
        }

        if (locationText.isNullOrBlank()) {
            datePaint.textAlign = Paint.Align.CENTER
            canvas.drawText(dateString, width / 2f, 200f, datePaint)
        } else {
            datePaint.textAlign = Paint.Align.LEFT
            val dateWidth = datePaint.measureText(dateString)

            val locTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 240
                textSize = 26f
                typeface = getCustomTypeface(context, "'wght' 600, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val locTextWidth = locTextPaint.measureText(locationText)

            val pillW = locTextWidth + 64f
            val pillH = 48f
            val spacing = 24f
            val totalHeaderWidth = dateWidth + spacing + pillW
            val startX = (width - totalHeaderWidth) / 2f

            canvas.drawText(dateString, startX, 200f, datePaint)

            val pillLeft = startX + dateWidth + spacing
            val pillTop = 162f
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
                alpha = (255 * 0.4f).toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBgPaint)
            canvas.drawRoundRect(pillRect, pillRadius, pillRadius, pillBorderPaint)

            val dotX = pillLeft + 24f
            val dotY = pillRect.centerY()
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(dotX, dotY, 5f, dotPaint)

            val textCenterX = pillLeft + 44f + (locTextWidth / 2f)
            locTextPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(locationText, textCenterX, pillRect.centerY() + 9f, locTextPaint)
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
            val forcedColor = snippetColors[text.trim()]
            val baseColor = if (forcedColor != null) {
                forcedColor
            } else {
                val stableRandom = Random(text.hashCode().toLong())
                val colorStrategy = (index + stableRandom.nextInt(10)) % 3
                when (colorStrategy) {
                    0 -> { // Fallback Theme
                        Color.parseColor(if (isDark) "#D0BCFF" else "#6750A4")
                    }
                    1 -> { // System Theme Mix
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
                    else -> { // Vivid Pop
                        val vivids = if (isDark) intArrayOf(
                            Color.parseColor("#FF8A65"), Color.parseColor("#F06292"), 
                            Color.parseColor("#BA68C8"), Color.parseColor("#4DD0E1")
                        ) else intArrayOf(
                            Color.parseColor("#D84315"), Color.parseColor("#C2185B"), 
                            Color.parseColor("#7B1FA2"), Color.parseColor("#0097A7")
                        )
                        vivids[stableRandom.nextInt(vivids.size)]
                    }
                }
            }

            // Contrast check for Bitmap drawing
            val colorInt = baseColor.let { c ->
                val r = Color.red(c) / 255f
                val g = Color.green(c) / 255f
                val b = Color.blue(c) / 255f
                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                if (isDark && lum < 0.3f) {
                    Color.rgb((r + 0.4f).coerceAtMost(1f), (g + 0.4f).coerceAtMost(1f), (b + 0.4f).coerceAtMost(1f))
                } else if (!isDark && lum > 0.7f) {
                    Color.rgb((r - 0.4f).coerceAtLeast(0f), (g - 0.4f).coerceAtLeast(0f), (b - 0.4f).coerceAtLeast(0f))
                } else c
            }

            val trimmedText = text.trim()
            val stableRandom = Random(trimmedText.hashCode())
            val personality = stableRandom.nextInt(0, 5)
            val isFilled = true
            
            val rotation = stableRandom.nextInt(-15, 15).toFloat()
            
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
                textPaint.color = pill.color
                textPaint.textSize = pill.textSize * fitScale
                textPaint.typeface = pill.typeface
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    textPaint.letterSpacing = pill.spacing
                }
                val alphaColor = Color.argb(140, Color.red(pill.color), Color.green(pill.color), Color.blue(pill.color))
                textPaint.shader = android.graphics.LinearGradient(x - (scaledW / 2f), y, x + (scaledW / 2f), y, intArrayOf(pill.color, alphaColor), null, android.graphics.Shader.TileMode.CLAMP)
                
                val pillPath = android.graphics.Path().apply {
                    val radius = scaledH / 2f
                    addRoundRect(
                        android.graphics.RectF(0f, 0f, scaledW, scaledH),
                        radius, radius,
                        android.graphics.Path.Direction.CW
                    )
                    offset(x - (scaledW / 2f), y - (scaledH / 2f))
                }
                
                if (pill.isFilled) {
                    pillPaint.style = Paint.Style.FILL
                    pillPaint.color = pill.color
                    pillPaint.alpha = (255 * 0.25f).toInt()
                    canvas.drawPath(pillPath, pillPaint)
                } else {
                    pillPaint.style = Paint.Style.STROKE
                    pillPaint.strokeWidth = 1.5f * fitScale * scaleFactor
                    pillPaint.color = pill.color
                    pillPaint.alpha = (255 * 0.40f).toInt()
                    canvas.drawPath(pillPath, pillPaint)
                }
                canvas.drawText(pill.text, x, y + ((pill.textBounds.height() * fitScale) / 3f), textPaint)
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
