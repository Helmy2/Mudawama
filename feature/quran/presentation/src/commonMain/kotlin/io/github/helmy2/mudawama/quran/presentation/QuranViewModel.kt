package io.github.helmy2.mudawama.quran.presentation

import androidx.lifecycle.viewModelScope
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.presentation.mvi.MviViewModel
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.quran.domain.usecase.AdvanceBookmarkUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.ComputeStreakUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.LogReadingUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.ObserveQuranStateUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.SetGoalUseCase
import io.github.helmy2.mudawama.quran.domain.usecase.UpdateBookmarkUseCase
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.presentation.model.QuranUiAction
import io.github.helmy2.mudawama.quran.presentation.model.QuranUiEvent
import io.github.helmy2.mudawama.quran.presentation.model.QuranUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.quran_error_generic
import mudawama.shared.designsystem.quran_error_invalid_ayah
import mudawama.shared.designsystem.quran_error_invalid_goal
import mudawama.shared.designsystem.quran_error_invalid_pages
import mudawama.shared.designsystem.quran_error_invalid_surah

class QuranViewModel(
    private val observeQuranStateUseCase: ObserveQuranStateUseCase,
    private val logReadingUseCase: LogReadingUseCase,
    private val setGoalUseCase: SetGoalUseCase,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val advanceBookmarkUseCase: AdvanceBookmarkUseCase,
    private val computeStreakUseCase: ComputeStreakUseCase,
    timeProvider: TimeProvider,
) : MviViewModel<QuranUiState, QuranUiAction, QuranUiEvent>(
    initialState = timeProvider.logicalDate().let { today ->
        QuranUiState(
            selectedDate = today,
            today = today,
            dateStrip = (-6..0).map { offset -> today.plus(DatePeriod(days = offset)) },
        )
    }
) {
    init {
        intent {
            // Compute initial streak
            val streak = computeStreakUseCase()
            reduce { copy(streak = streak, isLoading = false) }

            // Date-reactive observation — restarts whenever selectedDate changes
            state.collectLatest { currentState ->
                observeQuranStateUseCase(currentState.selectedDate).collect { screenState ->
                    reduce {
                        copy(
                            pagesReadToday = screenState.pagesReadToday,
                            goalPages = screenState.goalPages,
                            bookmark = screenState.bookmark,
                            recentLogs = screenState.recentLogs,
                        )
                    }
                }
            }
        }
    }

    override fun onAction(action: QuranUiAction) {
        when (action) {
            is QuranUiAction.SelectDate -> {
                reduce { copy(selectedDate = action.date) }
            }

            // Log Reading sheet
            QuranUiAction.OpenLogReadingSheet -> {
                reduce { copy(logReadingSheetVisible = true, logReadingPageInput = 0) }
            }
            QuranUiAction.DismissLogReadingSheet -> {
                reduce { copy(logReadingSheetVisible = false) }
            }
            is QuranUiAction.UpdateLogPageInput -> {
                reduce { copy(logReadingPageInput = action.pages) }
            }
            is QuranUiAction.ConfirmLogReading -> {
                exclusiveIntent("log") {
                    val result = logReadingUseCase(action.pages, state.value.selectedDate)
                    when (result) {
                        is Result.Failure -> {
                            val msgRes = when (result.error) {
                                is QuranError.InvalidPageCount -> Res.string.quran_error_invalid_pages
                                else -> Res.string.quran_error_generic
                            }
                            emitEvent(QuranUiEvent.ShowSnackbar(msgRes))
                        }
                        is Result.Success -> {
                            // Automatically advance the bookmark by the pages just read.
                            // Uses SurahMetadata.startPage to resolve the new surah position.
                            // Errors here are silently ignored — the log itself succeeded.
                            advanceBookmarkUseCase(
                                currentBookmark = state.value.bookmark,
                                pagesRead = action.pages,
                            )
                        }
                    }
                    reduce { copy(logReadingSheetVisible = false, logReadingPageInput = 0) }
                    recomputeStreak()
                }
            }

            // Set Goal sheet
            QuranUiAction.OpenSetGoalSheet -> {
                reduce { copy(setGoalSheetVisible = true) }
            }
            QuranUiAction.DismissSetGoalSheet -> {
                reduce { copy(setGoalSheetVisible = false) }
            }
            is QuranUiAction.ConfirmSetGoal -> {
                intent {
                    val result = setGoalUseCase(action.pages)
                    when (result) {
                        is Result.Success -> reduce { copy(setGoalSheetVisible = false) }
                        is Result.Failure ->
                            emitEvent(QuranUiEvent.ShowSnackbar(Res.string.quran_error_invalid_goal))
                    }
                }
            }

            // Update Position sheet
            QuranUiAction.OpenUpdatePositionSheet -> {
                reduce { copy(updatePositionSheetVisible = true) }
            }
            QuranUiAction.DismissUpdatePositionSheet -> {
                reduce { copy(updatePositionSheetVisible = false) }
            }
            is QuranUiAction.ConfirmUpdatePosition -> {
                intent {
                    val result = updateBookmarkUseCase(action.surah, action.ayah)
                    when (result) {
                        is Result.Success ->
                            reduce { copy(updatePositionSheetVisible = false) }
                        is Result.Failure -> {
                            val msgRes = when (result.error) {
                                is QuranError.InvalidSurah -> Res.string.quran_error_invalid_surah
                                is QuranError.InvalidAyah -> Res.string.quran_error_invalid_ayah
                                else -> Res.string.quran_error_generic
                            }
                            emitEvent(QuranUiEvent.ShowSnackbar(msgRes))
                        }
                    }
                }
            }
        }
    }

    private fun recomputeStreak() {
        viewModelScope.launch {
            val streak = computeStreakUseCase()
            reduce { copy(streak = streak) }
        }
    }
}
