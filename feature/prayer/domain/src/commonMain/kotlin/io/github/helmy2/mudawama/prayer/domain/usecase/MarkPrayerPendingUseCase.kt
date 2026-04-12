package io.github.helmy2.mudawama.prayer.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.domain.model.LogStatus
import io.github.helmy2.mudawama.habits.domain.usecase.SetHabitLogStatusUseCase
import io.github.helmy2.mudawama.prayer.domain.error.PrayerError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Resets a prayer's log status to [LogStatus.PENDING] for the given date.
 *
 * Spec (US2 acceptance scenario 3): long-press MISSED → "Undo / Mark Pending" → PENDING
 */
class MarkPrayerPendingUseCase(
    private val setHabitLogStatusUseCase: SetHabitLogStatusUseCase,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(prayerHabitId: String, date: LocalDate): EmptyResult<PrayerError> =
        withContext(dispatcher) {
            setHabitLogStatusUseCase(prayerHabitId, date, LogStatus.PENDING)
            Result.Success(Unit)
        }
}
