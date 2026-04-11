package io.github.helmy2.mudawama.athkar.domain.repository

import io.github.helmy2.mudawama.athkar.domain.error.AthkarError
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.model.NotificationPreference
import io.github.helmy2.mudawama.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

interface AthkarNotificationRepository {

    /**
     * Observe the saved notification preference for [groupType].
     * Emits a default (disabled) preference if none has been saved yet.
     */
    fun observePreference(groupType: AthkarGroupType): Flow<NotificationPreference>

    /**
     * Persist [preference] and, if [NotificationPreference.enabled] is true,
     * reschedule the alarm for the given time. If disabled, cancels any existing alarm.
     *
     * [title] and [body] are the already-resolved notification strings from the ViewModel.
     */
    suspend fun savePreference(
        preference: NotificationPreference,
        title: String,
        body: String,
    ): EmptyResult<AthkarError>
}
