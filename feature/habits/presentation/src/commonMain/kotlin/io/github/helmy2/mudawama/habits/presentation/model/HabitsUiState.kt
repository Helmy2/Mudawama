package io.github.helmy2.mudawama.habits.presentation.model

import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus

/**
 * Immutable snapshot of the Habits screen's entire visual state.
 * The ViewModel emits a new copy whenever any field changes.
 */
data class HabitsUiState(
    val habits: List<HabitWithStatus> = emptyList(),
    val isLoading: Boolean = true,
    val bottomSheetMode: BottomSheetMode = BottomSheetMode.Hidden,
    val errorMessage: String? = null,
)

