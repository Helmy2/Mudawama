package io.github.helmy2.mudawama.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val type: String,
    val category: String,
    val frequencyDays: String,
    val isCore: Boolean,
    val goalCount: Int?,
    val createdAt: Long
)

