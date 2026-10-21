package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.math.absoluteValue

private const val POSTER_ASPECT_RATIO = 2f / 3f

// Placeholder art for titles without a poster, the same nine gradients as the mockup.
private val PosterGradients = listOf(
    Color(0xFF14213D) to Color(0xFF3A6EA5),
    Color(0xFF3D2C2E) to Color(0xFFB5651D),
    Color(0xFF1B4332) to Color(0xFF40916C),
    Color(0xFF3C1642) to Color(0xFF8C4A9E),
    Color(0xFF1F1F3A) to Color(0xFF5B5F97),
    Color(0xFF5C1A1A) to Color(0xFFC2413A),
    Color(0xFF0B3C49) to Color(0xFF2E8B9A),
    Color(0xFF4A3B1C) to Color(0xFFA78A3F),
    Color(0xFF263238) to Color(0xFF607D8B),
)

/** The two colors of a title's placeholder gradient, picked from its id. */
fun posterGradientColors(seed: String): Pair<Color, Color> =
    PosterGradients[seed.hashCode().absoluteValue % PosterGradients.size]

/** A stable gradient for a title, picked from its id. */
fun posterGradient(seed: String): Brush {
    val (from, to) = posterGradientColors(seed)
    return Brush.linearGradient(listOf(from, to))
}

/**
 * A title's art: the poster image when there is one, otherwise a gradient with the name on it.
 * [aspectRatio] is null for square or custom sizes set by the caller.
 */
@Composable
fun PosterArt(
    seed: String,
    name: String,
    posterUrl: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    aspectRatio: Float? = POSTER_ASPECT_RATIO,
    showName: Boolean = true,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .then(if (aspectRatio != null) Modifier.aspectRatio(aspectRatio) else Modifier)
            .clip(shape)
            .background(posterGradient(seed)),
    ) {
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (showName) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.6f))),
            )
            Text(
                text = name,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
            )
        }
        overlay()
    }
}
