package io.github.helmy2.mudawama.core.data.networking

import io.github.helmy2.mudawama.core.domain.DataError
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

fun Throwable.toRemoteError(): DataError.Remote =
    when (this) {
        is ResponseException -> response.status.toDataError()
        is SerializationException -> DataError.Remote.SERIALIZATION
        is IOException -> DataError.Remote.NO_INTERNET
        else -> DataError.Remote.UNKNOWN
    }

fun Throwable.toLocalError(): DataError.Local =
    when (this) {
        is IOException -> DataError.Local.DISK_FULL
        else -> DataError.Local.UNKNOWN
    }

private fun HttpStatusCode.toDataError(): DataError.Remote =
    when (value) {
        400 -> DataError.Remote.BAD_REQUEST
        401 -> DataError.Remote.UNAUTHORIZED
        403 -> DataError.Remote.FORBIDDEN
        404 -> DataError.Remote.NOT_FOUND
        408 -> DataError.Remote.REQUEST_TIMEOUT
        409 -> DataError.Remote.CONFLICT
        413 -> DataError.Remote.PAYLOAD_TOO_LARGE
        429 -> DataError.Remote.TOO_MANY_REQUESTS
        503 -> DataError.Remote.SERVICE_UNAVAILABLE
        in 500..599 -> DataError.Remote.SERVER_ERROR
        else -> DataError.Remote.UNKNOWN
    }
