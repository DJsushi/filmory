package dev.gaens.filmory.feature.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.gaens.filmory.feature.settings.domain.AppSettings
import dev.gaens.filmory.feature.settings.domain.CalendarSettings
import dev.gaens.filmory.feature.settings.domain.LibrarySettings
import dev.gaens.filmory.feature.settings.domain.homeLayoutOf
import dev.gaens.filmory.feature.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal const val SETTINGS_FILE_NAME = "settings.preferences_pb"

private object Keys {
    val themeMode = stringPreferencesKey("theme_mode")
    val dynamicColor = booleanPreferencesKey("dynamic_color")
    val libraryLayout = stringPreferencesKey("library_layout")
    val librarySort = stringPreferencesKey("library_sort")
    val libraryStars = booleanPreferencesKey("library_show_stars")
    val libraryStatusIcon = booleanPreferencesKey("library_show_status_icon")
    val libraryWishlist = booleanPreferencesKey("library_include_wishlist")
    val calendarFinish = booleanPreferencesKey("calendar_finish_dates")
    val calendarRanges = booleanPreferencesKey("calendar_ranges")
    val calendarLogged = booleanPreferencesKey("calendar_logged_dates")
    val calendarStars = booleanPreferencesKey("calendar_stars")
    val calendarBorder = booleanPreferencesKey("calendar_vertical_border")
    val calendarBadge = booleanPreferencesKey("calendar_state_badge")
    val calendarLayout = stringPreferencesKey("calendar_day_layout")
    val calendarWeekStart = stringPreferencesKey("calendar_week_start")
    val homeLayout = stringPreferencesKey("home_layout")
}

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = dataStore.data.map { it.toAppSettings() }

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { preferences -> preferences.write(transform(preferences.toAppSettings())) }
    }
}

private inline fun <reified T : Enum<T>> Preferences.enum(key: Preferences.Key<String>, default: T): T =
    this[key]?.let { name -> enumValues<T>().find { it.name == name } } ?: default

private fun Preferences.toAppSettings(): AppSettings {
    val defaults = AppSettings()
    val library = defaults.library
    val calendar = defaults.calendar
    return AppSettings(
        themeMode = enum(Keys.themeMode, defaults.themeMode),
        useDynamicColor = this[Keys.dynamicColor] ?: defaults.useDynamicColor,
        library = LibrarySettings(
            layout = enum(Keys.libraryLayout, library.layout),
            sort = enum(Keys.librarySort, library.sort),
            showStars = this[Keys.libraryStars] ?: library.showStars,
            showStatusIcon = this[Keys.libraryStatusIcon] ?: library.showStatusIcon,
            includeWishlist = this[Keys.libraryWishlist] ?: library.includeWishlist,
        ),
        calendar = CalendarSettings(
            showFinishDates = this[Keys.calendarFinish] ?: calendar.showFinishDates,
            showRanges = this[Keys.calendarRanges] ?: calendar.showRanges,
            showLoggedDates = this[Keys.calendarLogged] ?: calendar.showLoggedDates,
            showStars = this[Keys.calendarStars] ?: calendar.showStars,
            showVerticalBorder = this[Keys.calendarBorder] ?: calendar.showVerticalBorder,
            showStateBadge = this[Keys.calendarBadge] ?: calendar.showStateBadge,
            dayLayout = enum(Keys.calendarLayout, calendar.dayLayout),
            weekStart = enum(Keys.calendarWeekStart, calendar.weekStart),
        ),
        homeLayout = homeLayoutOf(this[Keys.homeLayout]?.split(",").orEmpty()),
    )
}

private fun MutablePreferences.write(settings: AppSettings) {
    this[Keys.themeMode] = settings.themeMode.name
    this[Keys.dynamicColor] = settings.useDynamicColor
    this[Keys.libraryLayout] = settings.library.layout.name
    this[Keys.librarySort] = settings.library.sort.name
    this[Keys.libraryStars] = settings.library.showStars
    this[Keys.libraryStatusIcon] = settings.library.showStatusIcon
    this[Keys.libraryWishlist] = settings.library.includeWishlist
    this[Keys.calendarFinish] = settings.calendar.showFinishDates
    this[Keys.calendarRanges] = settings.calendar.showRanges
    this[Keys.calendarLogged] = settings.calendar.showLoggedDates
    this[Keys.calendarStars] = settings.calendar.showStars
    this[Keys.calendarBorder] = settings.calendar.showVerticalBorder
    this[Keys.calendarBadge] = settings.calendar.showStateBadge
    this[Keys.calendarLayout] = settings.calendar.dayLayout.name
    this[Keys.calendarWeekStart] = settings.calendar.weekStart.name
    this[Keys.homeLayout] = settings.homeLayout.joinToString(",") { it.name }
}
