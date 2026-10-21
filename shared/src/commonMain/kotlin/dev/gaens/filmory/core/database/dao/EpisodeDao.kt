package dev.gaens.filmory.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
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

    @Query("SELECT * FROM seasons ORDER BY titleId, number")
    abstract fun observeAllSeasons(): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM episodes ORDER BY number")
    abstract fun observeAllEpisodes(): Flow<List<EpisodeEntity>>

    @Query("DELETE FROM seasons WHERE titleId = :titleId")
    abstract suspend fun deleteSeasons(titleId: String)

    @Upsert
    abstract suspend fun upsertSeasons(seasons: List<SeasonEntity>)

    @Upsert
    abstract suspend fun upsertEpisodes(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM seasons WHERE titleId = :titleId AND id NOT IN (:keepIds)")
    abstract suspend fun deleteOtherSeasons(titleId: String, keepIds: List<String>)

    @Query(
        "DELETE FROM episodes WHERE seasonId IN (SELECT id FROM seasons WHERE titleId = :titleId) " +
            "AND id NOT IN (:keepIds)",
    )
    abstract suspend fun deleteOtherEpisodes(titleId: String, keepIds: List<String>)

    /** Replaces a series' structure. Rows with unchanged ids are updated in place, so their checks stay. */
    @Transaction
    open suspend fun replaceSeasons(
        titleId: String,
        seasons: List<SeasonEntity>,
        episodes: List<EpisodeEntity>,
    ) {
        deleteOtherEpisodes(titleId, episodes.map { it.id })
        deleteOtherSeasons(titleId, seasons.map { it.id })
        upsertSeasons(seasons)
        upsertEpisodes(episodes)
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
