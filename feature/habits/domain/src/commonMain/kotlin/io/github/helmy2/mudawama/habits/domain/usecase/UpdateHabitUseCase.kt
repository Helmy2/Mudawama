package io.github.helmy2.mudawama.habits.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.habits.domain.error.HabitError
import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.repository.HabitRepository

/**
 * Updates an existing habit.
 *
 * Caller is responsible for constructing the updated [Habit] instance via
 * `existing.copy(…)` before passing it here.
 */
class UpdateHabitUseCase(
    private val habitRepository: HabitRepository,
) {
    suspend operator fun invoke(habit: Habit): EmptyResult<HabitError> {
        if (habit.name.isBlank()) return Result.Failure(HabitError.EmptyHabitName)
        if (habit.frequencyDays.isEmpty()) return Result.Failure(HabitError.NoFrequencyDaySelected)
        habitRepository.upsertHabit(habit)
        return Result.Success(Unit)
    }
}

