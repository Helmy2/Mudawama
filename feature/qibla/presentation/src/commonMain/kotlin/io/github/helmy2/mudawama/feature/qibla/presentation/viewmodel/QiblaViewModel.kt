package io.github.helmy2.mudawama.feature.qibla.presentation.viewmodel

import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.location.Coordinates
import io.github.helmy2.mudawama.core.location.LocationProvider
import io.github.helmy2.mudawama.core.presentation.mvi.MviViewModel
import io.github.helmy2.mudawama.feature.qibla.data.sensor.CompassSensorManager
import io.github.helmy2.mudawama.feature.qibla.domain.model.QiblaAction
import io.github.helmy2.mudawama.feature.qibla.domain.model.QiblaError
import io.github.helmy2.mudawama.feature.qibla.domain.model.QiblaEvent
import io.github.helmy2.mudawama.feature.qibla.domain.model.QiblaState
import io.github.helmy2.mudawama.feature.qibla.domain.usecase.CalculateQiblaAngleUseCase
import io.github.helmy2.mudawama.settings.domain.LocationMode
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class QiblaViewModel(
    private val calculateQiblaAngleUseCase: CalculateQiblaAngleUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val locationProvider: LocationProvider,
    private val compassSensorManager: CompassSensorManager,
) : MviViewModel<QiblaState, QiblaAction, QiblaEvent>(QiblaState()) {

    private var previousIsAligned: Boolean = false

    override fun onAction(action: QiblaAction) {
        when (action) {
            is QiblaAction.StartCompass -> startCompass()
            is QiblaAction.StopCompass -> stopCompass()
            is QiblaAction.NavigateToSettings -> intent { emitEvent(QiblaEvent.NavigateToSettings) }
            is QiblaAction.RequestLocationPermission -> requestLocationPermission()
        }
    }

    private fun startCompass() {
        intent {
            reduce { copy(isLoading = true) }

            val coordinates = try {
                withTimeoutOrNull(3000) { getUserCoordinatesFast() }
            } catch (e: Exception) {
                null
            }

            val userCoords = coordinates ?: Coordinates(51.5074, -0.1278)

            val qiblaAngle = calculateQiblaAngleUseCase(userCoords)
            reduce { copy(qiblaAngle = qiblaAngle, hasLocation = true, isLoading = false, error = null) }

            compassSensorManager.observeHeading()
                .catch { _ ->
                    reduce { copy(error = QiblaError.SensorUnavailable) }
                }
                .collectLatest { heading ->
                    val qAngle = state.value.qiblaAngle
                    if (qAngle != null) {
                        val currentH = if (heading.heading > 0) heading.heading else state.value.currentHeading
                        val isAligned = isWithinThreshold(currentH, qAngle)

                        if (isAligned && !previousIsAligned) {
                            // Haptic triggered by UI
                        }
                        previousIsAligned = isAligned

                        reduce {
                            copy(
                                currentHeading = currentH,
                                accuracy = heading.accuracy,
                                isAligned = isAligned
                            )
                        }
                    } else {
                        reduce {
                            copy(
                                currentHeading = heading.heading,
                                accuracy = heading.accuracy
                            )
                        }
                    }
                }
        }
    }

    private fun stopCompass() {
        // Flow collection cancelled automatically
    }

    private suspend fun getUserCoordinatesFast(): Coordinates? {
        return try {
            val settings = observeSettingsUseCase().first()
            when (val locationMode = settings.locationMode) {
                is LocationMode.Manual -> {
                    Coordinates(locationMode.latitude, locationMode.longitude)
                }
                is LocationMode.Gps -> {
                    when (val result = locationProvider.getCurrentLocation()) {
                        is Result.Success -> result.data
                        is Result.Failure -> null
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isWithinThreshold(heading: Double, qiblaAngle: Double): Boolean {
        if (heading <= 0) return false
        val diff = kotlin.math.abs(heading - qiblaAngle)
        val normalizedDiff = if (diff > 180) 360 - diff else diff
        return normalizedDiff <= 2.0
    }

    private fun requestLocationPermission() {
        intent { emitEvent(QiblaEvent.NavigateToSettings) }
    }
}