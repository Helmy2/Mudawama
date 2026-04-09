package io.github.helmy2.mudawama.core.common.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.domain.error.Error
import kotlinx.coroutines.tasks.await

class AndroidLocationProvider(
    private val context: Context
) : LocationProvider {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun getCurrentLocation(): Result<Coordinates> {
        if (!hasPermission()) {
            return Result.Failure(LocationError.PermissionDenied)
        }

        return try {
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (location != null) {
                Result.Success(Coordinates(location.latitude, location.longitude))
            } else {
                Result.Failure(LocationError.LocationUnavailable)
            }
        } catch (e: Exception) {
            Result.Failure(LocationError.UnknownError(e.message ?: "Unknown error"))
        }
    }

    sealed interface LocationError : Error {
        data object PermissionDenied : LocationError
        data object LocationUnavailable : LocationError
        data class UnknownError(val message: String) : LocationError
    }
}
