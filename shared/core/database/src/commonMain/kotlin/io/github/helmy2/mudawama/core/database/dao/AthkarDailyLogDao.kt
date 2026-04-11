package io.github.helmy2.mudawama.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.helmy2.mudawama.core.database.entity.AthkarDailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AthkarDailyLogDao {

    @Query("SELECT * FROM athkar_daily_logs WHERE group_type = :groupType AND date = :date LIMIT 1")
    fun getLog(groupType: String, date: String): Flow<AthkarDailyLogEntity?>

    @Query("SELECT * FROM athkar_daily_logs WHERE date = :date")
    fun getCompletionStatusForDate(date: String): Flow<List<AthkarDailyLogEntity>>

    @Query("SELECT * FROM athkar_daily_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getLogsForDateRange(startDate: String, endDate: String): Flow<List<AthkarDailyLogEntity>>

    @Upsert
    suspend fun upsertLog(entity: AthkarDailyLogEntity)
}
