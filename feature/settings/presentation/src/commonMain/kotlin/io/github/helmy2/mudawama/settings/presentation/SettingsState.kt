package io.github.helmy2.mudawama.settings.presentation

import io.github.helmy2.mudawama.settings.domain.AppLanguage
import io.github.helmy2.mudawama.settings.domain.AppSettings
import io.github.helmy2.mudawama.settings.domain.AppTheme
import io.github.helmy2.mudawama.settings.domain.CalculationMethod
import io.github.helmy2.mudawama.settings.domain.LocationMode

data class SettingsState(
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val latitudeInput: String = "",
    val longitudeInput: String = "",
    val latitudeError: String? = null,
    val longitudeError: String? = null,
    val goalInput: String = "",
    val goalError: String? = null,
    val morningNotificationEnabled: Boolean = false,
    val eveningNotificationEnabled: Boolean = false
)

sealed interface SettingsAction {
    data class SetCalculationMethod(val method: CalculationMethod) : SettingsAction
    data class SetLocationMode(val mode: LocationMode) : SettingsAction
    data class UpdateLatitudeInput(val value: String) : SettingsAction
    data class UpdateLongitudeInput(val value: String) : SettingsAction
    data object SaveManualLocation : SettingsAction
    data class SetAppTheme(val theme: AppTheme) : SettingsAction
    data class SetAppLanguage(val language: AppLanguage) : SettingsAction
    data class UpdateGoalInput(val value: String) : SettingsAction
    data object SaveGoal : SettingsAction
    data class SetMorningNotification(val enabled: Boolean) : SettingsAction
    data class SetEveningNotification(val enabled: Boolean) : SettingsAction
}

sealed interface SettingsEvent {
    data class ShowError(val message: String) : SettingsEvent
    data object GoalSaved : SettingsEvent
}