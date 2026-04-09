package io.github.helmy2.mudawama.prayer.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import io.github.helmy2.mudawama.prayer.presentation.model.PrayerUiAction

/**
 * Android-side composable side-effect that requests location permissions once on first
 * composition (if not already granted) and fires [PrayerUiAction.LocationPermissionGranted]
 * when the user grants them.
 *
 * Wire this into [PrayerScreen] from [androidMain] only.
 */
@Composable
internal fun RequestLocationPermissionEffect(onAction: (PrayerUiAction) -> Unit) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            onAction(PrayerUiAction.LocationPermissionGranted)
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        // If already granted on entry, notify the ViewModel so it can use real location
        if (fineGranted || coarseGranted) {
            onAction(PrayerUiAction.LocationPermissionGranted)
        }
    }
}
