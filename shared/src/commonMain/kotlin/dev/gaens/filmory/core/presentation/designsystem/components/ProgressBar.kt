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

/** A thin progress bar that grows from empty when it first appears. [progress] is 0 to 1. */
@Composable
fun GrowingProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val reducedMotion = LocalReducedMotion.current
    val animated = remember { Animatable(if (reducedMotion) progress else 0f) }
    var hasGrown by remember { mutableStateOf(false) }
    LaunchedEffect(progress) {
        if (reducedMotion) return@LaunchedEffect animated.snapTo(progress)
        if (!hasGrown) delay(GROW_DELAY_MILLIS)
        hasGrown = true
        animated.animateTo(progress, tween(GROW_MILLIS, easing = EmphasizedEasing))
    }
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.outlineVariant)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) },
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated.value.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(shape)
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}
