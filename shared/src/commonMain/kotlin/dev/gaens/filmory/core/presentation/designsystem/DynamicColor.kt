package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/** Material You scheme, or null where the platform has none (iOS, Android below 12). */
@Composable
internal expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme?
