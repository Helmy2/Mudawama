package io.github.helmy2.mudawama.athkar.domain.usecase

import io.github.helmy2.mudawama.athkar.domain.error.AthkarError
import io.github.helmy2.mudawama.athkar.domain.repository.TasbeehRepository
import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result

class SetTasbeehGoalUseCase(
    private val repository: TasbeehRepository,
) {
    suspend operator fun invoke(goalCount: Int): EmptyResult<AthkarError> {
        if (goalCount < 1) return Result.Failure(AthkarError.InvalidInput)
        return repository.setGoal(goalCount)
    }
}
