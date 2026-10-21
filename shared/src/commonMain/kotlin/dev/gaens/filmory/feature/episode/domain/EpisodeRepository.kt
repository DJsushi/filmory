package dev.gaens.filmory.feature.episode.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface EpisodeRepository {
    /** Seasons ordered by number, each with its episodes ordered by number. */
    fun observeSeasons(titleId: String): Flow<List<Season>>
    suspend fun getSeasons(titleId: String): List<Season>

    /** Replaces a series' whole structure. Checks on removed episodes are removed too. */
    suspend fun replaceSeasons(titleId: String, seasons: List<Season>): EmptyResult<DataError.Local>

    fun observeChecks(watchingId: String): Flow<List<EpisodeCheck>>
    fun observeAllChecks(): Flow<List<EpisodeCheck>>

    /** Does nothing when the episode is already checked in that watching. */
    suspend fun checkEpisode(check: EpisodeCheck): EmptyResult<DataError.Local>
    suspend fun uncheckEpisode(watchingId: String, episodeId: String): EmptyResult<DataError.Local>
}
