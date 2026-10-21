package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun FilmoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val staticScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = if (dynamicColor) dynamicColorScheme(darkTheme) ?: staticScheme else staticScheme

    CompositionLocalProvider(
        LocalFilmoryColors provides if (darkTheme) DarkFilmoryColors else LightFilmoryColors,
        LocalReducedMotion provides rememberReducedMotion(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FilmoryTypography,
            shapes = FilmoryShapes,
            content = content,
        )
    }
}

object FilmoryTheme {
    val colors: FilmoryColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFilmoryColors.current
}
