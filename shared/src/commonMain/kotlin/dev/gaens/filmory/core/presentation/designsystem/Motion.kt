package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** True when the system asks apps to skip non-essential animation. */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
internal expect fun rememberReducedMotion(): Boolean

/** Material's emphasized decelerate curve, used by the mockup for every entrance. */
val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

private const val STAGGER_MILLIS = 55L
private const val RISE_MILLIS = 450
private const val MAX_STAGGERED_ITEMS = 12
private const val PRESSED_SCALE = 0.97f

/**
 * Staggered card entrance: the item fades in and rises 14 dp, [index] steps after the first one.
 * Plays once per item, so scrolling back does not replay it.
 */
fun Modifier.rise(index: Int): Modifier = composed {
    if (LocalReducedMotion.current) return@composed this
    var hasPlayed by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (hasPlayed) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (hasPlayed) return@LaunchedEffect
        delay(index.coerceAtMost(MAX_STAGGERED_ITEMS) * STAGGER_MILLIS)
        progress.animateTo(1f, tween(RISE_MILLIS, easing = EmphasizedEasing))
        hasPlayed = true
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 14.dp.toPx()
    }
}

/** Pop-in for checks and the floating action button: scales up from 70 % while fading in. */
fun Modifier.popIn(delayMillis: Long = 0L): Modifier = composed {
    if (LocalReducedMotion.current) return@composed this
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        progress.animateTo(1f, tween(RISE_MILLIS, easing = EmphasizedEasing))
    }
    graphicsLayer {
        alpha = progress.value
        val scale = 0.7f + 0.3f * progress.value
        scaleX = scale
        scaleY = scale
    }
}

/** Shrinks slightly while pressed. Pass the same source to the clickable. */
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val reducedMotion = LocalReducedMotion.current
    val scale by animateFloatAsState(if (isPressed && !reducedMotion) PRESSED_SCALE else 1f, tween(150))
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
