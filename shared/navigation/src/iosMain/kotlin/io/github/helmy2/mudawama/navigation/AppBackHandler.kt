package io.github.helmy2.mudawama.navigation

import androidx.compose.runtime.Composable

/** iOS back navigation is handled by the host — nothing to do here. */
@Composable
internal actual fun AppBackHandler(onBack: () -> Unit) = Unit
