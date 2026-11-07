package com.android.snippets.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import androidx.graphics.shapes.toPath
import com.android.snippets.model.Photo
import com.android.snippets.ui.shapes.CookiePolygon
import com.android.snippets.ui.theme.getSnippetBackgroundColorInt
import com.android.snippets.ui.theme.getSnippetTextColorInt
import com.android.snippets.ui.util.LocationUtils
import com.ln.android.snippets.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MemoryNotificationRenderer {

    private val cachedFonts = mutableMapOf<String, Typeface>()

    fun getCustomTypeface(context: Context, variationSettings: String): Typeface? {
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
        } catch (_: Exception) {
            null
        }
    }

    fun createMemoryCardBitmap(context: Context, photo: Photo, photoBitmap: Bitmap): Bitmap {
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

            // 2. Cookie Shape (Left aligned and bigger)
            val cookiePadding = 36f
            val cookieDiameter = targetHeight - cookiePadding * 2f // 504px diameter
            val radius = cookieDiameter / 2f
            val cx = cookiePadding + radius // 288px
            val cy = targetHeight / 2f // 288px

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

            // Draw photo clipped to cookie shape
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

            // Draw crisp white border around cookie
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 7f
                color = Color.WHITE
            }
            canvas.drawPath(path, borderPaint)

            // Right Area definition
            val rightAreaLeft = cx + radius + 36f // 576px
            val rightAreaRight = targetWidth - 36f // 988px
            val rightAreaWidth = rightAreaRight - rightAreaLeft // 412px
            val rightCenterX = (rightAreaLeft + rightAreaRight) / 2f // 782px

            // 3. Metadata Pills on the Right Center Top (Enlarged)
            val pillsTopY = 36f
            val pillH = 44f
            val pillRadius = pillH / 2f
            val pillSpacingX = 10f
            val pillSpacingY = 10f
            val calHorizontalPadding = 18f
            val iconSize = 22f
            val itemSpacing = 8f

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

            fun drawPill(
                left: Float,
                top: Float,
                width: Float,
                drawableRes: Int,
                text: String,
                paint: android.text.TextPaint
            ) {
                val rect = RectF(left, top, left + width, top + pillH)
                canvas.drawRoundRect(rect, pillRadius, pillRadius, pillBgPaint)
                canvas.drawRoundRect(rect, pillRadius, pillRadius, pillBorderPaint)

                val drawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, drawableRes, null)
                val iconX = left + calHorizontalPadding
                val iconY = top + (pillH - iconSize) / 2f
                if (drawable != null) {
                    drawable.setBounds(iconX.toInt(), iconY.toInt(), (iconX + iconSize).toInt(), (iconY + iconSize).toInt())
                    val tinted = drawable.mutate()
                    androidx.core.graphics.drawable.DrawableCompat.setTint(tinted, Color.WHITE)
                    tinted.draw(canvas)
                }

                val availTextW = (width - (calHorizontalPadding * 2 + iconSize + itemSpacing)).coerceAtLeast(0f)
                val ellipText = android.text.TextUtils.ellipsize(text, paint, availTextW, android.text.TextUtils.TruncateAt.END).toString()
                val textY = top + (pillH / 2f) - ((paint.descent() + paint.ascent()) / 2f)
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(ellipText, iconX + iconSize + itemSpacing, textY, paint)
            }

            fun drawRatingStar(left: Float, top: Float, rating: Int) {
                val starSize = 38f
                val starDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.ic_star_rating,
                    null
                )
                if (starDrawable != null) {
                    val starTop = top + (pillH - starSize) / 2f
                    starDrawable.setBounds(left.toInt(), starTop.toInt(), (left + starSize).toInt(), (starTop + starSize).toInt())
                    val tinted = starDrawable.mutate()
                    androidx.core.graphics.drawable.DrawableCompat.setTint(tinted, Color.WHITE)
                    tinted.draw(canvas)

                    val ratingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#1F1F1F")
                        textSize = 15f
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    val textX = left + starSize / 2f
                    val textY = starTop + starSize / 2f + (ratingPaint.textSize / 3f)
                    canvas.drawText(rating.toString(), textX, textY, ratingPaint)
                }
            }

            // Prepare Date & Time text
            val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
            val timeFormat = if (is24Hour) "HH:mm" else "h:mm a"
            val pattern = "EEE, d MMM • $timeFormat"
            val dateString = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(photo.date)).uppercase()
            val metaPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 240
                textSize = 18f
                typeface = getCustomTypeface(context, "'wght' 600, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val hasRating = photo.rating > 0
            val starSize = 38f
            val dateTextWidth = metaPaint.measureText(dateString)
            val datePillW = calHorizontalPadding * 2 + iconSize + itemSpacing + dateTextWidth
            val maxRow1DateW = if (hasRating) rightAreaWidth - starSize - pillSpacingX else rightAreaWidth
            val actualDatePillW = datePillW.coerceAtMost(maxRow1DateW)

            // Row 1: Date & Time (+ Star Rating if present), centered horizontally
            val row1TotalW = if (hasRating) actualDatePillW + pillSpacingX + starSize else actualDatePillW
            val row1Left = rightCenterX - (row1TotalW / 2f)
            drawPill(row1Left, pillsTopY, actualDatePillW, R.drawable.ic_calendar_month, dateString, metaPaint)
            if (hasRating) {
                drawRatingStar(row1Left + actualDatePillW + pillSpacingX, pillsTopY, photo.rating)
            }

            // Collect other metadata pills (Location, Photo Name)
            val locationText = LocationUtils.getLocationFromExif(context, photo) ?: photo.locationName
            val hasLocation = !locationText.isNullOrBlank()
            val photoName = photo.name?.trim()?.takeIf { it.isNotBlank() }
            val hasPhotoName = photoName != null

            val locPillW = if (hasLocation) {
                (calHorizontalPadding * 2 + iconSize + itemSpacing + metaPaint.measureText(locationText!!)).coerceAtMost(rightAreaWidth)
            } else 0f

            val namePillW = if (hasPhotoName) {
                (calHorizontalPadding * 2 + iconSize + itemSpacing + metaPaint.measureText(photoName!!)).coerceAtMost(rightAreaWidth)
            } else 0f

            var nextRowY = pillsTopY + pillH + pillSpacingY
            var lastPillBottom = pillsTopY + pillH

            if (hasLocation && hasPhotoName) {
                if (locPillW + pillSpacingX + namePillW <= rightAreaWidth) {
                    val rowW = locPillW + pillSpacingX + namePillW
                    val rowLeft = rightCenterX - (rowW / 2f)
                    drawPill(rowLeft, nextRowY, locPillW, R.drawable.ic_location_on, locationText!!, metaPaint)
                    drawPill(rowLeft + locPillW + pillSpacingX, nextRowY, namePillW, R.drawable.ic_label, photoName!!, metaPaint)
                    lastPillBottom = nextRowY + pillH
                } else {
                    val row2Left = rightCenterX - (locPillW / 2f)
                    drawPill(row2Left, nextRowY, locPillW, R.drawable.ic_location_on, locationText!!, metaPaint)
                    nextRowY += pillH + pillSpacingY
                    val row3Left = rightCenterX - (namePillW / 2f)
                    drawPill(row3Left, nextRowY, namePillW, R.drawable.ic_label, photoName!!, metaPaint)
                    lastPillBottom = nextRowY + pillH
                }
            } else if (hasLocation) {
                val row2Left = rightCenterX - (locPillW / 2f)
                drawPill(row2Left, nextRowY, locPillW, R.drawable.ic_location_on, locationText!!, metaPaint)
                lastPillBottom = nextRowY + pillH
            } else if (hasPhotoName) {
                val row2Left = rightCenterX - (namePillW / 2f)
                drawPill(row2Left, nextRowY, namePillW, R.drawable.ic_label, photoName!!, metaPaint)
                lastPillBottom = nextRowY + pillH
            }

            // 4. Snippets Stacked Vertically Below Them (Enlarged and Center Aligned)
            val snippets = photo.snippets
            if (snippets.isNotEmpty()) {
                val snippetAreaTop = lastPillBottom + 20f
                val snippetAreaBottom = targetHeight - 36f
                val snippetAreaHeight = (snippetAreaBottom - snippetAreaTop).coerceAtLeast(0f)

                val chipHeight = 46f
                val chipRadius = chipHeight / 2f
                val chipPaddingX = 20f
                val chipSpacing = 12f
                val maxChipW = rightAreaWidth

                val textColor = getSnippetTextColorInt(context, isDark = true)
                val bgColor = getSnippetBackgroundColorInt(context, isDark = true)

                val textPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textColor
                    textSize = 20f
                    typeface = getCustomTypeface(context, "'wght' 550, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = bgColor
                    style = Paint.Style.FILL
                }

                // Stacked vertically, center aligned in the right pane
                val maxVisible = ((snippetAreaHeight + chipSpacing) / (chipHeight + chipSpacing)).toInt().coerceAtLeast(1)
                val visibleSnippets = snippets.take(maxVisible)
                val totalStackH = visibleSnippets.size * chipHeight + (visibleSnippets.size - 1) * chipSpacing
                val startY = snippetAreaTop + (snippetAreaHeight - totalStackH) / 2f

                visibleSnippets.forEachIndexed { index, snippet ->
                    val textW = textPaint.measureText(snippet)
                    val chipW = (textW + chipPaddingX * 2).coerceAtMost(maxChipW)
                    // Center aligned in the right pane
                    val chipX = rightCenterX - (chipW / 2f)
                    val chipY = startY + index * (chipHeight + chipSpacing)

                    val rect = RectF(chipX, chipY, chipX + chipW, chipY + chipHeight)
                    canvas.drawRoundRect(rect, chipRadius, chipRadius, bgPaint)

                    val availTextW = (chipW - chipPaddingX * 2).coerceAtLeast(0f)
                    val ellipSnippet = android.text.TextUtils.ellipsize(snippet, textPaint, availTextW, android.text.TextUtils.TruncateAt.END).toString()
                    val textY = chipY + chipHeight / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f)
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText(ellipSnippet, chipX + chipW / 2f, textY, textPaint)
                }
            }

            output
        } catch (e: Exception) {
            e.printStackTrace()
            createMemoryCardBitmap(photoBitmap)
        }
    }

    fun createMemoryCardBitmap(photoBitmap: Bitmap): Bitmap {
        val targetWidth = 1024
        val targetHeight = 576 // 16:9 standard notification banner ratio

        return try {
            val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val blurredBg = createBlurredBackground(photoBitmap, targetWidth, targetHeight)
            canvas.drawBitmap(blurredBg, 0f, 0f, null)
            blurredBg.recycle()

            val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                alpha = (255 * 0.5f).toInt()
            }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), dimPaint)

            val cookiePadding = 36f
            val cookieDiameter = targetHeight - cookiePadding * 2f
            val radius = cookieDiameter / 2f
            val cx = cookiePadding + radius
            val cy = targetHeight / 2f

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

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 7f
                color = Color.WHITE
            }
            canvas.drawPath(path, borderPaint)

            output
        } catch (e: Exception) {
            e.printStackTrace()
            photoBitmap
        }
    }

    fun createBlurredBackground(src: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap =
        com.android.snippets.ui.util.BitmapBlurHelper.createBlurredBackground(src, targetWidth, targetHeight)
}
