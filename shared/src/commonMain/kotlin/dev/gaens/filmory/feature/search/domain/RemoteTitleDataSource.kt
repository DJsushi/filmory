package dev.gaens.filmory.feature.search.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.title.domain.TitleType

/** A metadata source for titles (TMDB today). Hidden behind this interface so it can be swapped. */
interface RemoteTitleDataSource {
    suspend fun search(query: String): Result<List<TitleSearchResult>, DataError.Network>
    suspend fun getDetails(tmdbId: Long, type: TitleType): Result<RemoteTitleDetails, DataError.Network>
}

data class TitleSearchResult(
    val tmdbId: Long,
    val type: TitleType,
    val name: String,
    val year: Int?,
    val posterUrl: String?,
    val overview: String?,
)

data class RemoteTitleDetails(
    val tmdbId: Long,
    val type: TitleType,
    val name: String,
    val year: Int?,
    val genre: String?,
    val runtimeMinutes: Int?,
    val creator: String?,
    val overview: String?,
    val posterUrl: String?,
    val seasons: List<RemoteSeason>,
)

data class RemoteSeason(val number: Int, val episodes: List<RemoteEpisode>)

data class RemoteEpisode(val number: Int, val name: String?, val runtimeMinutes: Int?)
