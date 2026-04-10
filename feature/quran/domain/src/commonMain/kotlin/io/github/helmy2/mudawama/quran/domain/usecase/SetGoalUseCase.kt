package io.github.helmy2.mudawama.quran.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.quran.domain.error.MAX_DAILY_GOAL_PAGES
import io.github.helmy2.mudawama.quran.domain.error.QuranError
import io.github.helmy2.mudawama.quran.domain.model.QuranGoal
import io.github.helmy2.mudawama.quran.domain.repository.QuranGoalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SetGoalUseCase(
    private val repo: QuranGoalRepository,
    private val timeProvider: TimeProvider,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(pagesPerDay: Int): EmptyResult<QuranError> =
        withContext(dispatcher) {
            if (pagesPerDay < 1 || pagesPerDay > MAX_DAILY_GOAL_PAGES) {
                return@withContext Result.Failure(QuranError.InvalidGoal)
            }
            repo.setGoal(
                QuranGoal(
                    pagesPerDay = pagesPerDay,
                    updatedAt = timeProvider.nowInstant().toEpochMilliseconds(),
                )
            )
        }
}
