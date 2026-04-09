package io.github.helmy2.mudawama.prayer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberOpenLocationSettings(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { openAndroidLocationSettings(context) }
    }
}
