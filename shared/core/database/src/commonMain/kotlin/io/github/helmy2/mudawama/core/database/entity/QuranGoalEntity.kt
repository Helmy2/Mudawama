package io.github.helmy2.mudawama.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_goals")
data class QuranGoalEntity(
    @PrimaryKey val id: Int = 1,   // singleton row — always id=1
    val pagesPerDay: Int,          // 1..60 (MAX_DAILY_GOAL_PAGES)
    val updatedAt: Long,           // epoch millis
)
