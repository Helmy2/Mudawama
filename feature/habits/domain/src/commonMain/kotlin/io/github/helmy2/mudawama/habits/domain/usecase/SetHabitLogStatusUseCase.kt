package io.github.helmy2.mudawama.habits.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.core.time.toIsoDateString
import io.github.helmy2.mudawama.core.domain.error.HabitError
import io.github.helmy2.mudawama.habits.domain.model.HabitLog
import io.github.helmy2.mudawama.core.domain.model.LogStatus
import io.github.helmy2.mudawama.habits.domain.repository.HabitLogRepository
import io.github.helmy2.mudawama.habits.domain.util.generateId
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi

/**
 * Explicitly sets a habit log to a given [LogStatus] for a specific date.
 *
 * Unlike [ToggleHabitCompletionUseCase] which cycles PENDING↔COMPLETED,
 * this use case is used when a specific target status is needed (e.g. MISSED).
 */
class SetHabitLogStatusUseCase(
    private val habitLogRepository: HabitLogRepository,
    private val timeProvider: TimeProvider,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        habitId: String,
        date: LocalDate,
        targetStatus: LogStatus,
    ): EmptyResult<HabitError> {
        val dateString = toIsoDateString(date)
        val now = timeProvider.nowInstant().toEpochMilliseconds()
        val existing = habitLogRepository.getLogForHabitOnDate(habitId, dateString)

        val updated = existing?.copy(status = targetStatus, loggedAt = now)
            ?: HabitLog(
                id = generateId(),
                habitId = habitId,
                date = dateString,
                status = targetStatus,
                completedCount = 0,
                loggedAt = now,
            )
        habitLogRepository.upsertLog(updated)
        return Result.Success(Unit)
    }
}
