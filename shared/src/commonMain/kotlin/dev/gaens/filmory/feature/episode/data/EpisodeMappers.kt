package dev.gaens.filmory.feature.episode.data

import dev.gaens.filmory.core.data.toEpochMillis
import dev.gaens.filmory.core.data.toInstant
import dev.gaens.filmory.core.database.entity.EpisodeCheckEntity
import dev.gaens.filmory.core.database.entity.EpisodeEntity
import dev.gaens.filmory.core.database.entity.SeasonEntity
import dev.gaens.filmory.feature.episode.domain.Episode
import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import dev.gaens.filmory.feature.episode.domain.Season

fun EpisodeEntity.toEpisode(): Episode =
    Episode(id = id, seasonId = seasonId, number = number, name = name, runtimeMinutes = runtimeMinutes)

fun Episode.toEpisodeEntity(): EpisodeEntity =
    EpisodeEntity(id = id, seasonId = seasonId, number = number, name = name, runtimeMinutes = runtimeMinutes)

fun Season.toSeasonEntity(): SeasonEntity = SeasonEntity(id = id, titleId = titleId, number = number)

/** Builds seasons from flat rows, both already sorted by number. */
fun toSeasons(seasons: List<SeasonEntity>, episodes: List<EpisodeEntity>): List<Season> {
    val episodesBySeason = episodes.groupBy { it.seasonId }
    return seasons.map { season ->
        Season(
            id = season.id,
            titleId = season.titleId,
            number = season.number,
            episodes = episodesBySeason[season.id].orEmpty().map { it.toEpisode() },
        )
    }
}

fun EpisodeCheckEntity.toEpisodeCheck(): EpisodeCheck = EpisodeCheck(
    id = id,
    watchingId = watchingId,
    episodeId = episodeId,
    checkedAt = checkedAtEpochMillis.toInstant(),
)

fun EpisodeCheck.toEpisodeCheckEntity(): EpisodeCheckEntity = EpisodeCheckEntity(
    id = id,
    watchingId = watchingId,
    episodeId = episodeId,
    checkedAtEpochMillis = checkedAt.toEpochMillis(),
)
