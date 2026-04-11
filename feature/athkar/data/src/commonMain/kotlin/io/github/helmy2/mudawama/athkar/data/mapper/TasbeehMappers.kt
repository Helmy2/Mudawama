package io.github.helmy2.mudawama.athkar.data.mapper

import io.github.helmy2.mudawama.athkar.domain.model.TasbeehDailyTotal
import io.github.helmy2.mudawama.athkar.domain.model.TasbeehGoal
import io.github.helmy2.mudawama.core.database.entity.TasbeehDailyTotalEntity
import io.github.helmy2.mudawama.core.database.entity.TasbeehGoalEntity

internal fun TasbeehGoalEntity.toDomain(): TasbeehGoal = TasbeehGoal(goalCount = goalCount)

internal fun TasbeehDailyTotalEntity.toDomain(): TasbeehDailyTotal =
    TasbeehDailyTotal(date = date, totalCount = totalCount)
