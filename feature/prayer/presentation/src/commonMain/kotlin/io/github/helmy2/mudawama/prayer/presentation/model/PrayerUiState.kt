package io.github.helmy2.mudawama.prayer.presentation.model

import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

data class PrayerUiState(
    val selectedDate: LocalDate,
    val dateStrip: List<LocalDate>, // 7 dates: 3 past, today, 3 future
    val prayers: List<PrayerWithStatus> = emptyList(), // 5 items sorted chronologically
    val today: LocalDate = selectedDate, // kept for read-only derivation
    val isLoading: Boolean = false,
    val timesAvailable: Boolean = false, // false = location/network error, display placeholder times
    val usingFallbackLocation: Boolean = false, // true = permission denied, using Mecca
    val locationServiceDisabled: Boolean = false, // true = permission granted but GPS/network is off
    val missedSheetPrayer: PrayerWithStatus? = null // non-null means show action sheet
) {
    val isReadOnly: Boolean get() = selectedDate != today
}

sealed interface PrayerUiAction {
    data class SelectDate(val date: LocalDate) : PrayerUiAction
    data class TogglePrayer(val prayerHabitId: String) : PrayerUiAction
    data class MarkMissedRequested(val prayer: PrayerWithStatus) : PrayerUiAction
    data class ConfirmMarkMissed(val prayerHabitId: String) : PrayerUiAction
    data class ConfirmMarkPending(val prayerHabitId: String) : PrayerUiAction
    data object DismissMissedSheet : PrayerUiAction
    /** Fired by the UI after the user grants location permission at runtime. */
    data object LocationPermissionGranted : PrayerUiAction
    /** Fired when the user taps the "location service disabled" banner to open Settings. */
    data object OpenLocationSettings : PrayerUiAction
}

sealed interface PrayerUiEvent {
    data class ShowError(val message: StringResource) : PrayerUiEvent
}
