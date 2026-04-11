package io.github.helmy2.mudawama.core.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * BroadcastReceiver that fires when an Athkar daily reminder alarm triggers.
 *
 * Posts the notification using the title and body stored in the Intent extras,
 * then re-schedules the next day's alarm via [AndroidNotificationScheduler].
 */
class AthkarNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val body  = intent.getStringExtra(EXTRA_BODY)  ?: return

        if (notificationId < 0) return

        ensureChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission revoked — ignore
        }

        // Re-schedule for the same time tomorrow so the reminder repeats daily.
        // We restore the same hour/minute by re-reading the Intent extras.
        // (The scheduler reads hour/minute from the pending intent stored in the alarm;
        //  for simplicity we pass 0/0 here and let the next save cycle correct it.)
        // A more robust approach would store hour/minute in extras too.
        val hour   = intent.getIntExtra(EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(EXTRA_MINUTE, -1)
        if (hour >= 0 && minute >= 0) {
            AndroidNotificationScheduler(context).scheduleDailyReminder(
                notificationId = notificationId,
                hour = hour,
                minute = minute,
                title = title,
                body = body,
            )
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Athkar Reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Daily morning and evening Athkar reminders"
            }
        )
    }

    companion object {
        const val CHANNEL_ID             = "athkar_reminders"
        const val EXTRA_NOTIFICATION_ID  = "notif_id"
        const val EXTRA_TITLE            = "notif_title"
        const val EXTRA_BODY             = "notif_body"
        const val EXTRA_HOUR             = "notif_hour"
        const val EXTRA_MINUTE           = "notif_minute"
    }
}
