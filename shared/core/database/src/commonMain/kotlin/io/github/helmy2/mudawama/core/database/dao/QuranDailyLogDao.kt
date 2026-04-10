package io.github.helmy2.mudawama.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.helmy2.mudawama.core.database.entity.QuranDailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDailyLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: QuranDailyLogEntity)

    @Query("SELECT * FROM quran_daily_logs WHERE date = :date ORDER BY loggedAt ASC")
    fun getLogsForDate(date: String): Flow<List<QuranDailyLogEntity>>

    @Query("""
        SELECT * FROM quran_daily_logs
        WHERE date < :beforeDate
        ORDER BY date DESC, loggedAt DESC
    """)
    fun getLogsBefore(beforeDate: String): Flow<List<QuranDailyLogEntity>>

    @Query("SELECT DISTINCT date FROM quran_daily_logs ORDER BY date DESC")
    suspend fun getAllLoggedDates(): List<String>
}
