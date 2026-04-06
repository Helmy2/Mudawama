package io.github.helmy2.mudawama.habits.domain.model

/**
 * Represents a single daily log record for a habit.
 *
 * `date` is ISO-8601 `yyyy-MM-dd` derived exclusively from
 * `TimeProvider.logicalDate()` — direct `Clock.System` calls are forbidden (FR-010, SC-002).
 *
 * The `(habitId, date)` pair is the logical composite key even though `id` (UUID)
 * is the physical primary key in the database.
 */
data class HabitLog(
    val id: String,
    val habitId: String,
    val date: String,
    val status: LogStatus,
    val completedCount: Int,
    val loggedAt: Long,
)

