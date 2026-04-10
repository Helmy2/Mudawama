package io.github.helmy2.mudawama.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_bookmarks")
data class QuranBookmarkEntity(
    @PrimaryKey val id: Int = 1,   // singleton row — always id=1
    val surah: Int,                // 1..114
    val ayah: Int,                 // 1..surah.ayahCount
    val lastUpdated: Long,         // epoch millis
)
