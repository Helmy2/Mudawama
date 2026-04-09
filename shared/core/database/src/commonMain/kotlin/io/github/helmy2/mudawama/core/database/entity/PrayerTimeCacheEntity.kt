package io.github.helmy2.mudawama.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_time_cache")
data class PrayerTimeCacheEntity(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val fajr: String, // "HH:mm"
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val fetchedAt: Long // epoch ms
)
