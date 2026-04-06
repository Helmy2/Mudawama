package io.github.helmy2.mudawama.habits.data.repository

import io.github.helmy2.mudawama.core.database.dao.HabitLogDao
import io.github.helmy2.mudawama.habits.data.mapper.toDomain
import io.github.helmy2.mudawama.habits.data.mapper.toEntity
import io.github.helmy2.mudawama.habits.domain.model.HabitLog
import io.github.helmy2.mudawama.habits.domain.repository.HabitLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class HabitLogRepositoryImpl(
    private val dao: HabitLogDao,
) : HabitLogRepository {

    override fun observeLogsForDateRange(startDate: String, endDate: String): Flow<List<HabitLog>> =
        dao.getLogsForDateRange(startDate, endDate).map { entities -> entities.map { it.toDomain() } }

    override suspend fun upsertLog(log: HabitLog) =
        // insertLog uses OnConflictStrategy.REPLACE on HabitLogEntity.id — same UUID reuses the upsert path
        dao.insertLog(log.toEntity())

    override suspend fun getLogForHabitOnDate(habitId: String, date: String): HabitLog? =
        dao.getLogForHabitOnDate(habitId, date)?.toDomain()

    override suspend fun deleteLogForHabitOnDate(habitId: String, date: String) =
        dao.deleteLogForHabitOnDate(habitId, date)
}
