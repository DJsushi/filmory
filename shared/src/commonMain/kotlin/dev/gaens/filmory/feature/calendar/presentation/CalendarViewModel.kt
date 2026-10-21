package dev.gaens.filmory.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.feature.calendar.domain.BuildCalendarMonth
import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.note.domain.Note
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.settings.domain.CalendarSettings
import dev.gaens.filmory.feature.settings.domain.SettingsRepository
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plusMonth
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

class CalendarViewModel(
    titleRepository: TitleRepository,
    watchingRepository: WatchingRepository,
    noteRepository: NoteRepository,
    episodeRepository: EpisodeRepository,
    private val settingsRepository: SettingsRepository,
    private val buildCalendarMonth: BuildCalendarMonth,
    clock: Clock,
) : ViewModel() {

    private val timeZone = TimeZone.currentSystemDefault()
    private val today = clock.now().toLocalDateTime(timeZone).date
    private val _state = MutableStateFlow(CalendarState(month = today.yearMonth, today = today))

    private val data = combine(
        titleRepository.observeTitles(),
        watchingRepository.observeAllWatchings(),
        noteRepository.observeNotes(),
        episodeRepository.observeAllChecks(),
        settingsRepository.observeSettings(),
    ) { titles, watchings, notes, checks, settings -> CalendarData(titles, watchings, notes, checks, settings.calendar) }

    val state = combine(_state, data) { ui, data ->
        val month = ui.month ?: today.yearMonth
        ui.copy(
            settings = data.settings,
            calendar = buildCalendarMonth(month, data.settings, data.titles, data.watchings, data.notes, data.checks, today, timeZone),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), CalendarState())

    fun onAction(action: CalendarAction) {
        when (action) {
            CalendarAction.OnPreviousMonthClick -> _state.update { it.copy(month = it.month?.minusMonth()) }
            CalendarAction.OnNextMonthClick -> _state.update { it.copy(month = it.month?.plusMonth()) }
            is CalendarAction.OnSettingsChange -> viewModelScope.launch {
                settingsRepository.update { it.copy(calendar = action.settings) }
            }
            CalendarAction.OnSettingsClick -> _state.update { it.copy(isSettingsOpen = true) }
            CalendarAction.OnSheetDismiss -> _state.update { it.copy(isSettingsOpen = false, selectedDate = null) }
            is CalendarAction.OnDayClick -> _state.update { it.copy(selectedDate = action.date) }
            is CalendarAction.OnTitleClick -> _state.update { it.copy(selectedDate = null) }
            else -> Unit
        }
    }

    private data class CalendarData(
        val titles: List<Title>,
        val watchings: List<Watching>,
        val notes: List<Note>,
        val checks: List<EpisodeCheck>,
        val settings: CalendarSettings,
    )
}
