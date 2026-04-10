package io.github.helmy2.mudawama.quran.data.mapper

import io.github.helmy2.mudawama.core.database.entity.QuranDailyLogEntity
import io.github.helmy2.mudawama.quran.domain.model.QuranDailyLog

fun QuranDailyLogEntity.toDomain(): QuranDailyLog = QuranDailyLog(
    id = id,
    date = date,
    pagesRead = pagesRead,
    loggedAt = loggedAt,
)

fun QuranDailyLog.toEntity(): QuranDailyLogEntity = QuranDailyLogEntity(
    id = id,
    date = date,
    pagesRead = pagesRead,
    loggedAt = loggedAt,
)
