package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF346A97),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E2EE),
    onPrimaryContainer = Color(0xFF0C2A42),
    secondaryContainer = Color(0xFFDCE6EE),
    onSecondaryContainer = Color(0xFF10283C),
    tertiaryContainer = Color(0xFFFFE08C),
    onTertiaryContainer = Color(0xFF241A00),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF7EFE4),
    onBackground = Color(0xFF2B2824),
    surface = Color(0xFFF7EFE4),
    onSurface = Color(0xFF2B2824),
    onSurfaceVariant = Color(0xFF5C574F),
    surfaceContainerLow = Color(0xFFF2EADF),
    surfaceContainer = Color(0xFFFBF8F3),
    surfaceContainerHigh = Color(0xFFEBE3D6),
    outline = Color(0xFF8A8479),
    outlineVariant = Color(0xFFD6CEC0),
)

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA3C9EC),
    onPrimary = Color(0xFF0A3350),
    primaryContainer = Color(0xFF1D4F78),
    onPrimaryContainer = Color(0xFFD3E2EE),
    secondaryContainer = Color(0xFF2C4152),
    onSecondaryContainer = Color(0xFFD6E4F0),
    tertiaryContainer = Color(0xFF5A4700),
    onTertiaryContainer = Color(0xFFFFE08C),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1B1A17),
    onBackground = Color(0xFFE8E2D9),
    surface = Color(0xFF1B1A17),
    onSurface = Color(0xFFE8E2D9),
    onSurfaceVariant = Color(0xFFC9C2B6),
    surfaceContainerLow = Color(0xFF201E1B),
    surfaceContainer = Color(0xFF262421),
    surfaceContainerHigh = Color(0xFF302D29),
    outline = Color(0xFF938D82),
    outlineVariant = Color(0xFF4A463F),
)

/** Colors the Material scheme has no slot for: status badges, stars and the highlighter underline. */
@Immutable
data class FilmoryColors(
    val hero: Color,
    val onHero: Color,
    val watchedContainer: Color,
    val onWatchedContainer: Color,
    val wishlistContainer: Color,
    val onWishlistContainer: Color,
    val star: Color,
    val highlighter: Color,
    val cardShadow: Color,
)

internal val LightFilmoryColors = FilmoryColors(
    hero = Color(0xFF346A97),
    onHero = Color(0xFFFFFFFF),
    watchedContainer = Color(0xFFC8EFD4),
    onWatchedContainer = Color(0xFF00210F),
    wishlistContainer = Color(0xFFE8DEF8),
    onWishlistContainer = Color(0xFF1D192B),
    star = Color(0xFFF2B705),
    highlighter = Color(0xFFC9D7DF),
    cardShadow = Color(0xFF503C1E),
)

internal val DarkFilmoryColors = FilmoryColors(
    hero = Color(0xFF1D4F78),
    onHero = Color(0xFFFFFFFF),
    watchedContainer = Color(0xFF1F5135),
    onWatchedContainer = Color(0xFFC8EFD4),
    wishlistContainer = Color(0xFF4A4458),
    onWishlistContainer = Color(0xFFE8DEF8),
    star = Color(0xFFF2B705),
    highlighter = Color(0xFF2E4A5E),
    cardShadow = Color(0xFF000000),
)

internal val LocalFilmoryColors = staticCompositionLocalOf { LightFilmoryColors }
