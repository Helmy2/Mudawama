package io.github.helmy2.mudawama.core.domain.notification

/**
 * Platform-agnostic contract for checking and requesting notification permission.
 *
 * Implemented natively on each platform:
 *  - Android: [AndroidNotificationPermissionChecker] via ContextCompat.checkSelfPermission
 *  - iOS: [IosNotificationPermissionChecker] via UNUserNotificationCenter
 */
interface NotificationPermissionChecker {
    /** Returns true if the app currently has permission to post notifications. */
    fun hasPermission(): Boolean

    /**
     * Requests the OS notification permission if not already granted.
     * @return [NotificationPermissionResult.Granted] or [NotificationPermissionResult.Denied].
     */
    suspend fun requestPermission(): NotificationPermissionResult
}

sealed interface NotificationPermissionResult {
    data object Granted : NotificationPermissionResult
    data object Denied  : NotificationPermissionResult
}
