package io.github.helmy2.mudawama.prayer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.location.Coordinates
import io.github.helmy2.mudawama.core.location.LocationError
import io.github.helmy2.mudawama.core.location.LocationProvider
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.prayer.domain.usecase.MarkPrayerMissedUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.MarkPrayerPendingUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.ObservePrayersForDateUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.SeedPrayerHabitsUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.TogglePrayerStatusUseCase
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class PrayerViewModel(
    private val observePrayersForDateUseCase: ObservePrayersForDateUseCase,
    private val togglePrayerStatusUseCase: TogglePrayerStatusUseCase,
    private val markPrayerMissedUseCase: MarkPrayerMissedUseCase,
    private val markPrayerPendingUseCase: MarkPrayerPendingUseCase,
    private val seedPrayerHabitsUseCase: SeedPrayerHabitsUseCase,
    private val locationProvider: LocationProvider,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(
        PrayerUiState(
            selectedDate = timeProvider.logicalDate(),
            today = timeProvider.logicalDate(),
            dateStrip = generateDateStrip(timeProvider.logicalDate())
        )
    )
    val state: StateFlow<PrayerUiState> = _state.asStateFlow()

    private var currentCoordinates: Coordinates? = null

    init {
        viewModelScope.launch {
            // 1. Seed DB rows (idempotent — no-op if already present)
            seedPrayerHabitsUseCase()
            // 2. Resolve location before starting the observe loop so coordinates are ready
            resolveLocation()
            // 3. Now start the observation loop — no race with location or empty DB
            _state.collectLatest { currentState ->
                observePrayersForDateUseCase(
                    currentState.selectedDate,
                    currentCoordinates ?: MECCA_COORDINATES
                ).collect { result ->
                    when (result) {
                        is Result.Success -> _state.value =
                            _state.value.copy(prayers = result.data, timesAvailable = true)
                        is Result.Failure -> _state.value =
                            _state.value.copy(timesAvailable = false)
                    }
                }
            }
        }
    }

    private suspend fun resolveLocation() {
        val location = locationProvider.getCurrentLocation()
        when (location) {
            is Result.Success -> {
                currentCoordinates = location.data
                _state.value = _state.value.copy(
                    locationServiceDisabled = false,
                    usingFallbackLocation = false
                )
            }
            is Result.Failure -> when (location.error) {
                is LocationError.LocationUnavailable ->
                    _state.value = _state.value.copy(
                        locationServiceDisabled = true,
                        usingFallbackLocation = false
                    )
                else ->
                    _state.value = _state.value.copy(
                        usingFallbackLocation = true,
                        locationServiceDisabled = false
                    )
            }
        }
    }

    fun onAction(action: PrayerUiAction) {
        when (action) {
            is PrayerUiAction.SelectDate -> {
                _state.value = _state.value.copy(selectedDate = action.date)
            }
            is PrayerUiAction.TogglePrayer -> {
                viewModelScope.launch {
                    togglePrayerStatusUseCase(action.prayerHabitId, _state.value.selectedDate)
                }
            }
            is PrayerUiAction.MarkMissedRequested -> {
                _state.value = _state.value.copy(missedSheetPrayer = action.prayer)
            }
            is PrayerUiAction.ConfirmMarkMissed -> {
                val date = _state.value.selectedDate
                _state.value = _state.value.copy(missedSheetPrayer = null)
                viewModelScope.launch {
                    markPrayerMissedUseCase(action.prayerHabitId, date)
                }
            }
            is PrayerUiAction.ConfirmMarkPending -> {
                val date = _state.value.selectedDate
                _state.value = _state.value.copy(missedSheetPrayer = null)
                viewModelScope.launch {
                    markPrayerPendingUseCase(action.prayerHabitId, date)
                }
            }
            is PrayerUiAction.DismissMissedSheet -> {
                _state.value = _state.value.copy(missedSheetPrayer = null)
            }
            is PrayerUiAction.LocationPermissionGranted -> {
                viewModelScope.launch {
                    // Re-resolve now that permission has been granted; clears banners
                    _state.value = _state.value.copy(
                        usingFallbackLocation = false,
                        locationServiceDisabled = false
                    )
                    resolveLocation()
                }
            }
            is PrayerUiAction.OpenLocationSettings -> {
                // Handled entirely in the UI layer (expect/actual); ViewModel is a no-op here.
                // When the user returns from Settings, LocationPermissionGranted will re-resolve.
            }
        }
    }

    private fun generateDateStrip(today: LocalDate): List<LocalDate> =
        (-3..3).map { today.plus(DatePeriod(days = it)) }

    companion object {
        // Fallback: Mecca, Saudi Arabia
        private val MECCA_COORDINATES = Coordinates(21.3891, 39.8579)
    }
}
