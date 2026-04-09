package io.github.helmy2.mudawama.prayer.domain.repository

import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.location.Coordinates
import io.github.helmy2.mudawama.prayer.domain.model.PrayerTime
import kotlinx.datetime.LocalDate

interface PrayerTimesRepository {
    /**
     * Fetches prayer times for the specified date.
     * Checks the local cache first. If absent, fetches from Aladhan API, caches, and returns.
     */
    suspend fun getPrayerTimes(date: LocalDate, coordinates: Coordinates): Result<List<PrayerTime>>
    
    /**
     * Optional: Just read from cache, do not trigger network fetch.
     * Useful for building projections when offline without triggering errors.
     */
    suspend fun getCachedPrayerTimes(date: LocalDate): List<PrayerTime>?
}
