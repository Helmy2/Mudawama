package io.github.helmy2.mudawama.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
internal actual fun AppBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}
