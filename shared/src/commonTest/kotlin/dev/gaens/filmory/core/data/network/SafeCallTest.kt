package dev.gaens.filmory.core.data.network

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SafeCallTest {

    @Serializable
    private data class Dto(val name: String)

    private fun client(status: HttpStatusCode = HttpStatusCode.OK, body: String = "{}", fail: Boolean = false) =
        HttpClient(
            MockEngine {
                if (fail) throw IOException("offline")
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            },
        ) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

    private suspend fun HttpClient.call() = safeCall<Dto> { get("https://example.test") }

    @Test
    fun `successful response is decoded`() = runTest {
        val result = client(body = """{"name":"Heat"}""").call()

        assertEquals(Result.Success(Dto("Heat")), result)
    }

    @Test
    fun `malformed body maps to serialization error`() = runTest {
        val result = client(body = """{"other":1}""").call()

        assertEquals(Result.Error(DataError.Network.SERIALIZATION), result)
    }

    @Test
    fun `http statuses map to network errors`() = runTest {
        val expected = mapOf(
            HttpStatusCode.Unauthorized to DataError.Network.UNAUTHORIZED,
            HttpStatusCode.NotFound to DataError.Network.NOT_FOUND,
            HttpStatusCode.TooManyRequests to DataError.Network.TOO_MANY_REQUESTS,
            HttpStatusCode.InternalServerError to DataError.Network.SERVER_ERROR,
            HttpStatusCode(418, "Teapot") to DataError.Network.UNKNOWN,
        )

        expected.forEach { (status, error) ->
            assertEquals(Result.Error(error), client(status).call(), "status $status")
        }
    }

    @Test
    fun `io failure maps to no internet`() = runTest {
        val result = client(fail = true).call()

        assertEquals(Result.Error(DataError.Network.NO_INTERNET), result)
    }
}
