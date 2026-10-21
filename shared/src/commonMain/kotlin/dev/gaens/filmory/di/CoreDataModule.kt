package dev.gaens.filmory.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.gaens.filmory.core.data.UuidIdGenerator
import dev.gaens.filmory.core.data.network.HttpClientFactory
import dev.gaens.filmory.core.database.AppDatabase
import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.feature.collection.data.CreateDefaultCollectionCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Clock
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    single { HttpClientFactory.build() }
    single {
        get<RoomDatabase.Builder<AppDatabase>>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addCallback(CreateDefaultCollectionCallback)
            .build()
    }
    single { get<AppDatabase>().titleDao() }
    single { get<AppDatabase>().watchingDao() }
    single { get<AppDatabase>().episodeDao() }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().collectionDao() }
    single { get<AppDatabase>().backupDao() }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single<Clock> { Clock.System }
    singleOf(::UuidIdGenerator).bind<IdGenerator>()
}
