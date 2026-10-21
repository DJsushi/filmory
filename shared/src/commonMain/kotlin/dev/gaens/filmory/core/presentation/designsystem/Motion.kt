package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** True when the system asks apps to skip non-essential animation. */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
internal expect fun rememberReducedMotion(): Boolean
