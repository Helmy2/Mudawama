package io.github.helmy2.mudawama.athkar.domain.usecase

import io.github.helmy2.mudawama.athkar.domain.error.AthkarError
import io.github.helmy2.mudawama.athkar.domain.repository.TasbeehRepository
import io.github.helmy2.mudawama.core.domain.EmptyResult

class AddToTasbeehDailyUseCase(
    private val repository: TasbeehRepository,
) {
    suspend operator fun invoke(date: String, amount: Int): EmptyResult<AthkarError> =
        repository.addToDaily(date, amount)
}
