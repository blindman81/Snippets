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
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.toPath
import androidx.compose.ui.graphics.Matrix

import androidx.graphics.shapes.Morph

enum class AppShape(val displayName: String) {
    COOKIE_9_SIDED("9-sided cookie"),
    COOKIE_4_SIDED("4-sided cookie"),
    VERY_SUNNY("Very sunny"),
    PILL("Pill"),
    PENTAGON("Pentagon"),
    OVAL("Oval")
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookiePolygon: RoundedPolygon = MaterialShapes.Cookie9Sided

val CookieShape = RoundedPolygonShape(CookiePolygon)
val Cookie9Shape: Shape = CookieShape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val Cookie12Polygon: RoundedPolygon = MaterialShapes.Cookie12Sided

val Cookie12Shape: Shape = RoundedPolygonShape(Cookie12Polygon)

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

val CirclePolygon: RoundedPolygon by lazy {
    RoundedPolygon.circle(numVertices = 9)
}

val Cookie9ToCircleMorph: Morph by lazy {
    Morph(CookiePolygon.normalized(), CirclePolygon.normalized())
}

class MorphShape(
    val morph: Morph,
    val progress: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val morphFraction = progress.coerceIn(0f, 1f)
        val androidPath = morph.toPath(morphFraction)
        val pathBounds = android.graphics.RectF()
        androidPath.computeBounds(pathBounds, true)
        val pathCenterX = pathBounds.centerX()
        val pathCenterY = pathBounds.centerY()
        val maxDim = maxOf(pathBounds.width(), pathBounds.height()).coerceAtLeast(0.001f)
        val scale = minOf(size.width, size.height) / maxDim

        val matrix = android.graphics.Matrix()
        matrix.postTranslate(-pathCenterX, -pathCenterY)
        matrix.postScale(scale, scale)
        matrix.postTranslate(size.width / 2f, size.height / 2f)
        androidPath.transform(matrix)

        return Outline.Generic(androidPath.asComposePath())
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookieHoldMorphPolygons: List<RoundedPolygon> by lazy {
    // Exact shapes and sequence matching LoadingIndicatorDefaults.IndeterminateIndicatorPolygons,
    // starting at Cookie9Sided and completing a cycle back to Cookie9Sided:
    listOf(
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Pentagon,
        MaterialShapes.Pill,
        MaterialShapes.Sunny,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.Oval,
        MaterialShapes.SoftBurst,
        MaterialShapes.Cookie9Sided
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookieHoldMorphs: List<Morph> by lazy {
    val polygons = CookieHoldMorphPolygons
    (0 until polygons.size - 1).map { Morph(polygons[it].normalized(), polygons[it + 1].normalized()) }
}

private val PolygonDiameters: List<Float> by lazy {
    val bounds = FloatArray(4)
    CookieHoldMorphPolygons.map { polygon ->
        polygon.normalized().calculateMaxBounds(bounds)
        (bounds[2] - bounds[0]).coerceAtLeast(0.001f)
    }
}

class MorphSequenceShape(
    val morphs: List<Morph>,
    val morphIndex: Int,
    val fraction: Float,
    val rotation: Float = (morphIndex + fraction) * 90f
) : Shape {
    constructor(
        morphs: List<Morph>,
        progress: Float,
        rotation: Float = progress * 90f
    ) : this(
        morphs = morphs,
        morphIndex = if (progress >= morphs.size) (morphs.size - 1).coerceAtLeast(0) else progress.toInt().coerceIn(0, (morphs.size - 1).coerceAtLeast(0)),
        fraction = if (progress >= morphs.size) 1f else (progress - progress.toInt()).coerceIn(0f, 1f),
        rotation = rotation
    )

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (morphs.isEmpty()) {
            return Outline.Rectangle(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
        }
        val safeIndex = morphIndex.coerceIn(0, morphs.size - 1)
        val morph = morphs[safeIndex]
        val morphFraction = fraction.coerceIn(-0.2f, 1.3f)

        // 1. Morph path at current step fraction (following default LoadingIndicator toPath with spring overshoot)
        val androidPath = morph.toPath(morphFraction)

        // 2. Measure unrotated path bounds to get the true center of the shape
        val pathBounds = android.graphics.RectF()
        androidPath.computeBounds(pathBounds, true)
        val pathCenterX = pathBounds.centerX()
        val pathCenterY = pathBounds.centerY()

        // 3. Smooth scale interpolated between shapes, ensuring Oval never clips while Cookie9Sided fills 100%
        val step = safeIndex.coerceIn(0, PolygonDiameters.size - 2)
        val frac = fraction.coerceIn(0f, 1f)
        val targetDim = PolygonDiameters[step] * (1f - frac) + PolygonDiameters[step + 1] * frac
        val scale = (minOf(size.width, size.height) / targetDim) * 0.96f

        // 4. Translate center of shape to origin (0, 0), scale, rotate around center, then translate to container center.
        // This ensures the shape rotates purely around its geometric center (no eccentric orbiting around the icon)
        // and never touches or clips outside the bounding box.
        val matrix = android.graphics.Matrix()
        matrix.postTranslate(-pathCenterX, -pathCenterY)
        matrix.postScale(scale, scale)
        matrix.postRotate(rotation)
        matrix.postTranslate(size.width / 2f, size.height / 2f)
        androidPath.transform(matrix)

        return Outline.Generic(androidPath.asComposePath())
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun AppShape.toComposeShape(): Shape {
    return when (this) {
        AppShape.COOKIE_9_SIDED -> RoundedPolygonShape(CookiePolygon)
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

