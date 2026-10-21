package dev.gaens.filmory.core.presentation.ui

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.error_disk_full
import dev.gaens.filmory.resources.error_no_internet
import dev.gaens.filmory.resources.error_not_found
import dev.gaens.filmory.resources.error_request_timeout
import dev.gaens.filmory.resources.error_serialization
import dev.gaens.filmory.resources.error_server
import dev.gaens.filmory.resources.error_too_many_requests
import dev.gaens.filmory.resources.error_unauthorized
import dev.gaens.filmory.resources.error_unknown

fun DataError.asUiText(): UiText = UiText.Resource(
    when (this) {
        DataError.Local.DISK_FULL -> Res.string.error_disk_full
        DataError.Local.NOT_FOUND, DataError.Network.NOT_FOUND -> Res.string.error_not_found
        DataError.Local.UNKNOWN, DataError.Network.UNKNOWN -> Res.string.error_unknown
        DataError.Network.NO_INTERNET -> Res.string.error_no_internet
        DataError.Network.REQUEST_TIMEOUT -> Res.string.error_request_timeout
        DataError.Network.TOO_MANY_REQUESTS -> Res.string.error_too_many_requests
        DataError.Network.SERVER_ERROR -> Res.string.error_server
        DataError.Network.UNAUTHORIZED -> Res.string.error_unauthorized
        DataError.Network.SERIALIZATION -> Res.string.error_serialization
    },
)
