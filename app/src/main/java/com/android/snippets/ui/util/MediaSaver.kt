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
import kotlinx.coroutines.Dispatchers
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

    suspend fun saveSnippetToGallery(context: Context, photo: Photo, snippets: List<String>, isDark: Boolean = false, bgColor: Int = Color.WHITE, snippetColors: Map<String, Int> = emptyMap(), snippetStyles: Map<String, com.android.snippets.viewmodel.SnippetStyle> = emptyMap(), snippetShapes: Map<String, com.android.snippets.viewmodel.SnippetShape> = emptyMap()): Boolean = withContext(Dispatchers.IO) {
        try {
            val bitmap = createSnippetBitmap(context, photo, snippets, isDark, bgColor, snippetColors, snippetStyles, snippetShapes) ?: return@withContext false
            
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

    suspend fun getShareableUri(context: Context, photo: Photo, snippets: List<String>, isDark: Boolean = false, bgColor: Int = Color.WHITE, snippetColors: Map<String, Int> = emptyMap(), snippetStyles: Map<String, com.android.snippets.viewmodel.SnippetStyle> = emptyMap(), snippetShapes: Map<String, com.android.snippets.viewmodel.SnippetShape> = emptyMap()): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = createSnippetBitmap(context, photo, snippets, isDark, bgColor, snippetColors, snippetStyles, snippetShapes) ?: return@withContext null
            
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

    private fun createSnippetBitmap(context: Context, photo: Photo, snippets: List<String>, isDark: Boolean, bgColor: Int, snippetColors: Map<String, Int>, snippetStyles: Map<String, com.android.snippets.viewmodel.SnippetStyle>, snippetShapes: Map<String, com.android.snippets.viewmodel.SnippetShape> = emptyMap()): Bitmap? {
        val width = 1440
        val height = 2560
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 1. Draw Background (Fallback)
        canvas.drawColor(bgColor)
        
        // 2. Draw Immersive Photo (Edge-to-Edge)
        val photoBitmap = runBlockingCoil(context, photo.uri)
        val centerX = width / 2f
        
        if (photoBitmap != null) {
            val srcWidth = photoBitmap.width.toFloat()
            val srcHeight = photoBitmap.height.toFloat()
            
            // Calculate ContentScale.Crop equivalent
            val scaleX = width / srcWidth
            val scaleY = height / srcHeight
            val scale = maxOf(scaleX, scaleY)
            
            val scaledWidth = srcWidth * scale
            val scaledHeight = srcHeight * scale
            
            // Focus adjustments (similar to MemoryScreen)
            val focusOffsetX = ((width - scaledWidth) * 0.5f).coerceIn((width - scaledWidth).coerceAtMost(0f), (width - scaledWidth).coerceAtLeast(0f))
            val focusOffsetY = ((height - scaledHeight) * 0.5f).coerceIn((height - scaledHeight).coerceAtMost(0f), (height - scaledHeight).coerceAtLeast(0f))
            
            val matrix = android.graphics.Matrix()
            matrix.postScale(scale, scale)
            matrix.postTranslate(focusOffsetX, focusOffsetY)
            
            canvas.drawBitmap(photoBitmap, matrix, null)
            
            // Draw Dark Gradient Scrim at bottom (e.g. from y = 1200 to 2560)
            val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, height - 1200f, 0f, height.toFloat(),
                    Color.TRANSPARENT, Color.parseColor("#E6000000"), // 90% black
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, height - 1200f, width.toFloat(), height.toFloat(), scrimPaint)
        }

        // 3. Draw Date Header (With Shadow for visibility)
        val dateString = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(photo.date)).uppercase()
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 230
            textSize = 34f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.15f
            setShadowLayer(8f, 0f, 4f, Color.argb(180, 0, 0, 0))
        }
        canvas.drawText(dateString, width / 2f, 200f, datePaint)

        // 4. Draw Snippets (Synchronized with ExpressiveShareSheet logic)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        val scaleFactor = width / 360f // Baseline for DP to PX scaling
        
        data class PlacedPill(val text: String, val w: Float, val h: Float, val boundingW: Float, val boundingH: Float, val textSize: Float, val color: Int, val rot: Float, val textBounds: Rect, val typeface: Typeface, val spacing: Float, val shape: com.android.snippets.viewmodel.SnippetShape, val isFilled: Boolean)
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
            val isFilled = (personality % 2 == 0)
            
            val rotation = stableRandom.nextInt(-15, 15).toFloat()
            
            val total = snippets.size
            val scalingFactor = com.android.snippets.ui.util.DistributionMath.getCloudScalingFactor(total)
            
            val forcedShape = snippetShapes[trimmedText] ?: com.android.snippets.viewmodel.SnippetShape.Default
            
            val forcedStyle = snippetStyles[trimmedText] ?: com.android.snippets.viewmodel.SnippetStyle.Default
            
            val typeface = when(forcedStyle) {
                com.android.snippets.viewmodel.SnippetStyle.Thin -> Typeface.create("sans-serif-thin", Typeface.NORMAL)
                com.android.snippets.viewmodel.SnippetStyle.Cursive -> Typeface.create("cursive", Typeface.NORMAL)
                com.android.snippets.viewmodel.SnippetStyle.Mono -> Typeface.MONOSPACE
                com.android.snippets.viewmodel.SnippetStyle.Serif -> Typeface.SERIF
                com.android.snippets.viewmodel.SnippetStyle.Bold -> Typeface.create("sans-serif-black", Typeface.NORMAL)
                com.android.snippets.viewmodel.SnippetStyle.Spaced -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                com.android.snippets.viewmodel.SnippetStyle.FlexHeavy -> Typeface.create("sans-serif-black", Typeface.NORMAL)
                com.android.snippets.viewmodel.SnippetStyle.FlexWide -> Typeface.create("sans-serif", Typeface.NORMAL)
                com.android.snippets.viewmodel.SnippetStyle.FlexSlant -> Typeface.create("sans-serif", Typeface.ITALIC)
                com.android.snippets.viewmodel.SnippetStyle.FlexGrade -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
                else -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
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
            
            currentRow.add(PlacedPill(text, pillW, pillH, boundingW, boundingH, size, colorInt, rotation, bounds, typeface, letterSpacingVal, forcedShape, isFilled))
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
                    pillPaint.alpha = (255 * 0.10f).toInt()
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
}
