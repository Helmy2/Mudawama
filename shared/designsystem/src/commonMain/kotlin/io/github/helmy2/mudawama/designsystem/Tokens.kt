package io.github.helmy2.mudawama.designsystem

import androidx.compose.ui.graphics.Color

// Re-export tokens and helpers
val Primary = DeepTeal
val PrimaryAccent = CalmEmerald
val Background = OffWhiteBackground
val Surface = PureWhiteSurface
val OnSurface = CharcoalText
val Error = MutedRedError

// Alpha helper for Color (commonMain) - extension function
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)

// Provide primaryDisabled helper (32% alpha as suggested)
fun primaryDisabled(): Color = Primary.copy(alpha = 0.32f)

