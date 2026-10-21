package dev.gaens.filmory.di

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.gaens.filmory.core.database.AppDatabase
import dev.gaens.filmory.core.database.DATABASE_NAME
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val documentsDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        Room.databaseBuilder<AppDatabase>(
            name = requireNotNull(documentsDirectory?.path) + "/$DATABASE_NAME",
        )
    }
}
