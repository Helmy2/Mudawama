package io.github.helmy2.mudawama.athkar.presentation.athkar

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
internal actual fun AthkarBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}
