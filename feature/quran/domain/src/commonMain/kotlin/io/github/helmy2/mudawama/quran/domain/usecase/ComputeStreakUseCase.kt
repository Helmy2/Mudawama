package io.github.helmy2.mudawama.quran.domain.usecase

import io.github.helmy2.mudawama.core.domain.getOrNull
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.quran.domain.repository.QuranDailyLogRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class ComputeStreakUseCase(
    private val repo: QuranDailyLogRepository,
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(): Int = withContext(dispatcher) {
        val dates = repo.getAllLoggedDates().getOrNull() ?: return@withContext 0
        val yesterday = timeProvider.logicalDate().minus(DatePeriod(days = 1))
        var streak = 0
        var expected = yesterday
        for (dateStr in dates) {
            val date = LocalDate.parse(dateStr)
            if (date > yesterday) continue    // skip today (not yet closed, per FR-013)
            if (date == expected) {
                streak++
                expected = expected.minus(DatePeriod(days = 1))
            } else {
                break                         // gap found — streak ends
            }
        }
        streak
    }
}
