package io.github.helmy2.mudawama.athkar.domain.usecase

import io.github.helmy2.mudawama.athkar.domain.error.AthkarError
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.model.AthkarPrayerSlot
import io.github.helmy2.mudawama.athkar.domain.repository.AthkarRepository
import io.github.helmy2.mudawama.core.domain.EmptyResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ResetAthkarItemUseCase(
    private val repository: AthkarRepository,
    private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        groupType: AthkarGroupType,
        date: String,
        itemId: String,
        prayerSlot: AthkarPrayerSlot? = null,
    ): EmptyResult<AthkarError> = withContext(dispatcher) {
        repository.resetItem(groupType, date, itemId, prayerSlot)
    }
}
