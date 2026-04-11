package io.github.helmy2.mudawama.athkar.presentation.athkar

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable effect that requests POST_NOTIFICATIONS permission
 * on first composition (Android API 33+). On other platforms this is a no-op.
 */
@Composable
internal expect fun RequestNotificationPermissionEffect()
