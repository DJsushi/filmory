package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** WCAG contrast ratio between two opaque colors. */
fun contrastRatio(a: Color, b: Color): Float {
    val (lighter, darker) = maxOf(a.luminance(), b.luminance()) to minOf(a.luminance(), b.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
}

/**
 * White or black, whichever reads better on [background]. Between them the contrast is at least
 * 4.5:1 on every color, which a softer dark would not reach on mid-tones.
 */
fun readableContentOn(background: Color): Color =
    if (contrastRatio(Color.White, background) >= contrastRatio(Color.Black, background)) Color.White else Color.Black
