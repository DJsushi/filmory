package dev.gaens.filmory.di

import dev.gaens.filmory.feature.episode.domain.AdvanceEpisode
import dev.gaens.filmory.feature.library.domain.BuildLibrary
import dev.gaens.filmory.feature.note.domain.AddNote
import dev.gaens.filmory.feature.note.domain.DeleteNote
import dev.gaens.filmory.feature.title.domain.DeleteTitle
import dev.gaens.filmory.feature.watched.domain.BuildWatchedList
import dev.gaens.filmory.feature.title.domain.ChangeTitleStatus
import dev.gaens.filmory.feature.title.domain.MoveWatchLater
import dev.gaens.filmory.feature.title.domain.ToggleWatchLater
import dev.gaens.filmory.feature.watching.domain.ChangeWatchingState
import dev.gaens.filmory.feature.watching.domain.DeleteWatching
import dev.gaens.filmory.feature.watching.domain.DetermineTitleStatus
import dev.gaens.filmory.feature.watching.domain.EnsureActiveWatching
import dev.gaens.filmory.feature.watching.domain.SaveWatching
import dev.gaens.filmory.feature.watching.domain.FindContinueWatching
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
    factoryOf(::FindContinueWatching)
    factoryOf(::ToggleWatchLater)
    factoryOf(::MoveWatchLater)
    factoryOf(::BuildLibrary)
    factoryOf(::SaveWatching)
    factoryOf(::DeleteNote)
    factoryOf(::DeleteTitle)
    factoryOf(::BuildWatchedList)
    factoryOf(::EnsureActiveWatching)
}
