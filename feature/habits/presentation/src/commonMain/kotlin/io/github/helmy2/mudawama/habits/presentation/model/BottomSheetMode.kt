package io.github.helmy2.mudawama.habits.presentation.model

import io.github.helmy2.mudawama.habits.domain.model.Habit

/**
 * Controls which bottom sheet (if any) is currently visible on the Habits screen.
 *
 * Transition table:
 *  Hidden        → AddHabit       (FAB tap)
 *  Hidden        → OptionsMenu    (long-press on a habit card)
 *  OptionsMenu   → EditHabit      (tap "Edit")
 *  OptionsMenu   → DeleteConfirm  (tap "Delete" — non-core habits only)
 *  AddHabit      → Hidden         (save success or dismiss)
 *  EditHabit     → Hidden         (save success or dismiss)
 *  DeleteConfirm → Hidden         (confirm or cancel)
 */
sealed interface BottomSheetMode {
    data object Hidden : BottomSheetMode
    data object AddHabit : BottomSheetMode
    data class OptionsMenu(val habit: Habit) : BottomSheetMode
    data class EditHabit(val habit: Habit) : BottomSheetMode
    data class DeleteConfirm(val habitId: String) : BottomSheetMode
}

