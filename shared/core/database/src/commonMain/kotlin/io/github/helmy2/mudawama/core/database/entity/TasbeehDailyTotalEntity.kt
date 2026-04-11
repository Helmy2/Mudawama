package io.github.helmy2.mudawama.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasbeeh_daily_totals")
data class TasbeehDailyTotalEntity(
    @PrimaryKey val date: String,
    @ColumnInfo(name = "total_count") val totalCount: Int = 0,
)
