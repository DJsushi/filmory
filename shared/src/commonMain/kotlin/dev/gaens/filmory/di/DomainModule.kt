package dev.gaens.filmory.di

import dev.gaens.filmory.feature.episode.domain.AdvanceEpisode
import dev.gaens.filmory.feature.note.domain.AddNote
import dev.gaens.filmory.feature.title.domain.ChangeTitleStatus
import dev.gaens.filmory.feature.watching.domain.ChangeWatchingState
import dev.gaens.filmory.feature.watching.domain.DeleteWatching
import dev.gaens.filmory.feature.watching.domain.DetermineTitleStatus
import dev.gaens.filmory.feature.watching.domain.StartWatching
import dev.gaens.filmory.feature.watching.domain.SyncTitleStatus
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::DetermineTitleStatus)
    factoryOf(::SyncTitleStatus)
    factoryOf(::StartWatching)
    factoryOf(::ChangeWatchingState)
    factoryOf(::DeleteWatching)
    factoryOf(::ChangeTitleStatus)
    factoryOf(::AdvanceEpisode)
    factoryOf(::AddNote)
}
