package io.github.helmy2.mudawama.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.helmy2.mudawama.core.database.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity)

    @Update
    suspend fun updateLog(log: HabitLogEntity)

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId")
    fun getLogsForHabit(habitId: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLogForHabitOnDate(habitId: String, date: String): HabitLogEntity?

    @Query("SELECT * FROM habit_logs WHERE date >= :startDate AND date <= :endDate")
    fun getLogsForDateRange(startDate: String, endDate: String): Flow<List<HabitLogEntity>>
}

