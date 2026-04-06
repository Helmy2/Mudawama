package io.github.helmy2.mudawama.habits.domain.error

import io.github.helmy2.mudawama.core.domain.DomainError

sealed interface HabitError : DomainError {
    data object CoreHabitCannotBeDeleted : HabitError
    data object EmptyHabitName : HabitError
    data object NoFrequencyDaySelected : HabitError
    data class HabitNotFound(val habitId: String) : HabitError
}

