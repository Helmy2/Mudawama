package io.github.helmy2.mudawama.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_bookmarks")
data class QuranBookmarkEntity(
    @PrimaryKey val id: Int = 1,
    val surah: Int,
    val ayah: Int,
    val dailyGoalPages: Int,
    val pagesReadToday: Int,
    val lastUpdated: Long
)

