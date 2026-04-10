package io.github.helmy2.mudawama.quran.domain.repository

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.QuranDailyLog
import kotlinx.coroutines.flow.Flow

interface QuranDailyLogRepository {
    fun observeLogsForDate(date: String): Flow<List<QuranDailyLog>>
    fun observeRecentLogs(beforeDate: String): Flow<List<QuranDailyLog>>
    suspend fun insertLog(log: QuranDailyLog): EmptyResult<QuranError>
    suspend fun getAllLoggedDates(): Result<List<String>, QuranError>
}
