package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.EmphasizedEasing
import dev.gaens.filmory.core.presentation.designsystem.LocalReducedMotion
import kotlinx.coroutines.delay

private const val GROW_DELAY_MILLIS = 300L
private const val GROW_MILLIS = 800

/**
 * A thin progress bar that grows from empty when it first appears. [progress] is 0 to 1. [secondary] is a second share
 * drawn after it in another colour, such as skipped episodes; both count as progress.
 */
@Composable
fun GrowingProgressBar(progress: Float, modifier: Modifier = Modifier, secondary: Float = 0f) {
    val grownProgress = rememberGrown(progress)
    val grownTotal = rememberGrown(progress + secondary)
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.outlineVariant)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress + secondary, 0f..1f) },
    ) {
        if (secondary > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(grownTotal.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
            )
        }
        Box(
            Modifier
                .fillMaxWidth(grownProgress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(shape)
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

/** [target] eased in from zero the first time, and from the last value after that. Snaps with reduced motion. */
@Composable
private fun rememberGrown(target: Float): Float {
    val reducedMotion = LocalReducedMotion.current
    val animated = remember { Animatable(if (reducedMotion) target else 0f) }
    var hasGrown by remember { mutableStateOf(false) }
    LaunchedEffect(target) {
        if (reducedMotion) return@LaunchedEffect animated.snapTo(target)
        if (!hasGrown) delay(GROW_DELAY_MILLIS)
        hasGrown = true
        animated.animateTo(target, tween(GROW_MILLIS, easing = EmphasizedEasing))
    }
    return animated.value
}
