package dev.gaens.filmory.feature.episode.data

import dev.gaens.filmory.core.data.safeDatabaseWrite
import dev.gaens.filmory.core.database.dao.EpisodeDao
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.episode.domain.Season
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class OfflineFirstEpisodeRepository(
    private val episodeDao: EpisodeDao,
) : EpisodeRepository {

    override fun observeSeasons(titleId: String): Flow<List<Season>> =
        combine(episodeDao.observeSeasons(titleId), episodeDao.observeEpisodes(titleId), ::toSeasons)

    override suspend fun getSeasons(titleId: String): List<Season> =
        toSeasons(episodeDao.getSeasons(titleId), episodeDao.getEpisodes(titleId))

    override suspend fun replaceSeasons(titleId: String, seasons: List<Season>): EmptyResult<DataError.Local> =
        safeDatabaseWrite {
            episodeDao.replaceSeasons(
                titleId = titleId,
                seasons = seasons.map { it.toSeasonEntity() },
                episodes = seasons.flatMap { season -> season.episodes.map { it.toEpisodeEntity() } },
            )
        }

    override fun observeChecks(watchingId: String): Flow<List<EpisodeCheck>> =
        episodeDao.observeChecks(watchingId).map { list -> list.map { it.toEpisodeCheck() } }

    override fun observeAllChecks(): Flow<List<EpisodeCheck>> =
        episodeDao.observeAllChecks().map { list -> list.map { it.toEpisodeCheck() } }

    override suspend fun checkEpisode(check: EpisodeCheck): EmptyResult<DataError.Local> =
        safeDatabaseWrite { episodeDao.insertCheck(check.toEpisodeCheckEntity()) }

    override suspend fun uncheckEpisode(watchingId: String, episodeId: String): EmptyResult<DataError.Local> =
        safeDatabaseWrite { episodeDao.deleteCheck(watchingId, episodeId) }
}
