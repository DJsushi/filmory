package dev.gaens.filmory.feature.settings.presentation

import dev.gaens.filmory.feature.settings.domain.ThemeMode

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    val tintHomeHeader: Boolean = true,
    val titleCount: Int = 0,
    val watchedCount: Int = 0,
    val noteCount: Int = 0,
)

sealed interface SettingsAction {
    data class OnThemeModeChange(val mode: ThemeMode) : SettingsAction
    data class OnDynamicColorChange(val enabled: Boolean) : SettingsAction
    data class OnTintHomeHeaderChange(val enabled: Boolean) : SettingsAction
}
