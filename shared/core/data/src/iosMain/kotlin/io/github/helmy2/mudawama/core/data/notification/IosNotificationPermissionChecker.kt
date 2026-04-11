package io.github.helmy2.mudawama.core.data.notification

import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionChecker
import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionResult
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * iOS implementation of [NotificationPermissionChecker].
 *
 * Checks and requests [UNUserNotificationCenter] authorization.
 */
class IosNotificationPermissionChecker : NotificationPermissionChecker {

    override fun hasPermission(): Boolean {
        // Synchronous check is not directly available on iOS; we return false as a conservative
        // default. Callers should prefer [requestPermission] for accurate status.
        return false
    }

    override suspend fun requestPermission(): NotificationPermissionResult =
        suspendCancellableCoroutine { cont ->
            UNUserNotificationCenter.currentNotificationCenter()
                .requestAuthorizationWithOptions(
                    options = UNAuthorizationOptionAlert or
                            UNAuthorizationOptionSound or
                            UNAuthorizationOptionBadge,
                ) { granted, _ ->
                    val result = if (granted) NotificationPermissionResult.Granted
                    else NotificationPermissionResult.Denied
                    cont.resume(result)
                }
        }
}
