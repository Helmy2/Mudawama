package io.github.helmy2.mudawama.prayer.presentation

import androidx.compose.runtime.Composable

/**
 * Returns a lambda that opens the platform's location settings screen.
 * On Android this fires [Settings.ACTION_LOCATION_SOURCE_SETTINGS].
 * On other platforms it is a no-op.
 */
@Composable
internal expect fun rememberOpenLocationSettings(): () -> Unit
