package io.github.helmy2.mudawama.athkar.presentation.athkar

import androidx.compose.runtime.Composable

/**
 * Platform-specific back-press handler.
 * On Android intercepts the system back button and calls [onBack].
 * On iOS this is a no-op — back navigation is handled by the SwiftUI/Compose host.
 */
@Composable
internal expect fun AthkarBackHandler(onBack: () -> Unit)
