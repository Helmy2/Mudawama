package io.github.helmy2.mudawama.quran.data.mapper

import io.github.helmy2.mudawama.core.database.entity.QuranGoalEntity
import io.github.helmy2.mudawama.quran.domain.model.QuranGoal

fun QuranGoalEntity.toDomain(): QuranGoal = QuranGoal(
    pagesPerDay = pagesPerDay,
    updatedAt = updatedAt,
)

fun QuranGoal.toEntity(): QuranGoalEntity = QuranGoalEntity(
    id = 1,
    pagesPerDay = pagesPerDay,
    updatedAt = updatedAt,
)
