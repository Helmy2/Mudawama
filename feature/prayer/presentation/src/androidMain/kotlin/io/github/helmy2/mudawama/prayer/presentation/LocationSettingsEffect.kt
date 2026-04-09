package io.github.helmy2.mudawama.prayer.presentation

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.runtime.LaunchedEffect
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction

/**
 * On Android:
 * - Intercepts [PrayerUiAction.OpenLocationSettings] by opening
 *   [Settings.ACTION_LOCATION_SOURCE_SETTINGS].
 * - When the user returns to the app (Activity RESUMED) while the location service
 *   is still flagged as disabled, fires [PrayerUiAction.LocationPermissionGranted]
 *   so the ViewModel re-resolves location.
 */
@Composable
internal actual fun LocationSettingsEffect(
    locationServiceDisabled: Boolean,
    onAction: (PrayerUiAction) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnAction by rememberUpdatedState(onAction)

    // When the user comes back from Settings (app resumes), re-check location.
    // Gated on locationServiceDisabled so we don't re-resolve on every resume.
    LaunchedEffect(locationServiceDisabled, lifecycleOwner) {
        if (locationServiceDisabled) {
            var isFirstResume = true
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (isFirstResume) {
                    // Skip the very first resume (the one that started this effect)
                    isFirstResume = false
                } else {
                    currentOnAction(PrayerUiAction.LocationPermissionGranted)
                }
            }
        }
    }
}

/**
 * Opens the Android Location Source Settings screen.
 * Called from [PrayerScreen] when the user taps the "location disabled" banner.
 */
internal fun openAndroidLocationSettings(context: android.content.Context) {
    context.startActivity(
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    )
}
