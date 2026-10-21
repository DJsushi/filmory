package dev.gaens.filmory.feature.settings.domain

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    val library: LibrarySettings = LibrarySettings(),
    val calendar: CalendarSettings = CalendarSettings(),
    val homeLayout: List<HomeBlock> = HomeBlock.entries,
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Blocks Home shows below the header. Declaration order is the default layout. */
enum class HomeBlock(val isSection: Boolean) {
    WATCH_LATER(isSection = true),
    WISHLIST(isSection = false),
    CALENDAR(isSection = false),
    COLLECTIONS(isSection = true),
    ON_HOLD(isSection = false),
    DROPPED(isSection = false),
    LIBRARY(isSection = false),
}

/**
 * Turns saved block names into a layout that holds every block exactly once: unknown and repeated
 * names are dropped, blocks added by a newer app version go last.
 */
fun homeLayoutOf(names: List<String>): List<HomeBlock> {
    val saved = names.mapNotNull { name -> HomeBlock.entries.find { it.name == name } }.distinct()
    return saved + (HomeBlock.entries - saved.toSet())
}

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
