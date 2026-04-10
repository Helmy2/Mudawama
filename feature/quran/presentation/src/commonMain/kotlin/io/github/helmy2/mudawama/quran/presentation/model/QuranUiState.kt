package io.github.helmy2.mudawama.quran.presentation.model

import io.github.helmy2.mudawama.quran.domain.model.QuranBookmark
import io.github.helmy2.mudawama.quran.domain.model.QuranScreenState
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource

data class QuranUiState(
    val selectedDate: LocalDate,
    val dateStrip: List<LocalDate>,       // 7 entries: [today-6 … today]; today is rightmost
    val today: LocalDate,
    val pagesReadToday: Int = 0,
    val goalPages: Int = 5,
    val bookmark: QuranBookmark? = null,
    val recentLogs: List<QuranScreenState.RecentLogEntry> = emptyList(),
    val streak: Int = 0,
    val isLoading: Boolean = true,
    // Sheet visibility
    val logReadingSheetVisible: Boolean = false,
    val setGoalSheetVisible: Boolean = false,
    val updatePositionSheetVisible: Boolean = false,
    // Log Reading sheet input
    val logReadingPageInput: Int = 0,
) {
    val isReadOnly: Boolean get() = selectedDate != today
    val progressFraction: Float get() =
        if (goalPages > 0) (pagesReadToday / goalPages.toFloat()).coerceIn(0f, 1f) else 0f
}

sealed interface QuranUiAction {
    data class SelectDate(val date: LocalDate) : QuranUiAction
    // Log Reading sheet
    data object OpenLogReadingSheet : QuranUiAction
    data object DismissLogReadingSheet : QuranUiAction
    data class UpdateLogPageInput(val pages: Int) : QuranUiAction
    data class ConfirmLogReading(val pages: Int) : QuranUiAction
    // Set Goal sheet
    data object OpenSetGoalSheet : QuranUiAction
    data object DismissSetGoalSheet : QuranUiAction
    data class ConfirmSetGoal(val pages: Int) : QuranUiAction
    // Update Position sheet (from main screen only — bookmark advances automatically on log)
    data object OpenUpdatePositionSheet : QuranUiAction
    data object DismissUpdatePositionSheet : QuranUiAction
    data class ConfirmUpdatePosition(val surah: Int, val ayah: Int) : QuranUiAction
}

sealed interface QuranUiEvent {
    data class ShowSnackbar(val message: StringResource) : QuranUiEvent
}
