package io.github.helmy2.mudawama.core.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionChecker
import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionResult

/**
 * Android implementation of [NotificationPermissionChecker].
 *
 * Note: On Android, actually *requesting* the permission must be done from the UI layer
 * (via [ActivityResultContracts.RequestPermission]) because it requires an Activity context.
 * The [RequestNotificationPermissionEffect] composable in the presentation layer handles
 * the request flow; this checker is used purely for the [hasPermission] query.
 *
 * [requestPermission] returns [NotificationPermissionResult.Granted] if already granted,
 * [NotificationPermissionResult.Denied] otherwise (UI-driven request not possible here).
 */
class AndroidNotificationPermissionChecker(
    private val context: Context,
) : NotificationPermissionChecker {

    override fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermission(): NotificationPermissionResult {
        return if (hasPermission()) NotificationPermissionResult.Granted
        else NotificationPermissionResult.Denied
    }
}
