package io.github.helmy2.mudawama.habits.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.domain.error.HabitError
import io.github.helmy2.mudawama.habits.domain.repository.HabitRepository

/**
 * Deletes a custom (non-core) habit.
 *
 * Room `ForeignKey.CASCADE` on `HabitLogEntity.habitId` ensures all associated
 * log rows are deleted atomically by the database — no explicit log deletion is
 * required in this use case.
 */
class DeleteHabitUseCase(
    private val habitRepository: HabitRepository,
) {
    suspend operator fun invoke(habitId: String): EmptyResult<HabitError> {
        val habit = habitRepository.getHabitById(habitId)
            ?: return Result.Failure(HabitError.HabitNotFound(habitId))

        if (habit.isCore) return Result.Failure(HabitError.CoreHabitCannotBeDeleted)

        habitRepository.deleteHabit(habitId)
        return Result.Success(Unit)
    }
}

