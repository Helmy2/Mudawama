package io.github.helmy2.mudawama.athkar.presentation.tasbeeh

import io.github.helmy2.mudawama.athkar.domain.usecase.AddToTasbeehDailyUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveTasbeehDailyTotalUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveTasbeehGoalUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.SetTasbeehGoalUseCase
import io.github.helmy2.mudawama.core.presentation.mvi.MviViewModel
import io.github.helmy2.mudawama.core.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TasbeehViewModel(
    private val observeTasbeehGoalUseCase: ObserveTasbeehGoalUseCase,
    private val setTasbeehGoalUseCase: SetTasbeehGoalUseCase,
    private val observeTasbeehDailyTotalUseCase: ObserveTasbeehDailyTotalUseCase,
    private val addToTasbeehDailyUseCase: AddToTasbeehDailyUseCase,
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
) : MviViewModel<TasbeehUiState, TasbeehUiAction, TasbeehUiEvent>(
    initialState = TasbeehUiState(today = timeProvider.logicalDate().toString()),
) {

    init {
        observeGoal()
        observeDailyTotal()
    }

    private fun observeGoal() {
        intent {
            observeTasbeehGoalUseCase().collect { goal ->
                reduce { copy(goalCount = goal.goalCount) }
            }
        }
    }

    private fun observeDailyTotal() {
        intent {
            observeTasbeehDailyTotalUseCase(state.value.today).collect { total ->
                reduce { copy(dailyTotal = total.totalCount) }
            }
        }
    }

    override fun onAction(action: TasbeehUiAction) {
        when (action) {
            TasbeehUiAction.Tap -> handleTap()
            TasbeehUiAction.Reset -> handleReset()
            TasbeehUiAction.OpenGoalSheet -> reduce {
                copy(isGoalSheetVisible = true, goalInputValue = goalCount.toString(), goalInputError = false)
            }
            TasbeehUiAction.CloseGoalSheet -> reduce { copy(isGoalSheetVisible = false, goalInputError = false) }
            is TasbeehUiAction.UpdateGoalInput -> reduce { copy(goalInputValue = action.value, goalInputError = false) }
            TasbeehUiAction.ConfirmGoal -> confirmGoal()
            is TasbeehUiAction.SelectPresetGoal -> selectPresetGoal(action.count)
        }
    }

    private fun handleTap() {
        val current = state.value.sessionCount
        // Overflow guard: silently drop taps beyond Int.MAX_VALUE - 1
        if (current >= Int.MAX_VALUE - 1) return

        val wasGoalReached = state.value.isGoalReached
        val newCount = current + 1
        reduce { copy(sessionCount = newCount) }

        intent { emitEvent(TasbeehUiEvent.TapHaptic) }

        if (!wasGoalReached && newCount >= state.value.goalCount) {
            intent { emitEvent(TasbeehUiEvent.GoalReached) }
        }
    }

    private fun handleReset() {
        val sessionCount = state.value.sessionCount
        val today = state.value.today

        reduce { copy(sessionCount = 0) }

        if (sessionCount > 0) {
            exclusiveIntent("reset") {
                withContext(dispatcher) {
                    addToTasbeehDailyUseCase(today, sessionCount)
                }
            }
        }
    }

    private fun confirmGoal() {
        val input = state.value.goalInputValue.trim()
        val goalCount = input.toIntOrNull()
        if (goalCount == null || goalCount < 1) {
            reduce { copy(goalInputError = true) }
            return
        }
        intent {
            withContext(dispatcher) {
                setTasbeehGoalUseCase(goalCount)
            }
            reduce { copy(isGoalSheetVisible = false, goalInputError = false) }
        }
    }

    private fun selectPresetGoal(count: Int) {
        intent {
            withContext(dispatcher) {
                setTasbeehGoalUseCase(count)
            }
            reduce { copy(isGoalSheetVisible = false) }
        }
    }
}
