package io.github.helmy2.mudawama.habits.data.mapper

import io.github.helmy2.mudawama.core.database.entity.HabitEntity
import io.github.helmy2.mudawama.habits.domain.model.Habit
import io.github.helmy2.mudawama.habits.domain.model.HabitType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber

/**
 * Converts a [HabitEntity] from the database layer to the domain [Habit] model.
 *
 * `frequencyDays` is stored as a comma-separated ISO day number string
 * (1 = Monday, 7 = Sunday per Decision 2 in research.md).
 */
fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    iconKey = iconKey,
    type = HabitType.valueOf(type),
    category = category,
    frequencyDays = frequencyDays
        .split(",")
        .filter { it.isNotBlank() }
        .map { DayOfWeek(it.trim().toInt()) }
        .toSet(),
    isCore = isCore,
    goalCount = goalCount,
    createdAt = createdAt,
)

/**
 * Converts a domain [Habit] to the database [HabitEntity].
 *
 * `frequencyDays` is serialised as a comma-separated string of ISO day numbers
 * (FR-009: comma-separated ordinal string).
 */
fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    type = type.name,
    category = category,
    frequencyDays = frequencyDays.joinToString(",") { it.isoDayNumber.toString() },
    isCore = isCore,
    goalCount = goalCount,
    createdAt = createdAt,
)

