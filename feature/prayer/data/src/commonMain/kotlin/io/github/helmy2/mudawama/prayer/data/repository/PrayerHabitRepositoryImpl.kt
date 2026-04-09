package io.github.helmy2.mudawama.prayer.data.repository

import io.github.helmy2.mudawama.core.database.dao.HabitDao
import io.github.helmy2.mudawama.core.database.dao.HabitLogDao
import io.github.helmy2.mudawama.core.database.entity.HabitEntity
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.core.time.toIsoDateString
import io.github.helmy2.mudawama.habits.data.mapper.toDomain
import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import io.github.helmy2.mudawama.prayer.domain.error.PrayerError
import io.github.helmy2.mudawama.prayer.domain.model.PrayerHabitIds
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerHabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate

internal class PrayerHabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val timeProvider: TimeProvider
) : PrayerHabitRepository {

    override fun observePrayerHabitsWithStatus(date: LocalDate): Flow<List<HabitWithStatus>> {
        val dateString = toIsoDateString(date)
        return combine(
            habitDao.getHabitsByCategory("prayer"),
            habitLogDao.getLogsForDate(dateString)
        ) { habits, logs ->
            val logsByHabit = logs.associateBy { it.habitId }
            habits.map { habit ->
                HabitWithStatus(
                    habit = habit.toDomain(),
                    todayLog = logsByHabit[habit.id]?.toDomain(),
                    weekLogs = emptyList() // Not used by prayer screen
                )
            }
        }
    }

    override suspend fun seedPrayerHabitsIfNeeded(): Result<Unit, PrayerError> {
        return try {
            val existing = habitDao.getHabitsByCategory("prayer").firstOrNull()
            if (existing != null && existing.size >= 5) {
                return Result.Success(Unit)
            }

            val now = timeProvider.nowInstant().toEpochMilliseconds()
            val allDays = "1,2,3,4,5,6,7"

            val prayers = listOf(
                HabitEntity(PrayerHabitIds.FAJR, "Fajr", "mosque", "BOOLEAN", "prayer", allDays, true, null, now),
                HabitEntity(PrayerHabitIds.DHUHR, "Dhuhr", "mosque", "BOOLEAN", "prayer", allDays, true, null, now),
                HabitEntity(PrayerHabitIds.ASR, "Asr", "mosque", "BOOLEAN", "prayer", allDays, true, null, now),
                HabitEntity(PrayerHabitIds.MAGHRIB, "Maghrib", "mosque", "BOOLEAN", "prayer", allDays, true, null, now),
                HabitEntity(PrayerHabitIds.ISHA, "Isha", "mosque", "BOOLEAN", "prayer", allDays, true, null, now),
            )

            prayers.forEach { habitDao.insertHabit(it) }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(PrayerError.DatabaseError)
        }
    }
}
