package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
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

private const val MAX_HALF_STARS = STAR_COUNT * 2
private val MinInputStarSize = 40.dp
private val MaxInputStarSize = 56.dp
private val ClearButtonGap = 8.dp

/** How [RatingInputRow] lays out: the star size in pixels and whether the clear button drops below. */
internal data class RatingLayoutPlan(val starSize: Int, val isButtonBelow: Boolean)

/**
 * Stars fill the width left beside the clear button. When that would make them smaller than
 * [minStar], they take the full width and the button goes below.
 */
internal fun planRatingLayout(available: Int, button: Int, gap: Int, minStar: Int, maxStar: Int): RatingLayoutPlan {
    val beside = (available - button - gap) / STAR_COUNT
    return if (beside >= minStar) {
        RatingLayoutPlan(minOf(beside, maxStar), isButtonBelow = false)
    } else {
        RatingLayoutPlan(minOf(available / STAR_COUNT, maxStar), isButtonBelow = true)
    }
}

/** Half-star step (1 to 10) under a touch at [x] on stars that are [starWidth] px wide each. */
internal fun halfStarsAt(x: Float, starWidth: Float): Int =
    kotlin.math.ceil(x / (starWidth / 2)).toInt().coerceIn(1, MAX_HALF_STARS)

/**
 * Stars that are set by tapping or by dragging a finger across them; the left half of a star is a
 * half star. They size themselves to the width they get, between 40 and 56 dp per star. Reports
 * half-star steps (1 to 10); dragging never clears, the clear button does.
 */
@Composable
fun StarRatingInput(
    halfStars: Int,
    onChange: (Int) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val currentHalfStars by rememberUpdatedState(halfStars)
    val report = { steps: Int ->
        if (steps != currentHalfStars) {
            haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            onChange(steps)
        }
    }
    BoxWithConstraints(modifier) {
        val starSize = minOf(maxWidth / STAR_COUNT, MaxInputStarSize)
        Row(
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures { report(halfStarsAt(it.x, size.width / STAR_COUNT.toFloat())) } }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        report(halfStarsAt(change.position.x, size.width / STAR_COUNT.toFloat()))
                    }
                }
                .semantics {
                    this.contentDescription = contentDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(halfStars.toFloat(), 0f..MAX_HALF_STARS.toFloat(), MAX_HALF_STARS - 1)
                    setProgress { value ->
                        onChange(value.roundToInt().coerceIn(1, MAX_HALF_STARS))
                        true
                    }
                },
        ) {
            repeat(STAR_COUNT) { index ->
                Star(fill = (halfStars / 2f - index).coerceIn(0f, 1f), size = starSize)
            }
        }
    }
}

/**
 * The rating input with its clear button. The button's slot is kept even when there is no rating,
 * so the stars never resize under a finger that is setting the first rating.
 */
@Composable
fun RatingInputRow(
    halfStars: Int,
    onChange: (Int) -> Unit,
    onClear: () -> Unit,
    contentDescription: String,
    clearLabel: String,
    modifier: Modifier = Modifier,
) {
    val hasRating = halfStars > 0
    Layout(
        modifier = modifier.fillMaxWidth(),
        content = {
            StarRatingInput(halfStars, onChange, contentDescription)
            TextButton(
                onClick = onClear,
                enabled = hasRating,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.alpha(if (hasRating) 1f else 0f).then(if (hasRating) Modifier else Modifier.clearAndSetSemantics {}),
            ) { Text(clearLabel) }
        },
    ) { measurables, constraints ->
        val button = measurables[1].measure(Constraints())
        val plan = planRatingLayout(
            available = constraints.maxWidth,
            button = button.width,
            gap = ClearButtonGap.roundToPx(),
            minStar = MinInputStarSize.roundToPx(),
            maxStar = MaxInputStarSize.roundToPx(),
        )
        val starsWidth = if (plan.isButtonBelow) constraints.maxWidth else plan.starSize * STAR_COUNT
        val stars = measurables[0].measure(Constraints(minWidth = starsWidth, maxWidth = starsWidth))
        if (plan.isButtonBelow) {
            layout(constraints.maxWidth, stars.height + button.height) {
                stars.place(0, 0)
                button.place(constraints.maxWidth - button.width, stars.height)
            }
        } else {
            val rowHeight = maxOf(stars.height, button.height)
            layout(constraints.maxWidth, rowHeight) {
                stars.place(0, (rowHeight - stars.height) / 2)
                button.place(constraints.maxWidth - button.width, (rowHeight - button.height) / 2)
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
