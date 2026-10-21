package dev.gaens.filmory.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import dev.gaens.filmory.core.database.dao.BackupDao
import dev.gaens.filmory.core.database.dao.CollectionDao
import dev.gaens.filmory.core.database.dao.EpisodeDao
import dev.gaens.filmory.core.database.dao.NoteDao
import dev.gaens.filmory.core.database.dao.TitleDao
import dev.gaens.filmory.core.database.dao.WatchingDao
import dev.gaens.filmory.core.database.entity.CollectionEntity
import dev.gaens.filmory.core.database.entity.CollectionTitleEntity
import dev.gaens.filmory.core.database.entity.EpisodeCheckEntity
import dev.gaens.filmory.core.database.entity.EpisodeEntity
import dev.gaens.filmory.core.database.entity.NoteCommentEntity
import dev.gaens.filmory.core.database.entity.NoteEntity
import dev.gaens.filmory.core.database.entity.SeasonEntity
import dev.gaens.filmory.core.database.entity.TitleEntity
import dev.gaens.filmory.core.database.entity.WatchingEntity

internal const val DATABASE_NAME = "filmory.db"

@Database(
    entities = [
        TitleEntity::class,
        WatchingEntity::class,
        SeasonEntity::class,
        EpisodeEntity::class,
        EpisodeCheckEntity::class,
        NoteEntity::class,
        NoteCommentEntity::class,
        CollectionEntity::class,
        CollectionTitleEntity::class,
    ],
    version = 1,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun titleDao(): TitleDao
    abstract fun watchingDao(): WatchingDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun noteDao(): NoteDao
    abstract fun collectionDao(): CollectionDao
    abstract fun backupDao(): BackupDao
}

// Room generates the actual implementations.
@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
