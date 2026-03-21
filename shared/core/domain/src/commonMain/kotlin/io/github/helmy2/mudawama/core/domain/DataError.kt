package io.github.helmy2.mudawama.core.domain

sealed interface DataError : DomainError {
    enum class Remote : DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN,
        BAD_REQUEST,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        CONFLICT,
        PAYLOAD_TOO_LARGE,
        SERVICE_UNAVAILABLE
    }

    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN
    }
}
