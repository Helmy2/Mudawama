package io.github.helmy2.mudawama.athkar.presentation.athkar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionProvider
import org.koin.compose.koinInject

/**
 * On iOS, delegates to [NotificationPermissionProvider] (backed by [IosNotificationProvider])
 * which requests UNUserNotificationCenter authorisation and fires a welcome notification
 * if the user grants permission.
 */
@Composable
internal actual fun RequestNotificationPermissionEffect() {
    val provider = koinInject<NotificationPermissionProvider>()
    LaunchedEffect(Unit) {
        provider.requestPermissionAndNotifyIfGranted()
    }
}
