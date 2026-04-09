package io.github.helmy2.mudawama.prayer.domain.usecase

import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.habits.domain.model.LogStatus
import io.github.helmy2.mudawama.habits.domain.usecase.SetHabitLogStatusUseCase
import io.github.helmy2.mudawama.prayer.domain.error.PrayerError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Explicitly marks a prayer habit log as [LogStatus.MISSED] for the given date.
 *
 * Spec (US2): long-press → action sheet → "Mark as Missed"
 */
class MarkPrayerMissedUseCase(
    private val setHabitLogStatusUseCase: SetHabitLogStatusUseCase,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(prayerHabitId: String, date: LocalDate): EmptyResult<PrayerError> =
        withContext(dispatcher) {
            setHabitLogStatusUseCase(prayerHabitId, date, LogStatus.MISSED)
            Result.Success(Unit)
        }
}
