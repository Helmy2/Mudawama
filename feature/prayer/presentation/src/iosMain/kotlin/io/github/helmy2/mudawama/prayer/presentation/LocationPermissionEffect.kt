package io.github.helmy2.mudawama.prayer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction

/**
 * On iOS, [IosLocationProvider.getCurrentLocation] internally calls
 * [CLLocationManager.requestWhenInUseAuthorization] and suspends until the user responds.
 * The ViewModel's [resolveLocation] already handles this in [init].
 *
 * This effect fires [PrayerUiAction.LocationPermissionGranted] once on first composition
 * to ensure that if the user previously granted permission (subsequent launches), the
 * ViewModel immediately re-resolves with real coordinates instead of staying on the
 * Mecca fallback.
 */
@Composable
internal actual fun LocationPermissionEffect(onAction: (PrayerUiAction) -> Unit) {
    LaunchedEffect(Unit) {
        onAction(PrayerUiAction.LocationPermissionGranted)
    }
}
