package io.github.helmy2.mudawama.prayer.domain.repository

import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import io.github.helmy2.mudawama.prayer.domain.error.PrayerError
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface PrayerHabitRepository {
    /**
     * Observes the 5 obligatory prayer habits combined with their log status for the given date.
     * Under the hood, this delegates to `HabitDao` and `HabitLogDao`, filtering for `category = "prayer"`.
     */
    fun observePrayerHabitsWithStatus(date: LocalDate): Flow<List<HabitWithStatus>>
    
    /**
     * Checks if the 5 prayers exist. If not, inserts them with stable IDs.
     */
    suspend fun seedPrayerHabitsIfNeeded(): Result<Unit, PrayerError>
}
