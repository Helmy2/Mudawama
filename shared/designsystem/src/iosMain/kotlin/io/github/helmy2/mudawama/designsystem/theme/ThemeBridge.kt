package io.github.helmy2.mudawama.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

actual val isDynamicColorSupported: Boolean = false

@Composable
actual fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    return null
}
