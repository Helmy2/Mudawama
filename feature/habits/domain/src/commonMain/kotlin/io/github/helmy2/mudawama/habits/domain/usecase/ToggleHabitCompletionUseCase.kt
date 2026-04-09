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
 * Toggles a boolean habit's completion status for today.
 *
 * Decision 6 (research.md): get-then-branch keeps the toggle logic in the domain layer —
 * the repository's `upsertLog` is a pure write.
 */
class ToggleHabitCompletionUseCase(
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
                status = LogStatus.COMPLETED,
                completedCount = 0,
                loggedAt = now,
            )
        } else {
            val newStatus = when (existing.status) {
                LogStatus.COMPLETED -> LogStatus.PENDING
                LogStatus.PENDING, LogStatus.MISSED -> LogStatus.COMPLETED
            }
            existing.copy(status = newStatus, loggedAt = now)
        }
        habitLogRepository.upsertLog(updated)
        return Result.Success(Unit)
    }
}

