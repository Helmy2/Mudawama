package io.github.helmy2.mudawama.home.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed interface HomeUiEvent {
    data class ShowSnackbar(val message: StringResource) : HomeUiEvent

    /** Typed navigation destinations — no dependency on shared:navigation or NavKey. */
    sealed interface Navigate : HomeUiEvent {
        data object ToPrayer : Navigate
        data object ToAthkar : Navigate
        data object ToQuran : Navigate
        data object ToSettings : Navigate
        data object ToHabits : Navigate
        data object ToTasbeeh : Navigate
        data object ToQibla : Navigate
    }
}
