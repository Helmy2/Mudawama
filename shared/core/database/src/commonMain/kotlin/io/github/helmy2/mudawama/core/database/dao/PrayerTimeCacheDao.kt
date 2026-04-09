package io.github.helmy2.mudawama.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.helmy2.mudawama.core.database.entity.PrayerTimeCacheEntity

@Dao
interface PrayerTimeCacheDao {
    @Query("SELECT * FROM prayer_time_cache WHERE date = :date")
    suspend fun getPrayerTimesForDate(date: String): PrayerTimeCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(entity: PrayerTimeCacheEntity)
}
