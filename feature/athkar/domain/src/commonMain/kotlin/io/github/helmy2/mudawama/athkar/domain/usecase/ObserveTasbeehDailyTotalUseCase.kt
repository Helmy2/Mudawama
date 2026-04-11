package io.github.helmy2.mudawama.athkar.domain.usecase

import io.github.helmy2.mudawama.athkar.domain.model.TasbeehDailyTotal
import io.github.helmy2.mudawama.athkar.domain.repository.TasbeehRepository
import kotlinx.coroutines.flow.Flow

class ObserveTasbeehDailyTotalUseCase(
    private val repository: TasbeehRepository,
) {
    operator fun invoke(date: String): Flow<TasbeehDailyTotal> =
        repository.observeDailyTotal(date)
}
