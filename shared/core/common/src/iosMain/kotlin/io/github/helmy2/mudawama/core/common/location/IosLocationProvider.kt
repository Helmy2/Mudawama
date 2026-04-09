package io.github.helmy2.mudawama.core.common.location

import io.github.helmy2.mudawama.core.domain.Result
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

class IosLocationProvider : LocationProvider {

    private val locationManager = CLLocationManager()

    override fun hasPermission(): Boolean {
        val status = locationManager.authorizationStatus
        return status == kCLAuthorizationStatusAuthorizedAlways || 
               status == kCLAuthorizationStatusAuthorizedWhenInUse
    }

    override suspend fun getCurrentLocation(): Result<Coordinates> {
        if (!hasPermission()) {
            return Result.Failure(LocationError.PermissionDenied)
        }

        // Simplistic implementation for now, in a real app this would use a delegate
        // to wait for the location update asynchronously.
        val location = locationManager.location
        return if (location != null) {
            Result.Success(Coordinates(location.coordinate.useContents { latitude }, location.coordinate.useContents { longitude }))
        } else {
            Result.Failure(LocationError.LocationUnavailable)
        }
    }

    sealed interface LocationError : Error {
        data object PermissionDenied : LocationError
        data object LocationUnavailable : LocationError
        data class UnknownError(val message: String) : LocationError
    }
}
