package io.github.helmy2.mudawama.athkar.domain.usecase

import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.model.NotificationPreference
import io.github.helmy2.mudawama.athkar.domain.repository.AthkarNotificationRepository
import kotlinx.coroutines.flow.Flow

class ObserveNotificationPreferenceUseCase(
    private val repository: AthkarNotificationRepository,
) {
    operator fun invoke(groupType: AthkarGroupType): Flow<NotificationPreference> =
        repository.observePreference(groupType)
}
