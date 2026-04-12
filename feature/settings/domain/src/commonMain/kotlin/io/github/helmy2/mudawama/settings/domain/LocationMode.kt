package io.github.helmy2.mudawama.settings.domain

sealed class LocationMode {
    data object Gps : LocationMode()
    data class Manual(
        val latitude: Double,
        val longitude: Double
    ) : LocationMode()
}