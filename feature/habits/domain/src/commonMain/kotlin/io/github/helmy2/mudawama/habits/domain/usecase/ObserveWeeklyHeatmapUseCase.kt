package io.github.helmy2.mudawama.habits.domain.usecase

import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.core.time.toIsoDateString
import io.github.helmy2.mudawama.habits.domain.model.HabitLog
import io.github.helmy2.mudawama.habits.domain.repository.HabitLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus

/**
 * Emits 7 entries for the rolling 7-day window; `null` at an index means no log record
 * exists for that day. Used by the presentation layer for per-habit heatmap display
 * (US6, SC-003). Index 0 = today.
 */
class ObserveWeeklyHeatmapUseCase(
    private val habitLogRepository: HabitLogRepository,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(habitId: String): Flow<List<HabitLog?>> {
        val today = timeProvider.logicalDate()
        val endDate = toIsoDateString(today)
        val startDate = toIsoDateString(today - DatePeriod(days = 6))

        return habitLogRepository.observeLogsForDateRange(startDate, endDate).map { logs ->
            val logsByDate = logs.filter { it.habitId == habitId }.associateBy { it.date }
            (0..6).map { daysAgo ->
                logsByDate[toIsoDateString(today - DatePeriod(days = daysAgo))]
            }
        }
    }
}

