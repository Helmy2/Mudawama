package io.github.helmy2.mudawama.feature.qibla.data.sensor

import io.github.helmy2.mudawama.feature.qibla.domain.model.CompassAccuracy
import io.github.helmy2.mudawama.feature.qibla.domain.model.CompassHeading
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual class CompassSensorManager {

    actual fun observeHeading(): Flow<CompassHeading> = callbackFlow {
        var manager: CLLocationManager? = null
        var locationDelegate: NSObject? = null
        
        try {
            val mgr = CLLocationManager()
            manager = mgr
            
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
                    val heading = didUpdateHeading.trueHeading.takeIf { it >= 0 }
                        ?: didUpdateHeading.magneticHeading
                    val accuracy = mapAccuracy(didUpdateHeading.headingAccuracy)
                    
                    trySend(CompassHeading(heading, accuracy))
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    // Send unreliable heading on error
                    trySend(CompassHeading(0.0, CompassAccuracy.UNRELIABLE))
                }
            }
            
            locationDelegate = delegate
            mgr.delegate = delegate
            mgr.startUpdatingHeading()
        } catch (e: Exception) {
            // If initialization fails, send error heading
            trySend(CompassHeading(0.0, CompassAccuracy.UNRELIABLE))
            close(e)
        }

        awaitClose {
            try {
                manager?.stopUpdatingHeading()
                manager?.delegate = null
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    private fun mapAccuracy(accuracy: Double): CompassAccuracy {
        return when {
            accuracy < 0 -> CompassAccuracy.UNRELIABLE
            accuracy <= 15 -> CompassAccuracy.HIGH
            accuracy <= 25 -> CompassAccuracy.MEDIUM
            else -> CompassAccuracy.LOW
        }
    }
}
