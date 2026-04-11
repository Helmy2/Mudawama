package io.github.helmy2.mudawama.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasbeeh_goals")
data class TasbeehGoalEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "goal_count") val goalCount: Int = 100,
)
