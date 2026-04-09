package io.github.helmy2.mudawama.core.location

import io.github.helmy2.mudawama.core.domain.DomainError

sealed interface LocationError : DomainError {
    data object PermissionDenied : LocationError
    data object LocationUnavailable : LocationError
    data class UnknownError(val message: String) : LocationError
}
