package io.github.helmy2.mudawama.athkar.presentation.athkar

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

private const val WELCOME_CHANNEL_ID = "mudawama_welcome"
private const val WELCOME_NOTIF_ID   = 9001

/**
 * On Android API 33+, requests [Manifest.permission.POST_NOTIFICATIONS] once on first
 * composition if not already granted. Posts a welcome notification when the user grants.
 * On API < 33 notifications are auto-granted — no dialog is shown and no welcome is sent.
 */
@Composable
internal actual fun RequestNotificationPermissionEffect() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            ensureWelcomeChannel(context)
            postWelcomeNotification(context)
        }
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // If already granted from a previous session: no welcome spam.
    }
}

private fun ensureWelcomeChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (manager.getNotificationChannel(WELCOME_CHANNEL_ID) != null) return
    manager.createNotificationChannel(
        NotificationChannel(
            WELCOME_CHANNEL_ID,
            "Welcome",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
    )
}

private fun postWelcomeNotification(context: Context) {
    val notification = NotificationCompat.Builder(context, WELCOME_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Notifications enabled")
        .setContentText("You'll receive daily Athkar reminders. May Allah accept your dhikr.")
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(WELCOME_NOTIF_ID, notification)
}
