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

            // 2. Draw Top Header: Rating star, Calendar & Date, Photo Name, Location
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

            // Center Pill: Calendar month icon + Day, Date and Time
            val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
            val timeFormat = if (is24Hour) "HH:mm" else "h:mm a"
            val pattern = "EEE, d MMM • $timeFormat"
            val dateString = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(photo.date)).uppercase()
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
                val nameTextWidth = namePaint.measureText(photoName)
                val leftPillW = (calHorizontalPadding * 2 + labelIconSize + itemSpacing + nameTextWidth).coerceAtMost(centerPillLeft - paddingX - 16f)

                if (leftPillW > (calHorizontalPadding * 2 + labelIconSize + itemSpacing)) {
                    val leftPillRect = RectF(paddingX, centerPillTop, paddingX + leftPillW, centerPillTop + pillH)
                    canvas.drawRoundRect(leftPillRect, pillRadius, pillRadius, pillBgPaint)
                    canvas.drawRoundRect(leftPillRect, pillRadius, pillRadius, pillBorderPaint)

                    val labelDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, R.drawable.ic_label, null)
                    val labelIconX = paddingX + calHorizontalPadding
                    val labelIconY = headerCenterY - (labelIconSize / 2f)
                    if (labelDrawable != null) {
                        labelDrawable.setBounds(labelIconX.toInt(), labelIconY.toInt(), (labelIconX + labelIconSize).toInt(), (labelIconY + labelIconSize).toInt())
                        val tinted = labelDrawable.mutate()
                        androidx.core.graphics.drawable.DrawableCompat.setTint(tinted, Color.WHITE)
                        tinted.draw(canvas)
                    }

                    val availTextW = leftPillW - (calHorizontalPadding * 2 + labelIconSize + itemSpacing)
                    val ellipName = android.text.TextUtils.ellipsize(photoName, namePaint, availTextW, android.text.TextUtils.TruncateAt.END).toString()
                    val nameTextY = headerCenterY - ((namePaint.descent() + namePaint.ascent()) / 2f)
                    namePaint.textAlign = Paint.Align.LEFT
                    canvas.drawText(ellipName, labelIconX + labelIconSize + itemSpacing, nameTextY, namePaint)
                }
            }

            // Right Pill: Location
            val locationText = LocationUtils.getLocationFromExif(context, photo) ?: photo.locationName
            if (!locationText.isNullOrBlank()) {
                val locPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    alpha = 240
                    textSize = 17f
                    typeface = getCustomTypeface(context, "'wght' 600, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                val locIconSize = 18f
                val locTextWidth = locPaint.measureText(locationText)
                val maxRightW = targetWidth - (centerPillLeft + centerPillW + 16f) - paddingX
                val rightPillW = (calHorizontalPadding * 2 + locIconSize + itemSpacing + locTextWidth).coerceAtMost(maxRightW)

                if (rightPillW > (calHorizontalPadding * 2 + locIconSize + itemSpacing)) {
                    val rightPillLeft = targetWidth - paddingX - rightPillW
                    val rightPillRect = RectF(rightPillLeft, centerPillTop, rightPillLeft + rightPillW, centerPillTop + pillH)
                    canvas.drawRoundRect(rightPillRect, pillRadius, pillRadius, pillBgPaint)
                    canvas.drawRoundRect(rightPillRect, pillRadius, pillRadius, pillBorderPaint)

                    val locDrawable = androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, R.drawable.ic_location_on, null)
                    val locIconX = rightPillLeft + calHorizontalPadding
                    val locIconY = headerCenterY - (locIconSize / 2f)
                    if (locDrawable != null) {
                        locDrawable.setBounds(locIconX.toInt(), locIconY.toInt(), (locIconX + locIconSize).toInt(), (locIconY + locIconSize).toInt())
                        val tinted = locDrawable.mutate()
                        androidx.core.graphics.drawable.DrawableCompat.setTint(tinted, Color.WHITE)
                        tinted.draw(canvas)
                    }

                    val availLocW = rightPillW - (calHorizontalPadding * 2 + locIconSize + itemSpacing)
                    val ellipLoc = android.text.TextUtils.ellipsize(locationText, locPaint, availLocW, android.text.TextUtils.TruncateAt.END).toString()
                    val locTextY = headerCenterY - ((locPaint.descent() + locPaint.ascent()) / 2f)
                    locPaint.textAlign = Paint.Align.LEFT
                    canvas.drawText(ellipLoc, locIconX + locIconSize + itemSpacing, locTextY, locPaint)
                }
            }

            // 3. Cookie Shape Center
            val cookieDiameter = if (hasRating) targetHeight * 0.58f else targetHeight * 0.62f
            val cx = targetWidth / 2f
            val cy = (headerCenterY + pillH / 2f) + 12f + (cookieDiameter / 2f)
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
                strokeWidth = 6f
                color = Color.WHITE
            }
            canvas.drawPath(path, borderPaint)

            // 4. Draw Snippets Cloud Beneath Cookie
            val snippets = photo.snippets
            if (snippets.isNotEmpty()) {
                val chipHeight = 36f
                val chipRadius = chipHeight / 2f
                val chipPaddingX = 14f
                val chipSpacing = 8f

                val snippetPaints = snippets.map { s ->
                    val textColor = getSnippetTextColorInt(context, isDark = true)
                    val bgColor = getSnippetBackgroundColorInt(context, isDark = true)
                    val textPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = textColor
                        textSize = 15f
                        typeface = getCustomTypeface(context, "'wght' 550, 'ROND' 100") ?: Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    }
                    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = bgColor
                        style = Paint.Style.FILL
                    }
                    val textW = textPaint.measureText(s)
                    val chipW = textW + chipPaddingX * 2
                    Triple(s, chipW, Pair(bgPaint, textPaint))
                }

                val row1 = mutableListOf<Triple<String, Float, Pair<Paint, android.text.TextPaint>>>()
                val row2 = mutableListOf<Triple<String, Float, Pair<Paint, android.text.TextPaint>>>()
                var currentW = 0f
                val maxRowW = targetWidth - paddingX * 2

                for (chip in snippetPaints) {
                    val addedW = if (currentW == 0f) chip.second else chip.second + chipSpacing
                    if (currentW + addedW <= maxRowW) {
                        row1.add(chip)
                        currentW += addedW
                    } else if (row2.isEmpty() || (row2.map { it.second }.sum() + (row2.size * chipSpacing) + chip.second <= maxRowW)) {
                        row2.add(chip)
                    } else {
                        break
                    }
                }

                val rows = listOf(row1, row2).filter { it.isNotEmpty() }
                val totalRowsH = rows.size * chipHeight + (rows.size - 1) * chipSpacing
                val cookieBottom = cy + radius
                val availableSpace = targetHeight - cookieBottom
                val startY = cookieBottom + (availableSpace - totalRowsH) / 2f

                rows.forEachIndexed { rIdx, rowChips ->
                    val rowTotalW = rowChips.map { it.second }.sum() + (rowChips.size - 1) * chipSpacing
                    var chipX = (targetWidth - rowTotalW) / 2f
                    val chipY = startY + rIdx * (chipHeight + chipSpacing)

                    for ((text, chipW, paints) in rowChips) {
                        val rect = RectF(chipX, chipY, chipX + chipW, chipY + chipHeight)
                        canvas.drawRoundRect(rect, chipRadius, chipRadius, paints.first)

                        val textPaint = paints.second
                        val textY = chipY + chipHeight / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f)
                        textPaint.textAlign = Paint.Align.CENTER
                        canvas.drawText(text, chipX + chipW / 2f, textY, textPaint)

                        chipX += chipW + chipSpacing
                    }
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

    fun createBlurredBackground(src: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap =
        com.android.snippets.ui.util.BitmapBlurHelper.createBlurredBackground(src, targetWidth, targetHeight)
}
