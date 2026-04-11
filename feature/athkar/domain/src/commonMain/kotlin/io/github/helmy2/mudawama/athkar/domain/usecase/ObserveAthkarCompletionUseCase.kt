package io.github.helmy2.mudawama.athkar.domain.usecase

import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.repository.AthkarRepository
import kotlinx.coroutines.flow.Flow

class ObserveAthkarCompletionUseCase(
    private val repository: AthkarRepository,
) {
    operator fun invoke(date: String): Flow<Map<AthkarGroupType, Boolean>> =
        repository.observeCompletionStatus(date)
}
