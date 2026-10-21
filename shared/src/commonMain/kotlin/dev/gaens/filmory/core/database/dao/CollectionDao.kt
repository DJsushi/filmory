package dev.gaens.filmory.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import dev.gaens.filmory.core.database.entity.CollectionEntity
import dev.gaens.filmory.core.database.entity.CollectionTitleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY createdAtEpochMillis")
    fun observeCollections(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collection_titles")
    fun observeCollectionTitles(): Flow<List<CollectionTitleEntity>>

    @Upsert
    suspend fun upsertCollection(collection: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollection(id: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTitle(link: CollectionTitleEntity)

    @Query("DELETE FROM collection_titles WHERE collectionId = :collectionId AND titleId = :titleId")
    suspend fun removeTitle(collectionId: String, titleId: String)
}
