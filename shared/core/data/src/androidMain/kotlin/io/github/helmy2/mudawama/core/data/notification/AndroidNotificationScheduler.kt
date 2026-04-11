package io.github.helmy2.mudawama.core.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import io.github.helmy2.mudawama.core.domain.notification.NotificationScheduler
import java.util.Calendar

/**
 * Android implementation of [NotificationScheduler].
 *
 * Uses [AlarmManager.setExactAndAllowWhileIdle] to fire a daily repeating reminder.
 * On each alarm fire the BroadcastReceiver re-schedules the next day's alarm,
 * achieving a "daily repeat" without relying on [AlarmManager.setRepeating]
 * (which is inexact on API 19+).
 */
class AndroidNotificationScheduler(
    private val context: Context,
) : NotificationScheduler {

    override fun scheduleDailyReminder(
        notificationId: Int,
        hour: Int,
        minute: Int,
        title: String,
        body: String,
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = buildPendingIntent(notificationId, title, body, hour, minute)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the calculated time is in the past today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent,
            )
        }
    }

    override fun cancelReminder(notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AthkarNotificationReceiver::class.java)
        val flags = PendingIntent.FLAG_NO_CREATE or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, flags)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun buildPendingIntent(notificationId: Int, title: String, body: String, hour: Int = 0, minute: Int = 0): PendingIntent {
        val intent = Intent(context, AthkarNotificationReceiver::class.java).apply {
            putExtra(AthkarNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(AthkarNotificationReceiver.EXTRA_TITLE, title)
            putExtra(AthkarNotificationReceiver.EXTRA_BODY, body)
            putExtra(AthkarNotificationReceiver.EXTRA_HOUR, hour)
            putExtra(AthkarNotificationReceiver.EXTRA_MINUTE, minute)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getBroadcast(context, notificationId, intent, flags)
    }
}
