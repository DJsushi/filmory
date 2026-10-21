package dev.gaens.filmory.feature.search.data

import dev.gaens.filmory.feature.search.domain.RemoteEpisode
import dev.gaens.filmory.feature.search.domain.RemoteSeason
import dev.gaens.filmory.feature.search.domain.RemoteTitleDetails
import dev.gaens.filmory.feature.search.domain.TitleSearchResult
import dev.gaens.filmory.feature.title.domain.TitleType

private const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
private const val DIRECTOR_JOB = "Director"
private const val YEAR_LENGTH = 4

internal fun posterUrl(path: String?): String? = path?.takeIf { it.isNotBlank() }?.let { POSTER_BASE_URL + it }

internal fun yearOf(date: String?): Int? = date?.take(YEAR_LENGTH)?.toIntOrNull()

/** Movies and series only; people and other media types are dropped. */
fun SearchResultDto.toSearchResult(): TitleSearchResult? {
    val type = when (mediaType) {
        "movie" -> TitleType.MOVIE
        "tv" -> TitleType.SERIES
        else -> return null
    }
    val name = (title ?: name)?.takeIf { it.isNotBlank() } ?: return null
    return TitleSearchResult(
        tmdbId = id,
        type = type,
        name = name,
        year = yearOf(releaseDate ?: firstAirDate),
        posterUrl = posterUrl(posterPath),
        overview = overview?.takeIf { it.isNotBlank() },
    )
}

fun MovieDetailsDto.toDetails(): RemoteTitleDetails = RemoteTitleDetails(
    tmdbId = id,
    type = TitleType.MOVIE,
    name = title,
    year = yearOf(releaseDate),
    genre = genres.firstOrNull()?.name,
    runtimeMinutes = runtime?.takeIf { it > 0 },
    creator = credits.crew.filter { it.job == DIRECTOR_JOB }.joinToString(", ") { it.name }.ifBlank { null },
    overview = overview?.takeIf { it.isNotBlank() },
    posterUrl = posterUrl(posterPath),
    seasons = emptyList(),
)

fun TvDetailsDto.toDetails(seasons: List<SeasonDto>): RemoteTitleDetails = RemoteTitleDetails(
    tmdbId = id,
    type = TitleType.SERIES,
    name = name,
    year = yearOf(firstAirDate),
    genre = genres.firstOrNull()?.name,
    runtimeMinutes = null,
    creator = createdBy.joinToString(", ") { it.name }.ifBlank { null },
    overview = overview?.takeIf { it.isNotBlank() },
    posterUrl = posterUrl(posterPath),
    seasons = seasons.sortedBy { it.seasonNumber }.map { season ->
        RemoteSeason(
            number = season.seasonNumber,
            episodes = season.episodes.map { RemoteEpisode(it.episodeNumber, it.name?.takeIf(String::isNotBlank), it.runtime) },
        )
    },
)
