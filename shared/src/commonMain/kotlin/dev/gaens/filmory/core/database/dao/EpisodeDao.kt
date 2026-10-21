package dev.gaens.filmory.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.gaens.filmory.core.database.entity.EpisodeCheckEntity
import dev.gaens.filmory.core.database.entity.EpisodeEntity
import dev.gaens.filmory.core.database.entity.SeasonEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EpisodeDao {
    @Query("SELECT * FROM seasons WHERE titleId = :titleId ORDER BY number")
    abstract fun observeSeasons(titleId: String): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM seasons WHERE titleId = :titleId ORDER BY number")
    abstract suspend fun getSeasons(titleId: String): List<SeasonEntity>

    @Query(
        "SELECT episodes.* FROM episodes JOIN seasons ON seasons.id = episodes.seasonId " +
            "WHERE seasons.titleId = :titleId ORDER BY seasons.number, episodes.number",
    )
    abstract fun observeEpisodes(titleId: String): Flow<List<EpisodeEntity>>

    @Query(
        "SELECT episodes.* FROM episodes JOIN seasons ON seasons.id = episodes.seasonId " +
            "WHERE seasons.titleId = :titleId ORDER BY seasons.number, episodes.number",
    )
    abstract suspend fun getEpisodes(titleId: String): List<EpisodeEntity>

    @Query("DELETE FROM seasons WHERE titleId = :titleId")
    abstract suspend fun deleteSeasons(titleId: String)

    @Insert
    abstract suspend fun insertSeasons(seasons: List<SeasonEntity>)

    @Insert
    abstract suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Transaction
    open suspend fun replaceSeasons(
        titleId: String,
        seasons: List<SeasonEntity>,
        episodes: List<EpisodeEntity>,
    ) {
        deleteSeasons(titleId)
        insertSeasons(seasons)
        insertEpisodes(episodes)
    }

    @Query("SELECT * FROM episode_checks WHERE watchingId = :watchingId")
    abstract fun observeChecks(watchingId: String): Flow<List<EpisodeCheckEntity>>

    @Query("SELECT * FROM episode_checks")
    abstract fun observeAllChecks(): Flow<List<EpisodeCheckEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertCheck(check: EpisodeCheckEntity)

    @Query("DELETE FROM episode_checks WHERE watchingId = :watchingId AND episodeId = :episodeId")
    abstract suspend fun deleteCheck(watchingId: String, episodeId: String)
}
