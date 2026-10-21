package dev.gaens.filmory.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.settings.domain.SeriesSettings
import dev.gaens.filmory.feature.settings.domain.SettingsRepository
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.title.domain.TitleStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    titleRepository: TitleRepository,
    noteRepository: NoteRepository,
) : ViewModel() {

    val state = combine(
        settingsRepository.observeSettings(),
        titleRepository.observeTitles(),
        noteRepository.observeNotes(),
    ) { settings, titles, notes ->
        SettingsState(
            themeMode = settings.themeMode,
            useDynamicColor = settings.useDynamicColor,
            tintHomeHeader = settings.tintHomeHeader,
            series = settings.series,
            titleCount = titles.size,
            watchedCount = titles.count { it.status == TitleStatus.WATCHED },
            noteCount = notes.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), SettingsState())

    fun onAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.OnThemeModeChange -> settingsRepository.update { it.copy(themeMode = action.mode) }
                is SettingsAction.OnDynamicColorChange ->
                    settingsRepository.update { it.copy(useDynamicColor = action.enabled) }
                is SettingsAction.OnTintHomeHeaderChange ->
                    settingsRepository.update { it.copy(tintHomeHeader = action.enabled) }
                is SettingsAction.OnMarkerFollowsTicksChange ->
                    updateSeries { it.copy(markerFollowsTicks = action.enabled) }
                is SettingsAction.OnMarkerTargetChange -> updateSeries { it.copy(markerTarget = action.target) }
                is SettingsAction.OnMarkerMovesBackwardsChange ->
                    updateSeries { it.copy(markerMovesBackwards = action.enabled) }
            }
        }
    }

    private suspend fun updateSeries(transform: (SeriesSettings) -> SeriesSettings) =
        settingsRepository.update { it.copy(series = transform(it.series)) }
}
