package com.android.snippets.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader

object BitmapBlurHelper {

    fun createBlurredBackground(src: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val srcWidth = src.width.toFloat()
        val srcHeight = src.height.toFloat()
        // Downsample directly to 20% to avoid allocating an 8.3MB intermediate bitmap
        val blurW = (targetWidth * 0.20f).toInt().coerceAtLeast(16)
        val blurH = (targetHeight * 0.20f).toInt().coerceAtLeast(16)
        val scaleX = blurW / srcWidth
        val scaleY = blurH / srcHeight
        val scale = maxOf(scaleX, scaleY) * 1.08f
        val scaledW = (srcWidth * scale).toInt().coerceAtLeast(1)
        val scaledH = (srcHeight * scale).toInt().coerceAtLeast(1)

        val downscaled = Bitmap.createBitmap(blurW, blurH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(downscaled)
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        matrix.postTranslate((blurW - scaledW) * 0.5f, (blurH - scaledH) * 0.5f)
        canvas.drawBitmap(src, matrix, null)

        val lightBlurred = gaussianBlur(downscaled, 4, 3)
        val deepBlurred = gaussianBlur(downscaled, 14, 3)
        downscaled.recycle()

        val composite = Bitmap.createBitmap(blurW, blurH, Bitmap.Config.ARGB_8888)
        val compCanvas = Canvas(composite)
        compCanvas.drawBitmap(lightBlurred, 0f, 0f, null)

        val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, blurH.toFloat(),
                Color.TRANSPARENT, Color.BLACK,
                Shader.TileMode.CLAMP
            )
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        val deepCanvas = Canvas(deepBlurred)
        deepCanvas.drawRect(0f, 0f, blurW.toFloat(), blurH.toFloat(), gradientPaint)

        compCanvas.drawBitmap(deepBlurred, 0f, 0f, null)
        lightBlurred.recycle()
        deepBlurred.recycle()

        val blurredFull = Bitmap.createScaledBitmap(composite, targetWidth, targetHeight, true)
        composite.recycle()
        return blurredFull
    }

    fun gaussianBlur(src: Bitmap, range: Int, passes: Int = 3): Bitmap {
        var current = src
        var isFirst = true
        repeat(passes) {
            val next = boxBlur(current, range)
            if (!isFirst) current.recycle()
            isFirst = false
            current = next
        }
        return current
    }

    fun boxBlur(src: Bitmap, range: Int): Bitmap {
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
