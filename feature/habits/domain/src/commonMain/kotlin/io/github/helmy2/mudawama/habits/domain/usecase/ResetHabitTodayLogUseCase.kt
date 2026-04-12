package io.github.helmy2.mudawama.habits.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.core.time.toIsoDateString
import io.github.helmy2.mudawama.core.domain.error.HabitError
import io.github.helmy2.mudawama.habits.domain.repository.HabitLogRepository

/**
 * Deletes today's log for a given habit, effectively resetting progress to zero.
 * The UI can re-trigger creation via toggle/increment as normal after this.
 */
class ResetHabitTodayLogUseCase(
    private val habitLogRepository: HabitLogRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(habitId: String): EmptyResult<HabitError> {
        val today = toIsoDateString(timeProvider.logicalDate())
        habitLogRepository.deleteLogForHabitOnDate(habitId, today)
        return Result.Success(Unit)
    }
}
