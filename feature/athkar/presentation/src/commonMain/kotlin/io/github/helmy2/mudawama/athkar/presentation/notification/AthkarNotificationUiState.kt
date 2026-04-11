package io.github.helmy2.mudawama.athkar.presentation.notification

import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.model.NotificationPreference

data class AthkarNotificationUiState(
    val morningPreference: NotificationPreference = NotificationPreference(
        groupType = AthkarGroupType.MORNING,
        enabled = false,
        hour = 6,
        minute = 0,
    ),
    val eveningPreference: NotificationPreference = NotificationPreference(
        groupType = AthkarGroupType.EVENING,
        enabled = false,
        hour = 18,
        minute = 0,
    ),
    val isLoading: Boolean = false,
)

sealed interface AthkarNotificationUiAction {
    /** Toggle the morning reminder on or off. [title]/[body] are already-resolved strings. */
    data class ToggleMorning(val enabled: Boolean, val title: String, val body: String) : AthkarNotificationUiAction
    /** Toggle the evening reminder on or off. [title]/[body] are already-resolved strings. */
    data class ToggleEvening(val enabled: Boolean, val title: String, val body: String) : AthkarNotificationUiAction
    /** Update morning reminder time. [title]/[body] are already-resolved strings. */
    data class SetMorningTime(val hour: Int, val minute: Int, val title: String, val body: String) : AthkarNotificationUiAction
    /** Update evening reminder time. [title]/[body] are already-resolved strings. */
    data class SetEveningTime(val hour: Int, val minute: Int, val title: String, val body: String) : AthkarNotificationUiAction
}

sealed interface AthkarNotificationUiEvent {
    data object PermissionDenied : AthkarNotificationUiEvent
    data object SaveError : AthkarNotificationUiEvent
}
