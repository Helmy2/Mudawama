package io.github.helmy2.mudawama.core.data.notification

import io.github.helmy2.mudawama.core.domain.notification.NotificationScheduler
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS implementation of [NotificationScheduler].
 *
 * Uses [UNCalendarNotificationTrigger] with `repeats = true` to fire a notification
 * daily at the requested hour and minute.
 */
class IosNotificationScheduler : NotificationScheduler {

    override fun scheduleDailyReminder(
        notificationId: Int,
        hour: Int,
        minute: Int,
        title: String,
        body: String,
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
        }

        val components = NSDateComponents().apply {
            this.hour = hour.toLong()
            this.minute = minute.toLong()
            this.second = 0
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = components,
            repeats = true,
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notificationId.toString(),
            content = content,
            trigger = trigger,
        )

        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request, withCompletionHandler = null)
    }

    override fun cancelReminder(notificationId: Int) {
        UNUserNotificationCenter.currentNotificationCenter()
            .removePendingNotificationRequestsWithIdentifiers(listOf(notificationId.toString()))
    }
}
