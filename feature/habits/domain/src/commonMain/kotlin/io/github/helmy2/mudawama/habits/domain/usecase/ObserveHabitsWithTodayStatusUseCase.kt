package io.github.helmy2.mudawama.habits.domain.usecase

import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.core.time.toIsoDateString
import io.github.helmy2.mudawama.habits.domain.model.HabitWithStatus
import io.github.helmy2.mudawama.habits.domain.repository.HabitLogRepository
import io.github.helmy2.mudawama.habits.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Combines the live habit list with the rolling 7-day log window to produce a
 * `Flow<List<HabitWithStatus>>` that the ViewModel collects once at init.
 *
 * Why `combine` and not `flatMapLatest`: Both habit definitions and daily logs can change
 * independently. `combine` emits a new merged list whenever *either* upstream Flow emits,
 * guaranteeing the UI is always consistent with the latest database state.
 * `flatMapLatest` would re-subscribe the logs Flow on every habits emission, creating
 * unnecessary overhead.
 *
 * Date window capture: `today`, `startDate`, and `endDate` are captured at `invoke()` call
 * time (ViewModel init). The Room Flow emits reactively for all changes within the fixed
 * window. This is correct for the primary use case (same-day session). Midnight-rollover is
 * a documented v1 limitation (Decision 4 in research.md).
 */
class ObserveHabitsWithTodayStatusUseCase(
    private val habitRepository: HabitRepository,
    private val habitLogRepository: HabitLogRepository,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(): Flow<List<HabitWithStatus>> {
        val today: LocalDate = timeProvider.logicalDate()
        val endDate: String = toIsoDateString(today)
        val startDate: String = toIsoDateString(today - DatePeriod(days = 6))

        return combine(
            habitRepository.observeAllHabits(),
            habitLogRepository.observeLogsForDateRange(startDate, endDate),
        ) { habits, logs ->
            // Build a two-level map: habitId -> (date -> HabitLog)
            val logsByHabitByDate = logs
                .groupBy { it.habitId }
                .mapValues { (_, l) -> l.associateBy { it.date } }

            // Fixed ordered list of the last 7 date strings: index 0 = today
            val last7Dates: List<String> = (0..6).map { daysAgo ->
                toIsoDateString(today - DatePeriod(days = daysAgo))
            }

            habits.map { habit ->
                val logMap = logsByHabitByDate[habit.id] ?: emptyMap()
                HabitWithStatus(
                    habit = habit,
                    todayLog = logMap[endDate],
                    weekLogs = last7Dates.map { date -> logMap[date] },
                )
            }
        }
    }
}

