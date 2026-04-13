package io.github.helmy2.mudawama.habits.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.core.time.toIsoDateString
import io.github.helmy2.mudawama.core.domain.error.HabitError
import io.github.helmy2.mudawama.core.domain.model.LogStatus
import io.github.helmy2.mudawama.habits.domain.repository.HabitLogRepository

/**
 * Decrements the count for a numeric habit for today, clamped to a minimum of 0.
 * If no log exists yet there is nothing to decrement; returns success immediately.
 */
class DecrementHabitCountUseCase(
    private val habitLogRepository: HabitLogRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(habitId: String): EmptyResult<HabitError> {
        val today = toIsoDateString(timeProvider.logicalDate())
        val existing = habitLogRepository.getLogForHabitOnDate(habitId, today)
            ?: return Result.Success(Unit)   // nothing to decrement

        val newCount = (existing.completedCount - 1).coerceAtLeast(0)
        val updated = existing.copy(
            completedCount = newCount,
            loggedAt = timeProvider.nowInstant().toEpochMilliseconds(),
            // If we've dropped back to 0 revert status to PENDING so the
            // habit is no longer shown as completed.
            status = if (existing.status == LogStatus.COMPLETED && newCount == 0)
                LogStatus.PENDING
            else
                existing.status,
        )
        habitLogRepository.upsertLog(updated)
        return Result.Success(Unit)
    }
}
