package io.github.helmy2.mudawama.athkar.presentation.tasbeeh

data class TasbeehUiState(
    val sessionCount: Int = 0,
    val dailyTotal: Int = 0,
    val goalCount: Int = 100,
    val isGoalSheetVisible: Boolean = false,
    val goalInputValue: String = "",
    val goalInputError: Boolean = false,
    val today: String = "",
) {
    val goalProgress: Float
        get() = if (goalCount > 0) (sessionCount.toFloat() / goalCount).coerceIn(0f, 1f) else 0f

    val isGoalReached: Boolean
        get() = sessionCount >= goalCount
}

sealed interface TasbeehUiAction {
    data object Tap : TasbeehUiAction
    data object Reset : TasbeehUiAction
    data object OpenGoalSheet : TasbeehUiAction
    data object CloseGoalSheet : TasbeehUiAction
    data class UpdateGoalInput(val value: String) : TasbeehUiAction
    data object ConfirmGoal : TasbeehUiAction
    data class SelectPresetGoal(val count: Int) : TasbeehUiAction
}

sealed interface TasbeehUiEvent {
    data object TapHaptic : TasbeehUiEvent
    data object GoalReached : TasbeehUiEvent
}
