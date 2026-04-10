package io.github.helmy2.mudawama.quran.data.repository

import io.github.helmy2.mudawama.core.database.dao.QuranDailyLogDao
import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.domain.safeCall
import io.github.helmy2.mudawama.quran.data.mapper.toDomain
import io.github.helmy2.mudawama.quran.data.mapper.toEntity
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.QuranDailyLog
import io.github.helmy2.mudawama.quran.domain.repository.QuranDailyLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class QuranDailyLogRepositoryImpl(
    private val dao: QuranDailyLogDao,
) : QuranDailyLogRepository {

    override fun observeLogsForDate(date: String): Flow<List<QuranDailyLog>> =
        dao.getLogsForDate(date).map { entities -> entities.map { it.toDomain() } }

    override fun observeRecentLogs(beforeDate: String): Flow<List<QuranDailyLog>> =
        dao.getLogsBefore(beforeDate).map { entities ->
            entities
                .groupBy { it.date }
                .entries
                .take(3)
                .flatMap { (_, logs) -> logs.map { it.toDomain() } }
        }

    override suspend fun insertLog(log: QuranDailyLog): EmptyResult<QuranError> =
        safeCall(
            block = { dao.insertLog(log.toEntity()) },
            onError = { QuranError.DatabaseError },
        )

    override suspend fun getAllLoggedDates(): Result<List<String>, QuranError> =
        safeCall(
            block = { dao.getAllLoggedDates() },
            onError = { QuranError.DatabaseError },
        )
}
