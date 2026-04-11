package io.github.helmy2.mudawama.home.presentation.model

sealed interface HomeUiAction {
    // ── Habit interactions (toggle/count only; add/edit/delete in Habits tab) ─
    data class ToggleCompletion(val habitId: String) : HomeUiAction
    data class IncrementCount(val habitId: String) : HomeUiAction
    data class DecrementCount(val habitId: String) : HomeUiAction

    // ── Navigation ────────────────────────────────────────────────────────────
    data object PrayerCardTapped : HomeUiAction
    data object AthkarCardTapped : HomeUiAction
    data object QuranCardTapped : HomeUiAction
    data object TasbeehCardTapped : HomeUiAction
    data object SettingsIconTapped : HomeUiAction
    data object HabitsViewAllTapped : HomeUiAction
}
