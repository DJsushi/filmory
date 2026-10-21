package dev.gaens.filmory.di

import dev.gaens.filmory.feature.backup.data.ZipBackupRepository
import dev.gaens.filmory.feature.backup.domain.BackupRepository
import dev.gaens.filmory.feature.collection.data.OfflineFirstCollectionRepository
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.episode.data.OfflineFirstEpisodeRepository
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.note.data.FileNoteImageStorage
import dev.gaens.filmory.feature.note.data.OfflineFirstNoteRepository
import dev.gaens.filmory.feature.note.domain.NoteImageStorage
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.title.data.OfflineFirstTitleRepository
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.watching.data.OfflineFirstWatchingRepository
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import dev.gaens.filmory.feature.search.data.TmdbRemoteTitleDataSource
import dev.gaens.filmory.feature.search.domain.RemoteTitleDataSource
import dev.gaens.filmory.feature.settings.data.DataStoreSettingsRepository
import dev.gaens.filmory.feature.settings.domain.SettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureDataModule = module {
    singleOf(::OfflineFirstTitleRepository).bind<TitleRepository>()
    singleOf(::OfflineFirstWatchingRepository).bind<WatchingRepository>()
    singleOf(::OfflineFirstEpisodeRepository).bind<EpisodeRepository>()
    singleOf(::OfflineFirstNoteRepository).bind<NoteRepository>()
    singleOf(::OfflineFirstCollectionRepository).bind<CollectionRepository>()
    singleOf(::FileNoteImageStorage).bind<NoteImageStorage>()
    singleOf(::TmdbRemoteTitleDataSource).bind<RemoteTitleDataSource>()
    singleOf(::ZipBackupRepository).bind<BackupRepository>()
    singleOf(::DataStoreSettingsRepository).bind<SettingsRepository>()
}
