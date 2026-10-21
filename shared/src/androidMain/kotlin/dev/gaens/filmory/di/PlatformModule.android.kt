package dev.gaens.filmory.di

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.gaens.filmory.core.database.AppDatabase
import dev.gaens.filmory.core.database.DATABASE_NAME
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
}
