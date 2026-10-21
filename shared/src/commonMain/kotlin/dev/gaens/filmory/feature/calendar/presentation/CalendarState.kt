package dev.gaens.filmory.feature.calendar.presentation

import dev.gaens.filmory.feature.calendar.domain.CalendarMonth
import dev.gaens.filmory.feature.settings.domain.CalendarSettings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

data class CalendarState(
    val month: YearMonth? = null,
    val today: LocalDate? = null,
    val calendar: CalendarMonth? = null,
    val settings: CalendarSettings = CalendarSettings(),
    val isSettingsOpen: Boolean = false,
    val selectedDate: LocalDate? = null,
)

sealed interface CalendarAction {
    data object OnBackClick : CalendarAction
    data object OnPreviousMonthClick : CalendarAction
    data object OnNextMonthClick : CalendarAction
    data class OnSettingsChange(val settings: CalendarSettings) : CalendarAction
    data object OnSettingsClick : CalendarAction
    data object OnSheetDismiss : CalendarAction
    data class OnDayClick(val date: LocalDate) : CalendarAction
    data class OnTitleClick(val titleId: String) : CalendarAction
    data object OnExportClick : CalendarAction
}
