package dev.gaens.filmory.feature.search.domain

import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.episode.domain.Episode
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.episode.domain.Season
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.title.domain.TitleStatus
import kotlin.time.Clock

/**
 * Adds a search result to the library with its full details and, for series, every season and
 * episode. A title that is already in the library is not added twice; its id is returned.
 */
class AddTitleFromRemote(
    private val remote: RemoteTitleDataSource,
    private val titleRepository: TitleRepository,
    private val episodeRepository: EpisodeRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(result: TitleSearchResult): Result<String, DataError> {
        titleRepository.getTitleByTmdbId(result.tmdbId, result.type)?.let { return Result.Success(it.id) }
        val details = when (val fetched = remote.getDetails(result.tmdbId, result.type)) {
            is Result.Error -> return fetched
            is Result.Success -> fetched.data
        }
        val now = clock.now()
        val title = Title(
            id = idGenerator.newId(),
            type = details.type,
            name = details.name,
            year = details.year,
            genre = details.genre,
            runtimeMinutes = details.runtimeMinutes,
            creator = details.creator,
            overview = details.overview,
            posterUrl = details.posterUrl,
            tmdbId = details.tmdbId,
            status = TitleStatus.NOT_WATCHED,
            isWishlisted = false,
            watchLaterPosition = null,
            addedAt = now,
            updatedAt = now,
        )
        val saved = titleRepository.upsertTitle(title)
        if (saved is Result.Error) return saved
        if (details.seasons.isNotEmpty()) {
            val seasons = details.seasons.map { remoteSeason ->
                val seasonId = idGenerator.newId()
                Season(
                    id = seasonId,
                    titleId = title.id,
                    number = remoteSeason.number,
                    episodes = remoteSeason.episodes.map {
                        Episode(idGenerator.newId(), seasonId, it.number, it.name, it.runtimeMinutes)
                    },
                )
            }
            val savedSeasons = episodeRepository.replaceSeasons(title.id, seasons)
            if (savedSeasons is Result.Error) return savedSeasons
        }
        return Result.Success(title.id)
    }
}
