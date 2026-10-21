package dev.gaens.filmory.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.gaens.filmory.core.database.entity.WatchingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchingDao {
    @Query("SELECT * FROM watchings WHERE titleId = :titleId ORDER BY createdAtEpochMillis DESC")
    fun observeWatchings(titleId: String): Flow<List<WatchingEntity>>

    @Query("SELECT * FROM watchings")
    fun observeAllWatchings(): Flow<List<WatchingEntity>>

    @Query("SELECT * FROM watchings WHERE id = :id")
    suspend fun getWatching(id: String): WatchingEntity?

    @Query("SELECT * FROM watchings WHERE titleId = :titleId ORDER BY createdAtEpochMillis DESC")
    suspend fun getWatchings(titleId: String): List<WatchingEntity>

    @Upsert
    suspend fun upsertWatching(watching: WatchingEntity)

    @Query("DELETE FROM watchings WHERE id = :id")
    suspend fun deleteWatching(id: String)
}
