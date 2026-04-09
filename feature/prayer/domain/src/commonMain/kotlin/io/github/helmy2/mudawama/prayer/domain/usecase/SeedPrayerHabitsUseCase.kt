package io.github.helmy2.mudawama.prayer.domain.usecase

import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.time.TimeProvider
import io.github.helmy2.mudawama.prayer.domain.model.PrayerHabitIds
import io.github.helmy2.mudawama.prayer.domain.repository.PrayerHabitRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SeedPrayerHabitsUseCase(
    private val prayerHabitRepository: PrayerHabitRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(): Result<Unit> = withContext(dispatcher) {
        prayerHabitRepository.seedPrayerHabitsIfNeeded()
    }
}
