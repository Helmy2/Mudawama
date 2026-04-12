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
import io.github.helmy2.mudawama.settings.domain.CalculationMethod
import io.github.helmy2.mudawama.settings.domain.LocationMode
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
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
    private val observeSettingsUseCase: ObserveSettingsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        PrayerUiState(
            selectedDate = timeProvider.logicalDate(),
            today = timeProvider.logicalDate(),
            dateStrip = generateDateStrip(timeProvider.logicalDate()))
    )
    val state: StateFlow<PrayerUiState> = _state.asStateFlow()

    private var currentCoordinates: Coordinates? = null
    private var currentLocationMode: LocationMode = LocationMode.Gps
    private var currentCalculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE

    init {
        viewModelScope.launch {
            seedPrayerHabitsUseCase()
            observeSettingsUseCase().collectLatest { settings ->
                val locationChanged = currentLocationMode != settings.locationMode
                val methodChanged = currentCalculationMethod != settings.calculationMethod

                currentLocationMode = settings.locationMode
                currentCalculationMethod = settings.calculationMethod

                resolveLocation()

                if (locationChanged || methodChanged) {
                    loadPrayersForDate(_state.value.selectedDate)
                }
            }
        }
        observeDateChanges()
    }

    private fun observeDateChanges() {
        viewModelScope.launch {
            _state.collectLatest { currentState ->
                loadPrayersForDate(currentState.selectedDate)
            }
        }
    }

    private suspend fun loadPrayersForDate(date: LocalDate) {
        val coords = currentCoordinates ?: MECCA_COORDINATES
        val method = currentCalculationMethod
        observePrayersForDateUseCase(date, coords, method).collect { result ->
            when (result) {
                is Result.Success -> _state.value =
                    _state.value.copy(prayers = result.data, timesAvailable = true)
                is Result.Failure -> _state.value =
                    _state.value.copy(timesAvailable = false)
            }
        }
    }

    private suspend fun resolveLocation() {
        when (val mode = currentLocationMode) {
            is LocationMode.Gps -> {
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
                        LocationError.PermissionDenied -> {
                            _state.value = _state.value.copy(
                                locationServiceDisabled = true,
                                usingFallbackLocation = true
                            )
                            currentCoordinates = MECCA_COORDINATES
                        }
                        else -> {
                            currentCoordinates = MECCA_COORDINATES
                            _state.value = _state.value.copy(usingFallbackLocation = true)
                        }
                    }
                }
            }
            is LocationMode.Manual -> {
                currentCoordinates = Coordinates(mode.latitude, mode.longitude)
                _state.value = _state.value.copy(
                    locationServiceDisabled = false,
                    usingFallbackLocation = false
                )
            }
        }
    }

    fun onAction(action: PrayerUiAction) {
        viewModelScope.launch {
            when (action) {
                is PrayerUiAction.SelectDate -> {
                    _state.value = _state.value.copy(selectedDate = action.date)
                }
                is PrayerUiAction.TogglePrayer -> {
                    togglePrayerStatusUseCase(action.prayerHabitId, _state.value.selectedDate)
                }
                is PrayerUiAction.ConfirmMarkMissed -> {
                    markPrayerMissedUseCase(action.prayerHabitId, _state.value.selectedDate)
                }
                is PrayerUiAction.ConfirmMarkPending -> {
                    markPrayerPendingUseCase(action.prayerHabitId, _state.value.selectedDate)
                }
                is PrayerUiAction.MarkMissedRequested -> {
                    _state.value = _state.value.copy(missedSheetPrayer = action.prayer)
                }
                is PrayerUiAction.DismissMissedSheet -> {
                    _state.value = _state.value.copy(missedSheetPrayer = null)
                }
                is PrayerUiAction.LocationPermissionGranted -> {
                    _state.value = _state.value.copy(
                        usingFallbackLocation = false,
                        locationServiceDisabled = false
                    )
                    resolveLocation()
                }
                is PrayerUiAction.OpenLocationSettings -> {
                    // Handled in UI layer
                }
            }
        }
    }

    private fun generateDateStrip(today: LocalDate): List<LocalDate> =
        (-3..3).map { today.plus(DatePeriod(days = it)) }

    companion object {
        private val MECCA_COORDINATES = Coordinates(21.3891, 39.8579)
    }
}