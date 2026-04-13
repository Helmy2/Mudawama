package io.github.helmy2.mudawama.settings.presentation

import androidx.lifecycle.viewModelScope
import io.github.helmy2.mudawama.core.domain.notification.NotificationScheduler
import io.github.helmy2.mudawama.core.presentation.mvi.MviViewModel
import io.github.helmy2.mudawama.quran.domain.usecase.SetGoalUseCase
import io.github.helmy2.mudawama.settings.domain.LocationMode
import io.github.helmy2.mudawama.settings.domain.ObserveSettingsUseCase
import io.github.helmy2.mudawama.settings.domain.SetAppLanguageUseCase
import io.github.helmy2.mudawama.settings.domain.SetAppThemeUseCase
import io.github.helmy2.mudawama.settings.domain.SetCalculationMethodUseCase
import io.github.helmy2.mudawama.settings.domain.SetEveningNotificationUseCase
import io.github.helmy2.mudawama.settings.domain.SetLocationModeUseCase
import io.github.helmy2.mudawama.settings.domain.SetMorningNotificationUseCase
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
) : MviViewModel<SettingsState, SettingsAction, SettingsEvent>(SettingsState()) {

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            observeSettingsUseCase().collect { settings ->
                reduce {
                    val locationMode = settings.locationMode
                    if (locationMode is LocationMode.Manual) {
                        copy(
                            settings = settings,
                            isLoading = false,
                            latitudeInput = locationMode.latitude.toString(),
                            longitudeInput = locationMode.longitude.toString(),
                            morningNotificationEnabled = settings.morningNotificationEnabled,
                            eveningNotificationEnabled = settings.eveningNotificationEnabled
                        )
                    } else {
                        copy(
                            settings = settings,
                            isLoading = false,
                            morningNotificationEnabled = settings.morningNotificationEnabled,
                            eveningNotificationEnabled = settings.eveningNotificationEnabled
                        )
                    }
                }
            }
        }
    }

    override fun onAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.SetCalculationMethod -> {
                    setCalculationMethodUseCase(action.method)
                }
                is SettingsAction.SetLocationMode -> {
                    setLocationModeUseCase(action.mode)
                }
                is SettingsAction.UpdateLatitudeInput -> {
                    reduce { copy(latitudeInput = action.value, latitudeError = null) }
                }
                is SettingsAction.UpdateLongitudeInput -> {
                    reduce { copy(longitudeInput = action.value, longitudeError = null) }
                }
                is SettingsAction.SaveManualLocation -> {
                    saveManualLocation()
                }
                is SettingsAction.SetAppTheme -> {
                    setAppThemeUseCase(action.theme)
                }
                is SettingsAction.SetAppLanguage -> {
                    setAppLanguageUseCase(action.language)
                    applySystemLocale(action.language.code)
                }
                is SettingsAction.UpdateGoalInput -> {
                    reduce { copy(goalInput = action.value, goalError = null) }
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
        val settings = state.value.settings
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
        val latStr = state.value.latitudeInput
        val lonStr = state.value.longitudeInput

        val lat = latStr.toDoubleOrNull()
        val lon = lonStr.toDoubleOrNull()

        var hasError = false

        if (lat == null || lat < -90 || lat > 90) {
            reduce { copy(latitudeError = "Invalid latitude") }
            hasError = true
        }
        if (lon == null || lon < -180 || lon > 180) {
            reduce { copy(longitudeError = "Invalid longitude") }
            hasError = true
        }

        if (!hasError && lat != null && lon != null) {
            viewModelScope.launch {
                setLocationModeUseCase(LocationMode.Manual(lat, lon))
            }
        }
    }

    private fun saveGoal() {
        val goalStr = state.value.goalInput
        val goal = goalStr.toIntOrNull()

        if (goal == null || goal < 1) {
            reduce { copy(goalError = "Minimum goal is 1") }
            return
        }

        viewModelScope.launch {
            setGoalUseCase(goal)
            emitEvent(SettingsEvent.GoalSaved)
        }
    }
}