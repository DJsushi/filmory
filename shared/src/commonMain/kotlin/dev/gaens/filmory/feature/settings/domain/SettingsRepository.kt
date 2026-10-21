package dev.gaens.filmory.feature.settings.domain

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
