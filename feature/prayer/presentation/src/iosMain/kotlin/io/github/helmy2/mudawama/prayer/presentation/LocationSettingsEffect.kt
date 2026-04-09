package io.github.helmy2.mudawama.prayer.presentation

import androidx.compose.runtime.Composable
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction

@Composable
internal actual fun LocationSettingsEffect(
    locationServiceDisabled: Boolean,
    onAction: (PrayerUiAction) -> Unit
) {
    // iOS location services are toggled in the OS Settings app; no in-app handling needed.
}
