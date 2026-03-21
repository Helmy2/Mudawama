package io.github.helmy2.mudawama.core.presentation.permissions

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberPermissionState(
    permission: String,
    onResult: (PermissionState) -> Unit,
): () -> Unit {
    val activity = LocalActivity.current ?: return {}

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            onResult(PermissionState.Granted)
        } else {
            onResult(activity.getPermissionState(permission))
        }
    }

    return remember(permission) {
        {
            when (activity.getPermissionState(permission)) {
                PermissionState.Granted -> onResult(PermissionState.Granted)
                PermissionState.Rationale -> onResult(PermissionState.Rationale)
                else -> launcher.launch(permission)
            }
        }
    }
}
