package dev.gaens.filmory.feature.collection.data

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import dev.gaens.filmory.feature.collection.domain.TitleCollection

private const val DEFAULT_COLLECTION_NAME = "Favorites"

/** Gives every new database its "Favorites" collection. */
object CreateDefaultCollectionCallback : RoomDatabase.Callback() {
    override fun onCreate(connection: SQLiteConnection) {
        connection.execSQL(
            "INSERT INTO collections (id, name, createdAtEpochMillis) " +
                "VALUES ('${TitleCollection.DEFAULT_COLLECTION_ID}', '$DEFAULT_COLLECTION_NAME', 0)",
        )
    }
}
