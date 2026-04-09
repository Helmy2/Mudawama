package io.github.helmy2.mudawama.prayer.data.mapper

import io.github.helmy2.mudawama.core.database.entity.PrayerTimeCacheEntity
import io.github.helmy2.mudawama.prayer.data.dto.AladhanTimingsDto
import io.github.helmy2.mudawama.prayer.domain.model.PrayerName
import io.github.helmy2.mudawama.prayer.domain.model.PrayerTime

fun AladhanTimingsDto.toEntity(date: String, fetchedAt: Long): PrayerTimeCacheEntity = PrayerTimeCacheEntity(
    date = date,
    fajr = fajr,
    dhuhr = dhuhr,
    asr = asr,
    maghrib = maghrib,
    isha = isha,
    fetchedAt = fetchedAt
)

fun PrayerTimeCacheEntity.toDomainList(): List<PrayerTime> = listOf(
    PrayerTime(PrayerName.FAJR, fajr),
    PrayerTime(PrayerName.DHUHR, dhuhr),
    PrayerTime(PrayerName.ASR, asr),
    PrayerTime(PrayerName.MAGHRIB, maghrib),
    PrayerTime(PrayerName.ISHA, isha)
)
