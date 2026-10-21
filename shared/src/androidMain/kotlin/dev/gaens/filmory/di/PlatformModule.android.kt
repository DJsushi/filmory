package dev.gaens.filmory.di

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.gaens.filmory.core.database.AppDatabase
import dev.gaens.filmory.core.database.DATABASE_NAME
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dev.gaens.filmory.feature.settings.data.SETTINGS_FILE_NAME
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val context = androidContext()
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = context.getDatabasePath(DATABASE_NAME).absolutePath,
        )
    }
    single<DataStore<Preferences>> {
        val file = androidContext().filesDir.resolve(SETTINGS_FILE_NAME)
        PreferenceDataStoreFactory.createWithPath(produceFile = { file.absolutePath.toPath() })
    }
}
