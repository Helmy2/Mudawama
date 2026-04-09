package io.github.helmy2.mudawama.prayer.data.repository

import io.github.helmy2.mudawama.core.location.Coordinates
import io.github.helmy2.mudawama.core.database.dao.PrayerTimeCacheDao
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.core.time.toIsoDateString
import io.github.helmy2.mudawama.prayer.data.mapper.toDomainList
import io.github.helmy2.mudawama.prayer.data.mapper.toEntity
import io.github.helmy2.mudawama.prayer.domain.error.PrayerError
import io.github.helmy2.mudawama.prayer.domain.model.PrayerTime
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerTimesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.datetime.LocalDate

internal class PrayerTimesRepositoryImpl(
    private val httpClient: HttpClient,
    private val cacheDao: PrayerTimeCacheDao,
    private val timeProvider: TimeProvider
) : PrayerTimesRepository {

    override suspend fun getPrayerTimes(date: LocalDate, coordinates: Coordinates): Result<List<PrayerTime>, PrayerError> {
        val cached = getCachedPrayerTimes(date)
        if (cached != null) return Result.Success(cached)

        return try {
            val dateString = toIsoDateString(date)
            // Aladhan API expects DD-MM-YYYY
            val formattedDate = "${date.day.toString().padStart(2, '0')}-${(date.month.ordinal + 1).toString().padStart(2, '0')}-${date.year}"
            
            val response: io.github.helmy2.mudawama.prayer.data.dto.AladhanResponseDto = httpClient.get("https://api.aladhan.com/v1/timings/$formattedDate") {
                url {
                    parameters.append("latitude", coordinates.latitude.toString())
                    parameters.append("longitude", coordinates.longitude.toString())
                    parameters.append("method", "2")
                }
            }.body()

            val entity = response.data.timings.toEntity(dateString, timeProvider.nowInstant().toEpochMilliseconds())
            cacheDao.insertPrayerTimes(entity)
            
            Result.Success(entity.toDomainList())
        } catch (e: Exception) {
            Result.Failure(PrayerError.NetworkError)
        }
    }

    override suspend fun getCachedPrayerTimes(date: LocalDate): List<PrayerTime>? {
        val entity = cacheDao.getPrayerTimesForDate(toIsoDateString(date))
        return entity?.toDomainList()
    }
}
