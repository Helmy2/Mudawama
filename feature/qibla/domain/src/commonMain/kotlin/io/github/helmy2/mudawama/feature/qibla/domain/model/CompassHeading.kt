package io.github.helmy2.mudawama.feature.qibla.domain.model

data class CompassHeading(
    val heading: Double,
    val accuracy: CompassAccuracy
)

enum class CompassAccuracy {
    HIGH,
    MEDIUM,
    LOW,
    UNRELIABLE
}