package io.github.helmy2.mudawama.habits.domain.model

/**
 * Read-only projection emitted by [ObserveHabitsWithTodayStatusUseCase]. Never persisted.
 *
 * `weekLogs` contains exactly 7 entries ordered `[today, today-1, …, today-6]`;
 * a `null` entry means no log record exists for that day; this is a read-only
 * projection emitted by `ObserveHabitsWithTodayStatusUseCase` and is never persisted.
 *
 * Derived display properties computed in composables (never stored):
 * - `isCompletedToday = todayLog?.status == LogStatus.COMPLETED`
 * - `isDueToday = today's DayOfWeek in habit.frequencyDays`
 * - `numericProgress = todayLog?.completedCount ?: 0`
 * - `isNumericGoalReached = habit.goalCount != null && numericProgress >= habit.goalCount`
 */
data class HabitWithStatus(
    val habit: Habit,
    val todayLog: HabitLog?,
    val weekLogs: List<HabitLog?>,
)

