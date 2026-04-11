package io.github.helmy2.mudawama.athkar.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.helmy2.mudawama.athkar.domain.error.AthkarError
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.model.AthkarNotificationIds
import io.github.helmy2.mudawama.athkar.domain.model.NotificationPreference
import io.github.helmy2.mudawama.athkar.domain.repository.AthkarNotificationRepository
import io.github.helmy2.mudawama.core.data.athkar.AthkarPreferencesKeys
import io.github.helmy2.mudawama.core.domain.EmptyResult
import io.github.helmy2.mudawama.core.domain.notification.NotificationScheduler
import io.github.helmy2.mudawama.core.domain.safeCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class AthkarNotificationRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val scheduler: NotificationScheduler,
    private val dispatcher: CoroutineDispatcher,
) : AthkarNotificationRepository {

    override fun observePreference(groupType: AthkarGroupType): Flow<NotificationPreference> =
        dataStore.data.map { prefs ->
            when (groupType) {
                AthkarGroupType.MORNING -> NotificationPreference(
                    groupType = AthkarGroupType.MORNING,
                    enabled = prefs[AthkarPreferencesKeys.MORNING_NOTIF_ENABLED] ?: false,
                    hour = prefs[AthkarPreferencesKeys.MORNING_NOTIF_HOUR] ?: 6,
                    minute = prefs[AthkarPreferencesKeys.MORNING_NOTIF_MINUTE] ?: 0,
                )
                AthkarGroupType.EVENING -> NotificationPreference(
                    groupType = AthkarGroupType.EVENING,
                    enabled = prefs[AthkarPreferencesKeys.EVENING_NOTIF_ENABLED] ?: false,
                    hour = prefs[AthkarPreferencesKeys.EVENING_NOTIF_HOUR] ?: 18,
                    minute = prefs[AthkarPreferencesKeys.EVENING_NOTIF_MINUTE] ?: 0,
                )
                // POST_PRAYER has no notification preference; return a disabled stub.
                AthkarGroupType.POST_PRAYER -> NotificationPreference(
                    groupType = AthkarGroupType.POST_PRAYER,
                    enabled = false,
                    hour = 0,
                    minute = 0,
                )
            }
        }

    override suspend fun savePreference(
        preference: NotificationPreference,
        title: String,
        body: String,
    ): EmptyResult<AthkarError> =
        withContext(dispatcher) {
            safeCall(
                block = {
                    // 1. Persist to DataStore
                    dataStore.edit { prefs ->
                        when (preference.groupType) {
                            AthkarGroupType.MORNING -> {
                                prefs[AthkarPreferencesKeys.MORNING_NOTIF_ENABLED] = preference.enabled
                                prefs[AthkarPreferencesKeys.MORNING_NOTIF_HOUR] = preference.hour
                                prefs[AthkarPreferencesKeys.MORNING_NOTIF_MINUTE] = preference.minute
                            }
                            AthkarGroupType.EVENING -> {
                                prefs[AthkarPreferencesKeys.EVENING_NOTIF_ENABLED] = preference.enabled
                                prefs[AthkarPreferencesKeys.EVENING_NOTIF_HOUR] = preference.hour
                                prefs[AthkarPreferencesKeys.EVENING_NOTIF_MINUTE] = preference.minute
                            }
                            AthkarGroupType.POST_PRAYER -> { /* no notification for post-prayer */ }
                        }
                    }

                    // 2. Schedule or cancel alarm
                    val notifId = when (preference.groupType) {
                        AthkarGroupType.MORNING     -> AthkarNotificationIds.MORNING
                        AthkarGroupType.EVENING     -> AthkarNotificationIds.EVENING
                        AthkarGroupType.POST_PRAYER -> return@safeCall
                    }
                    if (preference.enabled) {
                        scheduler.scheduleDailyReminder(
                            notificationId = notifId,
                            hour = preference.hour,
                            minute = preference.minute,
                            title = title,
                            body = body,
                        )
                    } else {
                        scheduler.cancelReminder(notifId)
                    }
                },
                onError = { AthkarError.NotificationSchedulingError },
            )
        }
}
