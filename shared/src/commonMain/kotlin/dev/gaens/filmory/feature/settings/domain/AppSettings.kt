package dev.gaens.filmory.feature.settings.domain

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    val library: LibrarySettings = LibrarySettings(),
    val calendar: CalendarSettings = CalendarSettings(),
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class LibrarySettings(
    val layout: LibraryLayout = LibraryLayout.GRID,
    val sort: LibrarySort = LibrarySort.UPDATED,
    val showStars: Boolean = true,
    val showStatusIcon: Boolean = true,
    val includeWishlist: Boolean = true,
)

enum class LibraryLayout { GRID, LIST }

enum class LibrarySort { UPDATED, ADDED, NAME, RATING }

/** Bookmory's calendar settings, applied to movies and series. */
data class CalendarSettings(
    val showFinishDates: Boolean = true,
    val showRanges: Boolean = true,
    val showLoggedDates: Boolean = true,
    val showStars: Boolean = true,
    val showVerticalBorder: Boolean = false,
    val showStateBadge: Boolean = true,
    val dayLayout: CalendarDayLayout = CalendarDayLayout.STACK,
    val weekStart: WeekStart = WeekStart.MONDAY,
)

enum class CalendarDayLayout { GRID, STACK }

enum class WeekStart { MONDAY, SUNDAY }
