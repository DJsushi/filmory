package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
internal actual fun dynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

actual val isDynamicColorAvailable: Boolean = false
