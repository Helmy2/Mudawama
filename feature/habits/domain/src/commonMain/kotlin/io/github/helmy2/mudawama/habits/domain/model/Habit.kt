package io.github.helmy2.mudawama.habits.domain.model

import kotlinx.datetime.DayOfWeek

/**
 * Immutable value type representing a user habit definition.
 *
 * Domain invariants (enforced by use cases, not the constructor):
 * - `name.isNotBlank()`
 * - `frequencyDays.isNotEmpty()`
 * - `type == BOOLEAN implies goalCount == null`
 */
data class Habit(
    val id: String,
    val name: String,
    val iconKey: String,
    val type: HabitType,
    val category: String,
    val frequencyDays: Set<DayOfWeek>,
    val isCore: Boolean,
    val goalCount: Int?,
    val createdAt: Long,
)

