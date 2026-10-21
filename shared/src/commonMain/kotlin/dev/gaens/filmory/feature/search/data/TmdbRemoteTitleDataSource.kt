package dev.gaens.filmory.feature.search.data

import dev.gaens.filmory.core.data.network.TmdbConfig
import dev.gaens.filmory.core.data.network.safeCall
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.core.domain.util.map
import dev.gaens.filmory.feature.search.domain.RemoteTitleDataSource
import dev.gaens.filmory.feature.search.domain.RemoteTitleDetails
import dev.gaens.filmory.feature.search.domain.TitleSearchResult
import dev.gaens.filmory.feature.title.domain.TitleType
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private const val BASE_URL = "https://api.themoviedb.org/3"

class TmdbRemoteTitleDataSource(
    private val httpClient: HttpClient,
    private val config: TmdbConfig,
) : RemoteTitleDataSource {

    override suspend fun search(query: String): Result<List<TitleSearchResult>, DataError.Network> =
        safeCall<SearchResponseDto> {
            httpClient.get("$BASE_URL/search/multi") {
                authorize()
                parameter("query", query)
                parameter("include_adult", false)
            }
        }.map { response -> response.results.mapNotNull { it.toSearchResult() } }

    override suspend fun getDetails(tmdbId: Long, type: TitleType): Result<RemoteTitleDetails, DataError.Network> =
        when (type) {
            TitleType.MOVIE -> safeCall<MovieDetailsDto> {
                httpClient.get("$BASE_URL/movie/$tmdbId") {
                    authorize()
                    parameter("append_to_response", "credits")
                }
            }.map { it.toDetails() }
            TitleType.SERIES -> getSeriesDetails(tmdbId)
        }

    private suspend fun getSeriesDetails(tmdbId: Long): Result<RemoteTitleDetails, DataError.Network> {
        val series = when (val result = safeCall<TvDetailsDto> { httpClient.get("$BASE_URL/tv/$tmdbId") { authorize() } }) {
            is Result.Error -> return result
            is Result.Success -> result.data
        }
        // Season 0 holds specials, which are not part of the watch order.
        val seasonNumbers = series.seasons.map { it.seasonNumber }.filter { it > 0 }
        val seasons = coroutineScope {
            seasonNumbers.map { number ->
                async { safeCall<SeasonDto> { httpClient.get("$BASE_URL/tv/$tmdbId/season/$number") { authorize() } } }
            }.awaitAll()
        }
        seasons.filterIsInstance<Result.Error<DataError.Network>>().firstOrNull()?.let { return it }
        return Result.Success(series.toDetails(seasons.filterIsInstance<Result.Success<SeasonDto>>().map { it.data }))
    }

    private fun HttpRequestBuilder.authorize() = parameter("api_key", config.apiKey)
}
