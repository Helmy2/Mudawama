package io.github.helmy2.mudawama.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.helmy2.mudawama.core.database.entity.TasbeehDailyTotalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbeehDailyTotalDao {

    @Query("SELECT * FROM tasbeeh_daily_totals WHERE date = :date LIMIT 1")
    fun getTotalForDate(date: String): Flow<TasbeehDailyTotalEntity?>

    @Upsert
    suspend fun upsertTotal(entity: TasbeehDailyTotalEntity)
}
