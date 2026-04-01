package io.github.helmy2.mudawama.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class HabitLogEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val date: String,
    val status: String,
    val completedCount: Int,
    val loggedAt: Long
)

