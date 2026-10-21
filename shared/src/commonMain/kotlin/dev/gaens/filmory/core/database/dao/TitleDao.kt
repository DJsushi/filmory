package dev.gaens.filmory.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.gaens.filmory.core.database.entity.TitleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TitleDao {
    @Query("SELECT * FROM titles ORDER BY addedAtEpochMillis DESC")
    fun observeTitles(): Flow<List<TitleEntity>>

    @Query("SELECT * FROM titles WHERE id = :id")
    fun observeTitle(id: String): Flow<TitleEntity?>

    @Query("SELECT * FROM titles WHERE id = :id")
    suspend fun getTitle(id: String): TitleEntity?

    @Query("SELECT * FROM titles WHERE tmdbId = :tmdbId AND type = :type LIMIT 1")
    suspend fun getTitleByTmdbId(tmdbId: Long, type: String): TitleEntity?

    @Upsert
    suspend fun upsertTitle(title: TitleEntity)

    @Query("DELETE FROM titles WHERE id = :id")
    suspend fun deleteTitle(id: String)
}
