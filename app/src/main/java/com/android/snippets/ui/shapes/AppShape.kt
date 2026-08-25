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
    COOKIE_12_SIDED("12-sided cookie"),
    COOKIE_4_SIDED("4-sided cookie"),
    VERY_SUNNY("Very sunny"),
    PILL("Pill"),
    PENTAGON("Pentagon"),
    OVAL("Oval")
}

val CookiePolygon = RoundedPolygon.star(
    numVerticesPerRadius = 12,
    radius = 0.5f,
    innerRadius = 0.5f * 0.88f,
    rounding = CornerRounding(0.5f * 0.12f),
    centerX = 0.5f,
    centerY = 0.5f
)

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
        val matrix = Matrix()
        matrix.translate(-bounds.left, -bounds.top)
        val scaleX = if (bounds.width > 0f) size.width / bounds.width else 1f
        val scaleY = if (bounds.height > 0f) size.height / bounds.height else 1f
        matrix.scale(scaleX, scaleY)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookieHoldMorphPolygons: List<RoundedPolygon> by lazy {
    listOf(
        CookiePolygon,
        MaterialShapes.Pill,
        MaterialShapes.Pentagon,
        CookiePolygon,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.VerySunny,
        MaterialShapes.Oval,
        CookiePolygon
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val CookieHoldMorphs: List<Morph> by lazy {
    val polygons = CookieHoldMorphPolygons
    (0 until polygons.size - 1).map { Morph(polygons[it], polygons[it + 1]) }
}

class MorphSequenceShape(
    val morphs: List<Morph>,
    val progress: Float
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
        val fraction = (normalized - index).coerceIn(0f, 1f)

        val path = morphs[index].toPath(fraction).asComposePath()
        val bounds = path.getBounds()
        val matrix = Matrix()
        matrix.translate(-bounds.left, -bounds.top)
        val scaleX = if (bounds.width > 0f) size.width / bounds.width else 1f
        val scaleY = if (bounds.height > 0f) size.height / bounds.height else 1f
        matrix.scale(scaleX, scaleY)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun AppShape.toComposeShape(): Shape {
    return when (this) {
        AppShape.COOKIE_12_SIDED -> RoundedPolygonShape(CookiePolygon)
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
        AppShape.COOKIE_12_SIDED -> CookiePolygon
        AppShape.COOKIE_4_SIDED -> MaterialShapes.Cookie4Sided
        AppShape.VERY_SUNNY -> MaterialShapes.VerySunny
        AppShape.PILL -> MaterialShapes.Pill
        AppShape.PENTAGON -> MaterialShapes.Pentagon
        AppShape.OVAL -> MaterialShapes.Oval
    }
}

val LocalAppShape = staticCompositionLocalOf<Shape> { RoundedPolygonShape(CookiePolygon) }
val LocalAppShapeType = staticCompositionLocalOf<AppShape> { AppShape.COOKIE_12_SIDED }

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
        val boundsF = android.graphics.RectF()
        polyPath.computeBounds(boundsF, true)

        val matrix = android.graphics.Matrix()
        matrix.postTranslate(-boundsF.left, -boundsF.top)
        val scaleX = if (boundsF.width() > 0f) bounds.width().toFloat() / boundsF.width() else 1f
        val scaleY = if (boundsF.height() > 0f) bounds.height().toFloat() / boundsF.height() else 1f
        matrix.postScale(scaleX, scaleY)

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

