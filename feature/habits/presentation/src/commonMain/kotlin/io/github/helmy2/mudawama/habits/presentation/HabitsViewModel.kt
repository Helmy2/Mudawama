package io.github.helmy2.mudawama.habits.presentation

import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.presentation.mvi.MviViewModel
import io.github.helmy2.mudawama.habits.domain.error.HabitError
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import io.github.helmy2.mudawama.habits.domain.usecase.CreateHabitUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.DeleteHabitUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.IncrementHabitCountUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.ObserveHabitsWithTodayStatusUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.ResetHabitTodayLogUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.ToggleHabitCompletionUseCase
import io.github.helmy2.mudawama.habits.domain.usecase.UpdateHabitUseCase
import io.github.helmy2.mudawama.habits.presentation.model.BottomSheetMode
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiAction
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiEvent
import io.github.helmy2.mudawama.habits.presentation.model.HabitsUiState
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.error_cannot_delete_core
import mudawama.shared.designsystem.error_generic
import mudawama.shared.designsystem.error_name_empty_snackbar
import mudawama.shared.designsystem.error_no_day_selected_snackbar

class HabitsViewModel(
    private val observeHabitsUseCase: ObserveHabitsWithTodayStatusUseCase,
    private val createHabitUseCase: CreateHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val toggleCompletionUseCase: ToggleHabitCompletionUseCase,
    private val incrementCountUseCase: IncrementHabitCountUseCase,
    private val resetTodayLogUseCase: ResetHabitTodayLogUseCase,
) : MviViewModel<HabitsUiState, HabitsUiAction, HabitsUiEvent>(HabitsUiState()) {

    init {
        // Single long-lived subscription — cancelled automatically via viewModelScope on onCleared()
        intent {
            observeHabitsUseCase().collect { habitsWithStatus ->
                reduce { copy(habits = habitsWithStatus, isLoading = false) }
            }
        }
    }

    override fun onAction(action: HabitsUiAction) {
        when (action) {
            HabitsUiAction.AddHabitFabClicked ->
                reduce { copy(bottomSheetMode = BottomSheetMode.AddHabit) }

            is HabitsUiAction.HabitLongPressed ->
                reduce { copy(bottomSheetMode = BottomSheetMode.OptionsMenu(action.habit)) }

            is HabitsUiAction.EditHabitSelected ->
                reduce { copy(bottomSheetMode = BottomSheetMode.EditHabit(action.habit)) }

            is HabitsUiAction.DeleteHabitSelected ->
                reduce { copy(bottomSheetMode = BottomSheetMode.DeleteConfirm(action.habitId)) }

            is HabitsUiAction.DeleteConfirmed -> handleDeleteHabit(action.habitId)

            is HabitsUiAction.SaveHabit -> handleSaveHabit(action)

            // exclusiveIntent debounces rapid taps on the same habit (Decision 7)
            is HabitsUiAction.ToggleCompletion ->
                exclusiveIntent("toggle_${action.habitId}") {
                    toggleCompletionUseCase(action.habitId)
                }

            is HabitsUiAction.IncrementCount ->
                exclusiveIntent("increment_${action.habitId}") {
                    incrementCountUseCase(action.habitId)
                }

            is HabitsUiAction.ResetTodayProgress ->
                exclusiveIntent("reset_${action.habitId}") {
                    when (resetTodayLogUseCase(action.habitId)) {
                        is Result.Success ->
                            reduce { copy(bottomSheetMode = BottomSheetMode.Hidden) }
                        is Result.Failure ->
                            emitEvent(HabitsUiEvent.ShowSnackbar(Res.string.error_generic))
                    }
                }

            HabitsUiAction.DismissBottomSheet ->
                reduce { copy(bottomSheetMode = BottomSheetMode.Hidden, errorMessage = null) }

            HabitsUiAction.DismissError ->
                reduce { copy(errorMessage = null) }
        }
    }

    private fun handleSaveHabit(action: HabitsUiAction.SaveHabit) {
        intent {
            val result = when (val mode = state.value.bottomSheetMode) {
                BottomSheetMode.AddHabit -> createHabitUseCase(
                    name = action.name,
                    iconKey = action.iconKey,
                    frequencyDays = action.frequencyDays,
                    type = action.type,
                    goalCount = action.goalCount,
                )
                is BottomSheetMode.EditHabit -> updateHabitUseCase(
                    mode.habit.copy(
                        name = action.name,
                        iconKey = action.iconKey,
                        frequencyDays = action.frequencyDays,
                        type = action.type,
                        goalCount = if (action.type == HabitType.NUMERIC) action.goalCount else null,
                    )
                )
                else -> return@intent
            }

            when (result) {
                is Result.Success ->
                    reduce { copy(bottomSheetMode = BottomSheetMode.Hidden, errorMessage = null) }

                is Result.Failure -> when (result.error) {
                    HabitError.EmptyHabitName ->
                        emitEvent(HabitsUiEvent.ShowSnackbar(Res.string.error_name_empty_snackbar))
                    HabitError.NoFrequencyDaySelected ->
                        emitEvent(HabitsUiEvent.ShowSnackbar(Res.string.error_no_day_selected_snackbar))
                    else ->
                        emitEvent(HabitsUiEvent.ShowSnackbar(Res.string.error_generic))
                }
            }
        }
    }

    private fun handleDeleteHabit(habitId: String) {
        intent {
            when (deleteHabitUseCase(habitId)) {
                is Result.Success ->
                    reduce { copy(bottomSheetMode = BottomSheetMode.Hidden) }

                is Result.Failure -> {
                    reduce { copy(bottomSheetMode = BottomSheetMode.Hidden) }
                    emitEvent(HabitsUiEvent.ShowSnackbar(Res.string.error_cannot_delete_core))
                }
            }
        }
    }
}
