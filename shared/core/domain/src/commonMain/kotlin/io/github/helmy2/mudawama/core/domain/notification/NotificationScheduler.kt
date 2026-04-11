package io.github.helmy2.mudawama.core.domain.notification

/**
 * Platform-agnostic contract for scheduling and cancelling repeating daily notifications.
 *
 * Implemented natively on each platform:
 *  - Android: [AndroidNotificationScheduler] via AlarmManager.setExactAndAllowWhileIdle
 *  - iOS: [IosNotificationScheduler] via UNCalendarNotificationTrigger
 */
interface NotificationScheduler {
    /**
     * Schedule (or reschedule) a daily repeating notification.
     *
     * @param notificationId  Stable integer ID — must not change across app versions.
     * @param hour            Hour-of-day in 24-hour format (0–23).
     * @param minute          Minute (0–59).
     * @param title           Notification title text (already resolved from string resources).
     * @param body            Notification body text (already resolved from string resources).
     */
    fun scheduleDailyReminder(
        notificationId: Int,
        hour: Int,
        minute: Int,
        title: String,
        body: String,
    )

    /**
     * Cancel the notification with the given [notificationId].
     * No-op if no such notification is scheduled.
     */
    fun cancelReminder(notificationId: Int)
}
