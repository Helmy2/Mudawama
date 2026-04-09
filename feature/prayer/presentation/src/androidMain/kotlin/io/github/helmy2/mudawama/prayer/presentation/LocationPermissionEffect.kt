package io.github.helmy2.mudawama.prayer.presentation

import androidx.compose.runtime.Composable
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction

@Composable
internal actual fun LocationPermissionEffect(onAction: (PrayerUiAction) -> Unit) {
    RequestLocationPermissionEffect(onAction = onAction)
}
