package dev.gaens.filmory.feature.search.data

import dev.gaens.filmory.core.data.network.TmdbConfig
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.title.domain.TitleType
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TmdbRemoteTitleDataSourceTest {
    private val requestedKeys = mutableListOf<String?>()

    private fun dataSource(status: HttpStatusCode = HttpStatusCode.OK) = TmdbRemoteTitleDataSource(
        HttpClient(
            MockEngine { request ->
                requestedKeys += request.url.parameters["api_key"]
                val body = when (request.url.encodedPath) {
                    "/3/search/multi" -> SEARCH
                    "/3/movie/329865" -> MOVIE
                    "/3/tv/95396" -> TV
                    "/3/tv/95396/season/1" -> SEASON_1
                    "/3/tv/95396/season/2" -> SEASON_2
                    else -> "{}"
                }
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            },
        ) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } },
        TmdbConfig("secret"),
    )

    @Test
    fun `search keeps movies and series and maps their fields`() = runTest {
        val results = (dataSource().search("sev") as Result.Success).data

        assertEquals(listOf(TitleType.MOVIE, TitleType.SERIES), results.map { it.type })
        assertEquals("Arrival", results[0].name)
        assertEquals(2016, results[0].year)
        assertEquals("https://image.tmdb.org/t/p/w500/arrival.jpg", results[0].posterUrl)
        assertEquals("Severance", results[1].name)
        assertEquals(2022, results[1].year)
        assertEquals(listOf<String?>("secret"), requestedKeys)
    }

    @Test
    fun `movie details carry runtime, first genre and director`() = runTest {
        val movie = (dataSource().getDetails(329865, TitleType.MOVIE) as Result.Success).data

        assertEquals(116, movie.runtimeMinutes)
        assertEquals("Science Fiction", movie.genre)
        assertEquals("Denis Villeneuve", movie.creator)
    }

    @Test
    fun `series details load every season except specials`() = runTest {
        val series = (dataSource().getDetails(95396, TitleType.SERIES) as Result.Success).data

        assertEquals(listOf(1, 2), series.seasons.map { it.number })
        assertEquals(listOf(1, 2), series.seasons[0].episodes.map { it.number })
        assertEquals("Good News About Hell", series.seasons[0].episodes[0].name)
        assertEquals("Dan Erickson", series.creator)
    }

    @Test
    fun `a rejected key maps to unauthorized`() = runTest {
        assertEquals(Result.Error(DataError.Network.UNAUTHORIZED), dataSource(HttpStatusCode.Unauthorized).search("x"))
    }

    private companion object {
        const val SEARCH = """{"results":[
            {"id":329865,"media_type":"movie","title":"Arrival","release_date":"2016-11-10","poster_path":"/arrival.jpg"},
            {"id":95396,"media_type":"tv","name":"Severance","first_air_date":"2022-02-17","poster_path":null},
            {"id":1,"media_type":"person","name":"Someone"}
        ]}"""
        const val MOVIE = """{"id":329865,"title":"Arrival","release_date":"2016-11-10","runtime":116,
            "genres":[{"id":878,"name":"Science Fiction"},{"id":18,"name":"Drama"}],
            "credits":{"crew":[{"job":"Producer","name":"Shawn Levy"},{"job":"Director","name":"Denis Villeneuve"}]}}"""
        const val TV = """{"id":95396,"name":"Severance","first_air_date":"2022-02-17","created_by":[{"name":"Dan Erickson"}],
            "seasons":[{"season_number":0,"episode_count":3},{"season_number":1,"episode_count":2},{"season_number":2,"episode_count":1}]}"""
        const val SEASON_1 = """{"season_number":1,"episodes":[
            {"episode_number":1,"name":"Good News About Hell","runtime":57},{"episode_number":2,"name":"Half Loop","runtime":53}]}"""
        const val SEASON_2 = """{"season_number":2,"episodes":[{"episode_number":1,"name":"Hello, Ms. Cobel","runtime":null}]}"""
    }
}
