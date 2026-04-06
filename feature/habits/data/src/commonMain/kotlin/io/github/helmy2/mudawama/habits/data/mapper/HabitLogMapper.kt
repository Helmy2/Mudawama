package io.github.helmy2.mudawama.habits.data.mapper

import io.github.helmy2.mudawama.core.database.entity.HabitLogEntity
import io.github.helmy2.mudawama.habits.domain.model.HabitLog
import io.github.helmy2.mudawama.habits.domain.model.LogStatus

/**
 * Converts a [HabitLogEntity] from the database layer to the domain [HabitLog] model.
 */
fun HabitLogEntity.toDomain(): HabitLog = HabitLog(
    id = id,
    habitId = habitId,
    date = date,
    status = LogStatus.valueOf(status),
    completedCount = completedCount,
    loggedAt = loggedAt,
)

/**
 * Converts a domain [HabitLog] to the database [HabitLogEntity].
 */
fun HabitLog.toEntity(): HabitLogEntity = HabitLogEntity(
    id = id,
    habitId = habitId,
    date = date,
    status = status.name,
    completedCount = completedCount,
    loggedAt = loggedAt,
)

