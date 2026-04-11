package io.github.helmy2.mudawama.athkar.domain.usecase

import io.github.helmy2.mudawama.athkar.domain.error.AthkarError
import io.github.helmy2.mudawama.athkar.domain.model.NotificationPreference
import io.github.helmy2.mudawama.athkar.domain.repository.AthkarNotificationRepository
import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.Result

class SaveNotificationPreferenceUseCase(
    private val repository: AthkarNotificationRepository,
) {
    suspend operator fun invoke(
        preference: NotificationPreference,
        title: String,
        body: String,
    ): EmptyResult<AthkarError> {
        if (preference.hour !in 0..23) return Result.Failure(AthkarError.InvalidInput)
        if (preference.minute !in 0..59) return Result.Failure(AthkarError.InvalidInput)
        return repository.savePreference(preference, title, body)
    }
}
