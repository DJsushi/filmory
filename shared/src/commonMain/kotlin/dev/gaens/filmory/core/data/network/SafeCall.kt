package dev.gaens.filmory.core.data.network

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: HttpRequestTimeoutException) {
        return Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: IOException) {
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SerializationException) {
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        return Result.Error(DataError.Network.UNKNOWN)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, DataError.Network> =
    when (response.status.value) {
        in 200..299 -> try {
            Result.Success(response.body<T>())
        } catch (e: NoTransformationFoundException) {
            Result.Error(DataError.Network.SERIALIZATION)
        } catch (e: ContentConvertException) {
            Result.Error(DataError.Network.SERIALIZATION)
        } catch (e: SerializationException) {
            Result.Error(DataError.Network.SERIALIZATION)
        }
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        404 -> Result.Error(DataError.Network.NOT_FOUND)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
