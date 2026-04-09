package io.github.helmy2.mudawama.core.location

import io.github.helmy2.mudawama.core.domain.Result
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import kotlinx.cinterop.useContents
import kotlinx.cinterop.ExperimentalForeignApi

class IosLocationProvider : LocationProvider {

    private val locationManager = CLLocationManager()

    override fun hasPermission(): Boolean {
        val status = locationManager.authorizationStatus
        return status == kCLAuthorizationStatusAuthorizedAlways || 
               status == kCLAuthorizationStatusAuthorizedWhenInUse
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getCurrentLocation(): Result<Coordinates, LocationError> {
        if (!hasPermission()) {
            return Result.Failure(LocationError.PermissionDenied)
        }

        val location = locationManager.location
        return if (location != null) {
            Result.Success(Coordinates(location.coordinate.useContents { latitude }, location.coordinate.useContents { longitude }))
        } else {
            Result.Failure(LocationError.LocationUnavailable)
        }
    }
}
