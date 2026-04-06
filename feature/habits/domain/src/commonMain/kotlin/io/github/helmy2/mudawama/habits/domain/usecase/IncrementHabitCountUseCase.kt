package io.github.helmy2.mudawama.habits.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.core.time.toIsoDateString
import io.github.helmy2.mudawama.habits.domain.error.HabitError
import io.github.helmy2.mudawama.habits.domain.model.HabitLog
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.habits.domain.repository.HabitLogRepository
import io.github.helmy2.mudawama.habits.domain.util.generateId
import kotlin.uuid.ExperimentalUuidApi

/**
 * Increments the count for a numeric habit for today.
 */
class IncrementHabitCountUseCase(
    private val habitLogRepository: HabitLogRepository,
    private val timeProvider: TimeProvider,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(habitId: String): EmptyResult<HabitError> {
        val today = toIsoDateString(timeProvider.logicalDate())
        val now = timeProvider.nowInstant().toEpochMilliseconds()
        val existing = habitLogRepository.getLogForHabitOnDate(habitId, today)

        val updated = if (existing == null) {
            HabitLog(
                id = generateId(),
                habitId = habitId,
                date = today,
                status = LogStatus.PENDING,
                completedCount = 1,
                loggedAt = now,
            )
        } else {
            existing.copy(completedCount = existing.completedCount + 1, loggedAt = now)
        }
        habitLogRepository.upsertLog(updated)
        return Result.Success(Unit)
    }
}

