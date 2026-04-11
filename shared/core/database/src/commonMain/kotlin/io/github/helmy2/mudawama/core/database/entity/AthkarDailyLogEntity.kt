package io.github.helmy2.mudawama.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "athkar_daily_logs", primaryKeys = ["group_type", "date"])
data class AthkarDailyLogEntity(
    @ColumnInfo(name = "group_type") val groupType: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "counters_json") val counters: Map<String, Int>,
    @ColumnInfo(name = "is_complete") val isComplete: Boolean,
)
