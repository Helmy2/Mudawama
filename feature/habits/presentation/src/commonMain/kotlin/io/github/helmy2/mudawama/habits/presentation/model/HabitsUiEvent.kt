package io.github.helmy2.mudawama.habits.presentation.model

import org.jetbrains.compose.resources.StringResource

/** One-shot events emitted from the ViewModel to the screen (not reflected in state). */
sealed interface HabitsUiEvent {
    data class ShowSnackbar(val message: StringResource) : HabitsUiEvent
}

