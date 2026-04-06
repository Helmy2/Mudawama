package io.github.helmy2.mudawama.habits.domain.repository

import io.github.helmy2.mudawama.habits.domain.model.HabitLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for [HabitLog] persistence.
 *
 * `observeLogsForDateRange`: Both `startDate` and `endDate` must be ISO-8601 `yyyy-MM-dd`
 * strings produced by `toIsoDateString(TimeProvider.logicalDate())`. Lexicographic ordering
 * equals chronological ordering for this format (Decision 5 in research.md).
 */
interface HabitLogRepository {
    fun observeLogsForDateRange(startDate: String, endDate: String): Flow<List<HabitLog>>
    suspend fun upsertLog(log: HabitLog)
    suspend fun getLogForHabitOnDate(habitId: String, date: String): HabitLog?
    suspend fun deleteLogForHabitOnDate(habitId: String, date: String)
}

