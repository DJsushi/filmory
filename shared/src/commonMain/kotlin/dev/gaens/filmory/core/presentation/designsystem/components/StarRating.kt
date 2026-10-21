package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.ic_star
import org.jetbrains.compose.resources.painterResource

private const val STAR_COUNT = 5

/** Five stars filled to [stars] (0 to 5, half steps show as half stars). */
@Composable
fun StarRating(
    stars: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 14.dp,
) {
    Row(modifier = modifier) {
        repeat(STAR_COUNT) { index ->
            Star(fill = (stars - index).coerceIn(0f, 1f), size = starSize)
        }
    }
}

/** Tappable stars. The left half of a star sets a half star. Reports half-star steps (0 to 10). */
@Composable
fun StarRatingInput(
    halfStars: Int,
    onChange: (Int) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    starSize: Dp = 36.dp,
) {
    Row(modifier = modifier.semantics { this.contentDescription = contentDescription }) {
        repeat(STAR_COUNT) { index ->
            Box(
                modifier = Modifier.pointerInput(index) {
                    detectTapGestures { offset ->
                        val isLeftHalf = offset.x < size.width / 2
                        onChange(index * 2 + if (isLeftHalf) 1 else 2)
                    }
                },
            ) {
                Star(fill = (halfStars / 2f - index).coerceIn(0f, 1f), size = starSize)
            }
        }
    }
}

@Composable
private fun Star(fill: Float, size: Dp) {
    val painter = painterResource(Res.drawable.ic_star)
    Box {
        Icon(painter, null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(size))
        if (fill > 0f) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = FilmoryTheme.colors.star,
                modifier = Modifier
                    .size(size)
                    .drawWithContent { clipRect(right = this.size.width * fill) { this@drawWithContent.drawContent() } },
            )
        }
    }
}
