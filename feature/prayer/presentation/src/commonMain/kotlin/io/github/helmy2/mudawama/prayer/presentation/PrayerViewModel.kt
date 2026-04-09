package io.github.helmy2.mudawama.prayer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.location.Coordinates
import io.github.helmy2.mudawama.core.location.LocationProvider
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import io.github.helmy2.mudawama.prayer.domain.usecase.ObservePrayersForDateUseCase
import io.github.helmy2.mudawama.prayer.domain.usecase.TogglePrayerStatusUseCase
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiEvent
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class PrayerViewModel(
    private val observePrayersForDateUseCase: ObservePrayersForDateUseCase,
    private val togglePrayerStatusUseCase: TogglePrayerStatusUseCase,
    private val locationProvider: LocationProvider,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _state = MutableStateFlow(
        PrayerUiState(
            selectedDate = timeProvider.logicalDate(),
            dateStrip = generateDateStrip(timeProvider.logicalDate())
        )
    )
    val state: StateFlow<PrayerUiState> = _state.asStateFlow()

    private var currentCoordinates: Coordinates? = null

    init {
        viewModelScope.launch {
            // Resolve location once on init
            resolveLocation()
            
            // Observe prayers
            _state.collectLatest { state ->
                observePrayersForDateUseCase(state.selectedDate, currentCoordinates ?: Coordinates(21.3891, 39.8579))
                    .collect { result ->
                        if (result is Result.Success) {
                            _state.value = _state.value.copy(prayers = result.data, timesAvailable = true)
                        } else {
                            _state.value = _state.value.copy(timesAvailable = false)
                        }
                    }
            }
        }
    }

    private fun resolveLocation() {
        if (locationProvider.hasPermission()) {
            viewModelScope.launch {
                val location = locationProvider.getCurrentLocation()
                if (location is Result.Success) {
                    currentCoordinates = location.data
                } else {
                    _state.value = _state.value.copy(usingFallbackLocation = true)
                }
            }
        } else {
            _state.value = _state.value.copy(usingFallbackLocation = true)
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
            else -> {} // Implement others later
        }
    }

    private fun generateDateStrip(today: LocalDate): List<LocalDate> {
        return (-3..3).map { today.plus(DatePeriod(days = it)) }
    }
}
