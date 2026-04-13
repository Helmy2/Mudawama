package io.github.helmy2.mudawama.prayer.domain.usecase

import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.domain.error.HabitError
import io.github.helmy2.mudawama.habits.domain.usecase.ToggleHabitCompletionUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class TogglePrayerStatusUseCase(
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(prayerHabitId: String, date: LocalDate): Result<Unit, HabitError> = withContext(dispatcher) {
        toggleHabitCompletionUseCase(prayerHabitId)
    }
}
