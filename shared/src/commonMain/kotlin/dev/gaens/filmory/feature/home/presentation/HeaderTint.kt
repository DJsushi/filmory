package dev.gaens.filmory.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Size as CoilSize
import dev.gaens.filmory.core.presentation.designsystem.components.posterGradientColors
import dev.gaens.filmory.core.presentation.designsystem.forPixelReading

private const val SAMPLE_SIZE = 24
private const val MIN_ALPHA = 128
private const val MIN_CONTRAST = 4.5f
private const val TINT_STRENGTH = 0.6f
private const val DARKEN_STEP = 0.1f
private const val MAX_DARKEN_STEPS = 20

/** Average of the opaque pixels, which are packed ARGB, or null when there are none. */
fun averageColor(pixels: IntArray): Color? {
    var red = 0L
    var green = 0L
    var blue = 0L
    var count = 0
    for (pixel in pixels) {
        if ((pixel ushr 24) < MIN_ALPHA) continue
        red += (pixel shr 16) and 0xFF
        green += (pixel shr 8) and 0xFF
        blue += pixel and 0xFF
        count++
    }
    if (count == 0) return null
    val divisor = count * 255f
    return Color(red = red / divisor, green = green / divisor, blue = blue / divisor)
}

/** WCAG contrast ratio between two opaque colors. */
fun contrastRatio(a: Color, b: Color): Float {
    val (lighter, darker) = maxOf(a.luminance(), b.luminance()) to minOf(a.luminance(), b.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
}

/**
 * Header color for a poster's [average] color. It leans toward the poster but stays close to the
 * [base] color, and is pushed away from [onBase] until the text on it reaches AA contrast.
 */
fun headerTint(average: Color, base: Color, onBase: Color): Color {
    val away = if (onBase.luminance() > 0.5f) Color.Black else Color.White
    var tint = lerp(base, average, TINT_STRENGTH)
    repeat(MAX_DARKEN_STEPS) {
        if (contrastRatio(onBase, tint) >= MIN_CONTRAST) return tint
        tint = lerp(tint, away, DARKEN_STEP)
    }
    return tint
}

/** The average color of a title's poster, or of its placeholder gradient. Null until loaded. */
@Composable
fun rememberPosterColor(seed: String, posterUrl: String?): Color? =
    if (posterUrl == null) {
        remember(seed) { posterGradientColors(seed).let { (from, to) -> lerp(from, to, 0.5f) } }
    } else {
        rememberLoadedPosterColor(posterUrl)
    }

@Composable
private fun rememberLoadedPosterColor(posterUrl: String): Color? {
    val request = ImageRequest.Builder(LocalPlatformContext.current)
        .data(posterUrl)
        .size(CoilSize(SAMPLE_SIZE * 2, SAMPLE_SIZE * 2))
        .forPixelReading()
        .build()
    val painter = rememberAsyncImagePainter(request)
    val state by painter.state.collectAsState()
    val loaded = (state as? AsyncImagePainter.State.Success)?.painter
    return remember(loaded) { loaded?.sampleAverage() }
}

private fun Painter.sampleAverage(): Color? {
    val bitmap = ImageBitmap(SAMPLE_SIZE, SAMPLE_SIZE)
    val size = Size(SAMPLE_SIZE.toFloat(), SAMPLE_SIZE.toFloat())
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap), size) {
        with(this@sampleAverage) { draw(size) }
    }
    val pixels = IntArray(SAMPLE_SIZE * SAMPLE_SIZE)
    bitmap.readPixels(pixels)
    return averageColor(pixels)
}
