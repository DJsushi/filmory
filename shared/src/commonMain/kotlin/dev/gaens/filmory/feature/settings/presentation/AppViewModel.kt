package dev.gaens.filmory.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.feature.settings.domain.AppSettings
import dev.gaens.filmory.feature.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** App-wide state the root composable needs: the theme settings. */
class AppViewModel(settingsRepository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings?> = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
