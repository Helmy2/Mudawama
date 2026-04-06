package io.github.helmy2.mudawama.habits.presentation.model

import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import kotlinx.datetime.DayOfWeek

/** All user-initiated events emitted from the Habits screen to the ViewModel. */
sealed interface HabitsUiAction {
    data object AddHabitFabClicked : HabitsUiAction
    data class HabitLongPressed(val habit: Habit) : HabitsUiAction
    data class EditHabitSelected(val habit: Habit) : HabitsUiAction
    data class DeleteHabitSelected(val habitId: String) : HabitsUiAction
    data class DeleteConfirmed(val habitId: String) : HabitsUiAction
    data class SaveHabit(
        val name: String,
        val iconKey: String,
        val frequencyDays: Set<DayOfWeek>,
        val type: HabitType,
        val goalCount: Int?,
    ) : HabitsUiAction
    data class ToggleCompletion(val habitId: String) : HabitsUiAction
    data class IncrementCount(val habitId: String) : HabitsUiAction
    data class DecrementCount(val habitId: String) : HabitsUiAction
    data class ResetTodayProgress(val habitId: String) : HabitsUiAction
    data object DismissBottomSheet : HabitsUiAction
    data object DismissError : HabitsUiAction
}

