package dev.gaens.filmory.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.gaens.filmory.core.data.network.HttpClientFactory
import dev.gaens.filmory.core.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val coreDataModule = module {
    single { HttpClientFactory.build() }
    single {
        get<RoomDatabase.Builder<AppDatabase>>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}
