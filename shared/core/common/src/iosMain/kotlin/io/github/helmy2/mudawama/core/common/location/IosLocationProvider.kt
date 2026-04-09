package io.github.helmy2.mudawama.core.common.location

import io.github.helmy2.mudawama.core.domain.Result
import kotlinx.cinterop.ExperimentalForeignApi
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

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getCurrentLocation(): Result<Coordinates, LocationProvider.LocationError> {
        if (!hasPermission()) {
            return Result.Failure(LocationProvider.LocationError.PermissionDenied)
        }

        // Simplistic implementation — in a real app this would use a delegate
        // to wait for the location update asynchronously.
        val location = locationManager.location
        return if (location != null) {
            val lat = location.coordinate.useContents { latitude }
            val lon = location.coordinate.useContents { longitude }
            Result.Success(Coordinates(lat, lon))
        } else {
            Result.Failure(LocationProvider.LocationError.LocationUnavailable)
        }
    }
}
