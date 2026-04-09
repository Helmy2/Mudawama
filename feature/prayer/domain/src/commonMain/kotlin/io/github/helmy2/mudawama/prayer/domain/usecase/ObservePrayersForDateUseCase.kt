package io.github.helmy2.mudawama.prayer.domain.usecase

import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.location.Coordinates
import io.github.helmy2.mudawama.core.time.toIsoDateString
import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import io.github.helmy2.mudawama.prayer.domain.model.PrayerWithStatus
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerHabitRepository
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerTimesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class ObservePrayersForDateUseCase(
    private val prayerHabitRepository: PrayerHabitRepository,
    private val prayerTimesRepository: PrayerTimesRepository,
    private val dispatcher: CoroutineDispatcher
) {
    operator fun invoke(date: LocalDate, coordinates: Coordinates): Flow<Result<List<PrayerWithStatus>>> = flow {
        // Implementation Note: Fetch times once and combine with the habits flow.
        val timesResult = withContext(dispatcher) {
            prayerTimesRepository.getPrayerTimes(date, coordinates)
        }

        prayerHabitRepository.observePrayerHabitsWithStatus(date).collect { habits ->
            val prayerHabits = habits.filter { it.habit.category == "prayer" }
                .sortedBy { it.habit.name } // Assumes names are ordered or ID based

            val prayersWithStatus = if (timesResult is Result.Success) {
                val times = timesResult.data
                prayerHabits.mapIndexed { index, habitWithStatus ->
                    val time = times.getOrNull(index)
                    PrayerWithStatus(
                        habitId = habitWithStatus.habit.id,
                        name = habitWithStatus.habit.name.let { io.github.helmy2.mudawama.prayer.domain.model.PrayerName.valueOf(it.uppercase()) },
                        timeString = time?.timeString ?: "—",
                        status = habitWithStatus.todayLog?.status ?: io.github.helmy2.mudawama.habits.domain.model.LogStatus.PENDING
                    )
                }
            } else {
                prayerHabits.map { habitWithStatus ->
                    PrayerWithStatus(
                        habitId = habitWithStatus.habit.id,
                        name = io.github.helmy2.mudawama.prayer.domain.model.PrayerName.valueOf(habitWithStatus.habit.name.uppercase()),
                        timeString = "—",
                        status = habitWithStatus.todayLog?.status ?: io.github.helmy2.mudawama.habits.domain.model.LogStatus.PENDING
                    )
                }
            }
            emit(Result.Success(prayersWithStatus))
        }
    }
}
