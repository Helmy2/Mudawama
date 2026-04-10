package io.github.helmy2.mudawama.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_daily_logs")
data class QuranDailyLogEntity(
    @PrimaryKey val id: String,    // UUID string (generated in domain)
    val date: String,              // "yyyy-MM-dd" ISO-8601
    val pagesRead: Int,            // pages read in this session (1..604)
    val loggedAt: Long,            // epoch millis
)
