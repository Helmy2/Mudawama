package io.github.helmy2.mudawama.prayer.presentation

import androidx.compose.runtime.Composable
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction

/**
 * Platform-specific side-effect composable that opens the device's Location Settings
 * when [PrayerUiAction.OpenLocationSettings] is dispatched, and re-triggers location
 * resolution when the user returns to the app.
 *
 * On Android this fires an [android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS] intent.
 * On other platforms it is a no-op.
 */
@Composable
internal expect fun LocationSettingsEffect(
    locationServiceDisabled: Boolean,
    onAction: (PrayerUiAction) -> Unit
)
