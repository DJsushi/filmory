package dev.gaens.filmory.feature.collection.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/** Ready-made card colors, from the app's blue to a pale cream that takes black text. */
val CollectionColorPresets: List<Long> = listOf(
    0xFF346A97,
    0xFF1F7A8C,
    0xFF2E7D4F,
    0xFF7A8F2A,
    0xFFE0A100,
    0xFFD9822B,
    0xFFC2413A,
    0xFFB5476A,
    0xFF8C4A9E,
    0xFF5B5F97,
    0xFF607D8B,
    0xFF3D2C2E,
    0xFFF2D7A0,
)

/** A color as hue (0 to 360), saturation and brightness (both 0 to 1). */
data class Hsv(val hue: Float, val saturation: Float, val value: Float)

fun hsvOf(argb: Long): Hsv {
    val color = Color(argb.toInt())
    val max = maxOf(color.red, color.green, color.blue)
    val min = minOf(color.red, color.green, color.blue)
    val delta = max - min
    val hue = when {
        delta == 0f -> 0f
        max == color.red -> 60f * (((color.green - color.blue) / delta) % 6f)
        max == color.green -> 60f * ((color.blue - color.red) / delta + 2f)
        else -> 60f * ((color.red - color.green) / delta + 4f)
    }
    return Hsv(if (hue < 0f) hue + 360f else hue, if (max == 0f) 0f else delta / max, max)
}

fun colorArgbOf(hsv: Hsv): Long = Color.hsv(hsv.hue.coerceIn(0f, 360f), hsv.saturation.coerceIn(0f, 1f), hsv.value.coerceIn(0f, 1f))
    .toArgb().toLong() and 0xFFFFFFFFL
