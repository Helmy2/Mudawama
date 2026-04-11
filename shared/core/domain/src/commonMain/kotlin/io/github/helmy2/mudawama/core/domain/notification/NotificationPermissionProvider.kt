package io.github.helmy2.mudawama.core.domain.notification

/**
 * Platform-agnostic contract for requesting notification permission and
 * scheduling a one-shot "welcome" notification when the user first grants it.
 *
 * Implemented natively on each platform:
 *  - Android: handled directly in the presentation layer via ActivityResultContracts.
 *  - iOS: [IosNotificationProvider] using UNUserNotificationCenter.
 */
interface NotificationPermissionProvider {
    /**
     * Requests the OS notification permission (if not already granted).
     * If the user grants permission, schedules a welcome notification immediately after.
     */
    suspend fun requestPermissionAndNotifyIfGranted()
}
