package io.github.helmy2.mudawama.athkar.domain.usecase

import io.github.helmy2.mudawama.athkar.domain.model.AthkarDailyLog
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.repository.AthkarRepository
import kotlinx.coroutines.flow.Flow

class ObserveAthkarLogUseCase(
    private val repository: AthkarRepository,
) {
    operator fun invoke(groupType: AthkarGroupType, date: String): Flow<AthkarDailyLog?> =
        repository.observeLog(groupType, date)
}
