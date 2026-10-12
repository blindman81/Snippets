package com.android.snippets.ui.shapes

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.compose.ui.graphics.Matrix

import androidx.graphics.shapes.Morph

enum class AppShape(val displayName: String) {
    COOKIE_9_SIDED("9-sided cookie"),
    COOKIE_12_SIDED("12-sided cookie"),
    COOKIE_4_SIDED("4-sided cookie"),
    VERY_SUNNY("Very sunny"),
    PILL("Pill"),
    PENTAGON("Pentagon"),
    OVAL("Oval")
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookiePolygon: RoundedPolygon = MaterialShapes.Cookie9Sided

val CookieShape = RoundedPolygonShape(CookiePolygon)

class RoundedPolygonShape(
    val polygon: RoundedPolygon
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = polygon.toPath().asComposePath()
        val bounds = path.getBounds()
        val scale = if (bounds.width > 0f && bounds.height > 0f) {
            minOf(size.width / bounds.width, size.height / bounds.height)
        } else 1f

        val matrix = Matrix()
        matrix.translate(
            x = (size.width - bounds.width * scale) / 2f - bounds.left * scale,
            y = (size.height - bounds.height * scale) / 2f - bounds.top * scale
        )
        matrix.scale(scale, scale)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookieHoldMorphPolygons: List<RoundedPolygon> by lazy {
    val pentagonMatrix = android.graphics.Matrix().apply { postRotate(270f) }
    val alignedPentagon = MaterialShapes.Pentagon.transformed { x, y ->
        val pts = floatArrayOf(x, y)
        pentagonMatrix.mapPoints(pts)
        androidx.graphics.shapes.TransformResult(pts[0], pts[1])
    }
    listOf(
        MaterialShapes.Cookie9Sided,
        alignedPentagon,
        MaterialShapes.Pill,
        MaterialShapes.SoftBurst,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.Oval,
        MaterialShapes.VerySunny,
        MaterialShapes.Cookie9Sided
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookieHoldMorphs: List<Morph> by lazy {
    val polygons = CookieHoldMorphPolygons
    (0 until polygons.size - 1).map { Morph(polygons[it].normalized(), polygons[it + 1].normalized()) }
}

private val CookieHoldScaleFactor: Float by lazy {
    var scaleFactor = 1f
    val bounds = FloatArray(4)
    val maxBounds = FloatArray(4)
    CookieHoldMorphPolygons.forEach { polygon ->
        polygon.calculateBounds(bounds)
        polygon.calculateMaxBounds(maxBounds)
        val w = bounds[2] - bounds[0]
        val h = bounds[3] - bounds[1]
        val maxW = maxBounds[2] - maxBounds[0]
        val maxH = maxBounds[3] - maxBounds[1]
        val scaleX = if (maxW > 0f) w / maxW else 1f
        val scaleY = if (maxH > 0f) h / maxH else 1f
        scaleFactor = minOf(scaleFactor, maxOf(scaleX, scaleY))
    }
    scaleFactor
}

class MorphSequenceShape(
    val morphs: List<Morph>,
    val progress: Float,
    val rotationPerMorph: Float = 90f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (morphs.isEmpty()) {
            return Outline.Rectangle(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
        }
        val total = morphs.size
        val normalized = ((progress % total) + total) % total
        val index = normalized.toInt().coerceIn(0, total - 1)
        val rawFraction = normalized - index
        val morphFraction = rawFraction.coerceIn(0f, 1f)

        // 1. Morph path at current step fraction
        val androidPath = morphs[index].toPath(morphFraction)

        // 2. Rotate clockwise by progress * rotationPerMorph
        val rotationDegrees = progress * rotationPerMorph
        val rotateMatrix = android.graphics.Matrix()
        rotateMatrix.postRotate(rotationDegrees)
        androidPath.transform(rotateMatrix)

        // 3. Dynamic scale pulse following Material 3 Expressive LoadingIndicator:
        // Morph squash during transition + bounce overshoot pulse at completion of each shape step
        val morphSquash = 1f - kotlin.math.sin(morphFraction * Math.PI.toFloat()) * 0.08f
        val bouncePulse = if (rawFraction > 1f) (rawFraction - 1f) * 0.85f else 0f
        val pulseFactor = morphSquash + bouncePulse

        val baseScale = (minOf(size.width, size.height) / 2f) * CookieHoldScaleFactor * 0.95f
        val finalScale = baseScale * pulseFactor

        val scaleMatrix = android.graphics.Matrix()
        scaleMatrix.postScale(finalScale, finalScale)
        androidPath.transform(scaleMatrix)

        // 4. Center inside available size: align center with container center (M3 processPath logic)
        val bounds = android.graphics.RectF()
        androidPath.computeBounds(bounds, true)
        androidPath.offset(size.width / 2f - bounds.centerX(), size.height / 2f - bounds.centerY())

        return Outline.Generic(androidPath.asComposePath())
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun AppShape.toComposeShape(): Shape {
    return when (this) {
        AppShape.COOKIE_9_SIDED -> RoundedPolygonShape(CookiePolygon)
        AppShape.COOKIE_12_SIDED -> RoundedPolygonShape(MaterialShapes.Cookie12Sided)
        AppShape.COOKIE_4_SIDED -> RoundedPolygonShape(MaterialShapes.Cookie4Sided)
        AppShape.VERY_SUNNY -> RoundedPolygonShape(MaterialShapes.VerySunny)
        AppShape.PILL -> RoundedPolygonShape(MaterialShapes.Pill)
        AppShape.PENTAGON -> RoundedPolygonShape(MaterialShapes.Pentagon)
        AppShape.OVAL -> RoundedPolygonShape(MaterialShapes.Oval)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun AppShape.getNormalizedPolygon(): RoundedPolygon {
    return when (this) {
        AppShape.COOKIE_9_SIDED -> CookiePolygon
        AppShape.COOKIE_12_SIDED -> MaterialShapes.Cookie12Sided
        AppShape.COOKIE_4_SIDED -> MaterialShapes.Cookie4Sided
        AppShape.VERY_SUNNY -> MaterialShapes.VerySunny
        AppShape.PILL -> MaterialShapes.Pill
        AppShape.PENTAGON -> MaterialShapes.Pentagon
        AppShape.OVAL -> MaterialShapes.Oval
    }
}

val LocalAppShape = staticCompositionLocalOf<Shape> { RoundedPolygonShape(CookiePolygon) }
val LocalAppShapeType = staticCompositionLocalOf<AppShape> { AppShape.COOKIE_9_SIDED }

class PolygonDrawable(
    private val polygon: RoundedPolygon,
    private val fillColor: Int
) : android.graphics.drawable.Drawable() {
    private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = android.graphics.Paint.Style.FILL
    }
    private val path = android.graphics.Path()

    override fun onBoundsChange(bounds: android.graphics.Rect) {
        super.onBoundsChange(bounds)
        val polyPath = polygon.toPath()
        val matrix = android.graphics.Matrix()
        val scale = minOf(bounds.width(), bounds.height()).toFloat() / 2f
        matrix.setScale(scale, scale)
        matrix.postTranslate(bounds.exactCenterX(), bounds.exactCenterY())

        path.reset()
        polyPath.transform(matrix, path)
    }

    override fun draw(canvas: android.graphics.Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java", ReplaceWith("android.graphics.PixelFormat.TRANSLUCENT"))
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}

