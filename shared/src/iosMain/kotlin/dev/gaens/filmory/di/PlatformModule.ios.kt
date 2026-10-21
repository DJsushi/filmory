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
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private fun documentsPath(): String {
    val documentsDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentsDirectory?.path)
}

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        Room.databaseBuilder<AppDatabase>(name = documentsPath() + "/$DATABASE_NAME")
    }
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(produceFile = { (documentsPath() + "/$SETTINGS_FILE_NAME").toPath() })
    }
}
