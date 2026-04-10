package io.github.helmy2.mudawama.quran.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.quran.domain.error.MAX_SESSION_PAGES
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.QuranDailyLog
import io.github.helmy2.mudawama.quran.domain.repository.QuranDailyLogRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class LogReadingUseCase(
    private val repo: QuranDailyLogRepository,
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(pages: Int, date: LocalDate): EmptyResult<QuranError> =
        withContext(dispatcher) {
            if (pages < 1 || pages > MAX_SESSION_PAGES) {
                return@withContext Result.Failure(QuranError.InvalidPageCount)
            }
            val log = QuranDailyLog(
                id = generateUuid(),
                date = date.toString(),
                pagesRead = pages,
                loggedAt = timeProvider.nowInstant().toEpochMilliseconds(),
            )
            repo.insertLog(log)
        }

    private fun generateUuid(): String {
        // KMP-compatible UUID generation using random bytes
        val bytes = ByteArray(16)
        for (i in bytes.indices) {
            bytes[i] = (kotlin.random.Random.nextInt(256) - 128).toByte()
        }
        bytes[6] = ((bytes[6].toInt() and 0x0f) or 0x40).toByte()
        bytes[8] = ((bytes[8].toInt() and 0x3f) or 0x80).toByte()
        return bytes.toHexString()
    }

    private fun ByteArray.toHexString(): String {
        return buildString {
            forEachIndexed { index, byte ->
                if (index == 4 || index == 6 || index == 8 || index == 10) append('-')
                append(byte.code.and(0xff).toString(16).padStart(2, '0'))
            }
        }
    }
}
