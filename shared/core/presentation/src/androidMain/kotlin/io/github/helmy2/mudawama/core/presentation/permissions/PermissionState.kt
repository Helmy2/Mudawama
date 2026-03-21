package io.github.helmy2.mudawama.core.presentation.permissions

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

sealed interface PermissionState {
    data object Granted : PermissionState
    data object Denied : PermissionState
    data object Rationale : PermissionState
    data object PermanentlyDenied : PermissionState
}

fun Activity.getPermissionState(permission: String): PermissionState {
    val granted = ContextCompat.checkSelfPermission(
        this,
        permission,
    ) == PackageManager.PERMISSION_GRANTED

    if (granted) return PermissionState.Granted

    val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
        this,
        permission,
    )

    return if (shouldShowRationale) {
        PermissionState.Rationale
    } else {
        PermissionState.PermanentlyDenied
    }
}
