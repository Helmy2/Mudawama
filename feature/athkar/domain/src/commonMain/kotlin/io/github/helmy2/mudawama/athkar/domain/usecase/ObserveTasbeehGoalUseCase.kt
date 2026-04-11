package io.github.helmy2.mudawama.athkar.domain.usecase

import io.github.helmy2.mudawama.athkar.domain.model.TasbeehGoal
import io.github.helmy2.mudawama.athkar.domain.repository.TasbeehRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveTasbeehGoalUseCase(
    private val repository: TasbeehRepository,
) {
    operator fun invoke(): Flow<TasbeehGoal> = repository.observeGoal()
}
