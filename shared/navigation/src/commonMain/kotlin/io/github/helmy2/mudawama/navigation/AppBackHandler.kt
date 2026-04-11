package io.github.helmy2.mudawama.navigation

import androidx.compose.runtime.Composable

/** Platform-specific intercept of the system back gesture / button. */
@Composable
internal expect fun AppBackHandler(onBack: () -> Unit)
