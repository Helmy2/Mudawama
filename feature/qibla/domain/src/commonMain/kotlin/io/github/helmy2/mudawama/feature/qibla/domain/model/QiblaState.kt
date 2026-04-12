package io.github.helmy2.mudawama.feature.qibla.domain.model

data class QiblaState(
    val currentHeading: Double = 0.0,
    val qiblaAngle: Double? = null,
    val accuracy: CompassAccuracy = CompassAccuracy.UNRELIABLE,
    val isAligned: Boolean = false,
    val hasLocation: Boolean = false,
    val isLoading: Boolean = true,
    val error: QiblaError? = null
)

sealed class QiblaError {
    object NoLocation : QiblaError()
    object SensorUnavailable : QiblaError()
    object LocationError : QiblaError()
}