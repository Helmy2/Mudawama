package io.github.helmy2.mudawama.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.helmy2.mudawama.core.domain.notification.NotificationScheduler
import io.github.helmy2.mudawama.quran.domain.usecase.SetGoalUseCase
import io.github.helmy2.mudawama.settings.domain.LocationMode
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import io.github.helmy2.mudawama.settings.domain.SetAppLanguageUseCase
import io.github.helmy2.mudawama.settings.domain.SetAppThemeUseCase
import io.github.helmy2.mudawama.settings.domain.SetCalculationMethodUseCase
import io.github.helmy2.mudawama.settings.domain.SetEveningNotificationUseCase
import io.github.helmy2.mudawama.settings.domain.SetLocationModeUseCase
import io.github.helmy2.mudawama.settings.domain.SetMorningNotificationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val setCalculationMethodUseCase: SetCalculationMethodUseCase,
    private val setLocationModeUseCase: SetLocationModeUseCase,
    private val setAppThemeUseCase: SetAppThemeUseCase,
    private val setAppLanguageUseCase: SetAppLanguageUseCase,
    private val setGoalUseCase: SetGoalUseCase,
    private val setMorningNotificationUseCase: SetMorningNotificationUseCase,
    private val setEveningNotificationUseCase: SetEveningNotificationUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            observeSettingsUseCase().collect { settings ->
                val currentState = _state.value
                val locationMode = settings.locationMode
                val updatedState = if (locationMode is LocationMode.Manual) {
                    currentState.copy(
                        settings = settings,
                        isLoading = false,
                        latitudeInput = locationMode.latitude.toString(),
                        longitudeInput = locationMode.longitude.toString(),
                        morningNotificationEnabled = settings.morningNotificationEnabled,
                        eveningNotificationEnabled = settings.eveningNotificationEnabled
                    )
                } else {
                    currentState.copy(
                        settings = settings,
                        isLoading = false,
                        morningNotificationEnabled = settings.morningNotificationEnabled,
                        eveningNotificationEnabled = settings.eveningNotificationEnabled
                    )
                }
                _state.value = updatedState
            }
        }
    }

    fun onAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.SetCalculationMethod -> {
                    setCalculationMethodUseCase(action.method)
                }
                is SettingsAction.SetLocationMode -> {
                    setLocationModeUseCase(action.mode)
                }
                is SettingsAction.UpdateLatitudeInput -> {
                    _state.update { it.copy(latitudeInput = action.value, latitudeError = null) }
                }
                is SettingsAction.UpdateLongitudeInput -> {
                    _state.update { it.copy(longitudeInput = action.value, longitudeError = null) }
                }
                is SettingsAction.SaveManualLocation -> {
                    saveManualLocation()
                }
                is SettingsAction.SetAppTheme -> {
                    setAppThemeUseCase(action.theme)
                }
                is SettingsAction.SetAppLanguage -> {
                    viewModelScope.launch(Dispatchers.Main) {
                        setAppLanguageUseCase(action.language)
                        applySystemLocale(action.language.code)
                    }
                }
                is SettingsAction.UpdateGoalInput -> {
                    _state.update { it.copy(goalInput = action.value, goalError = null) }
                }
                is SettingsAction.SaveGoal -> {
                    saveGoal()
                }
                is SettingsAction.SetMorningNotification -> {
                    setMorningNotificationUseCase(action.enabled)
                    updateNotificationSchedule(true, action.enabled)
                }
                is SettingsAction.SetEveningNotification -> {
                    setEveningNotificationUseCase(action.enabled)
                    updateNotificationSchedule(false, action.enabled)
                }
            }
        }
    }

    private suspend fun updateNotificationSchedule(isMorning: Boolean, enabled: Boolean) {
        val settings = _state.value.settings
        if (enabled) {
            val hour = if (isMorning) settings.morningNotificationHour else settings.eveningNotificationHour
            val minute = if (isMorning) settings.morningNotificationMinute else settings.eveningNotificationMinute
            val id = if (isMorning) 1001 else 1002
            val title = if (isMorning) "Morning Athkar" else "Evening Athkar"
            val body = if (isMorning) "Time for morning dhikr" else "Time for evening dhikr"
            notificationScheduler.scheduleDailyReminder(id, hour, minute, title, body)
        } else {
            val id = if (isMorning) 1001 else 1002
            notificationScheduler.cancelReminder(id)
        }
    }

    private fun saveManualLocation() {
        val latStr = _state.value.latitudeInput
        val lonStr = _state.value.longitudeInput

        val lat = latStr.toDoubleOrNull()
        val lon = lonStr.toDoubleOrNull()

        var hasError = false

        if (lat == null || lat < -90 || lat > 90) {
            _state.update { it.copy(latitudeError = "Invalid latitude") }
            hasError = true
        }
        if (lon == null || lon < -180 || lon > 180) {
            _state.update { it.copy(longitudeError = "Invalid longitude") }
            hasError = true
        }

        if (!hasError && lat != null && lon != null) {
            viewModelScope.launch {
                setLocationModeUseCase(LocationMode.Manual(lat, lon))
            }
        }
    }

    private fun saveGoal() {
        val goalStr = _state.value.goalInput
        val goal = goalStr.toIntOrNull()

        if (goal == null || goal < 1) {
            _state.update { it.copy(goalError = "Minimum goal is 1") }
            return
        }

        viewModelScope.launch {
            setGoalUseCase(goal)
            _events.emit(SettingsEvent.GoalSaved)
        }
    }
}