package io.github.helmy2.mudawama.prayer.domain.usecase

import io.github.helmy2.mudawama.core.location.Coordinates
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.prayer.domain.error.PrayerError
import io.github.helmy2.mudawama.prayer.domain.model.PrayerName
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerHabitRepository
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerTimesRepository
import io.github.helmy2.mudawama.settings.domain.CalculationMethod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class ObservePrayersForDateUseCase(
    private val prayerHabitRepository: PrayerHabitRepository,
    private val prayerTimesRepository: PrayerTimesRepository,
    private val dispatcher: CoroutineDispatcher
) {
    operator fun invoke(date: LocalDate, coordinates: Coordinates, method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE): Flow<Result<List<PrayerWithStatus>, PrayerError>> = flow {
        // Fetch times once and combine with the habits flow.
        val timesResult = withContext(dispatcher) {
            prayerTimesRepository.getPrayerTimes(date, coordinates, method)
        }

        prayerHabitRepository.observePrayerHabitsWithStatus(date).collect { habits ->
            val prayerHabits = habits.filter { it.habit.category == "prayer" }
                .sortedBy { PrayerName.valueOf(it.habit.name.uppercase()).ordinal }

            val prayersWithStatus = if (timesResult is Result.Success) {
                val times = timesResult.data
                prayerHabits.mapIndexed { index, habitWithStatus ->
                    val time = times.getOrNull(index)
                    PrayerWithStatus(
                        habitId = habitWithStatus.habit.id,
                        name = PrayerName.valueOf(habitWithStatus.habit.name.uppercase()),
                        timeString = time?.timeString ?: "—",
                        status = habitWithStatus.todayLog?.status ?: LogStatus.PENDING
                    )
                }
            } else {
                prayerHabits.map { habitWithStatus ->
                    PrayerWithStatus(
                        habitId = habitWithStatus.habit.id,
                        name = PrayerName.valueOf(habitWithStatus.habit.name.uppercase()),
                        timeString = "—",
                        status = habitWithStatus.todayLog?.status ?: LogStatus.PENDING
                    )
                }
            }
            emit(Result.Success(prayersWithStatus))
        }
    }
}
