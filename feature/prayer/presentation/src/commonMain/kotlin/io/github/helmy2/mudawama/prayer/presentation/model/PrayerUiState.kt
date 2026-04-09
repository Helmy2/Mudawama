package io.github.helmy2.mudawama.prayer.presentation.model

import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import kotlinx.datetime.LocalDate

data class PrayerUiState(
    val selectedDate: LocalDate,
    val dateStrip: List<LocalDate>, // 7 dates: 3 past, today, 3 future
    val prayers: List<PrayerWithStatus>, // 5 items sorted chronologically
    val isLoading: Boolean = false,
    val timesAvailable: Boolean = false, // false = location/network error, display placeholder times
    val usingFallbackLocation: Boolean = false, // true = permission denied, using Mecca
    val missedSheetPrayer: PrayerWithStatus? = null // non-null means show action sheet
)

sealed interface PrayerUiAction {
    data class SelectDate(val date: LocalDate) : PrayerUiAction
    data class TogglePrayer(val prayerHabitId: String) : PrayerUiAction
    data class MarkMissedRequested(val prayer: PrayerWithStatus) : PrayerUiAction
    data class ConfirmMarkMissed(val prayerHabitId: String) : PrayerUiAction
    data object DismissMissedSheet : PrayerUiAction
}

import org.jetbrains.compose.resources.StringResource

sealed interface PrayerUiEvent {
    data class ShowError(val message: StringResource) : PrayerUiEvent
}
