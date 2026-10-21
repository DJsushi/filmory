package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
internal actual fun rememberReducedMotion(): Boolean = UIAccessibilityIsReduceMotionEnabled()
