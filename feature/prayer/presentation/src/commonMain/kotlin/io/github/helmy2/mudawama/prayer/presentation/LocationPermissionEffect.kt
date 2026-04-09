package io.github.helmy2.mudawama.prayer.presentation

import androidx.compose.runtime.Composable
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction

/**
 * Platform-specific hook that wires location-permission requests into the prayer screen.
 * On Android, this launches the system permission dialog.
 * On other platforms it is a no-op.
 */
@Composable
internal expect fun LocationPermissionEffect(onAction: (PrayerUiAction) -> Unit)
