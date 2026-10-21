package dev.gaens.filmory.core.domain.util

sealed interface DataError : Error {
    enum class Network : DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERIALIZATION,
        SERVER_ERROR,
        UNAUTHORIZED,
        NOT_FOUND,
        UNKNOWN,
    }

    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN,
    }
}
