package io.github.helmy2.mudawama.core.common.location

import io.github.helmy2.mudawama.core.domain.DomainError
import io.github.helmy2.mudawama.core.domain.Result

interface LocationProvider {
    /**
     * Checks if the app currently has location permissions.
     */
    fun hasPermission(): Boolean

    /**
     * Gets the current location of the device.
     * Returns a `Result` to encapsulate platform-specific errors (e.g. permission denied, disabled GPS).
     */
    suspend fun getCurrentLocation(): Result<Coordinates, LocationError>

    sealed interface LocationError : DomainError {
        data object PermissionDenied : LocationError
        data object LocationUnavailable : LocationError
        data class UnknownError(val message: String) : LocationError
    }
}
