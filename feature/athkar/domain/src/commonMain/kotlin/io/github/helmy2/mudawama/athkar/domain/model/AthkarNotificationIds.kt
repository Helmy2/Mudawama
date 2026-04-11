package io.github.helmy2.mudawama.athkar.domain.model

/**
 * Stable notification IDs for Athkar reminders.
 * These IDs are passed to [NotificationScheduler] and must remain constant across app versions.
 */
object AthkarNotificationIds {
    const val MORNING = 1001
    const val EVENING = 1002
}
