package io.github.helmy2.mudawama.athkar.data.mapper

import io.github.helmy2.mudawama.athkar.domain.model.AthkarDailyLog
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.core.database.entity.AthkarDailyLogEntity

internal fun AthkarDailyLogEntity.toDomain(): AthkarDailyLog = AthkarDailyLog(
    groupType = AthkarGroupType.valueOf(groupType),
    date = date,
    counters = counters,
    isComplete = isComplete,
)

internal fun AthkarDailyLog.toEntity(): AthkarDailyLogEntity = AthkarDailyLogEntity(
    groupType = groupType.name,
    date = date,
    counters = counters,
    isComplete = isComplete,
)
