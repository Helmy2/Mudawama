package io.github.helmy2.mudawama.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

expect val isDynamicColorSupported: Boolean

@Composable
expect fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme?
