package io.github.helmy2.mudawama.designsystem

import androidx.compose.ui.graphics.Color

// ── Light-mode primitives ──────────────────────────────────────────────────────
val DeepTeal = Color(0xFF02594F)
val CalmEmerald = Color(0xFF1B8049)
val OffWhiteBackground = Color(0xFFF7F7F4)
val PureWhiteSurface = Color(0xFFFFFFFF)
val CharcoalText = Color(0xFF1D2322)
val MutedRedError = Color(0xFFC45151)

// ── Dark-mode primitives ───────────────────────────────────────────────────────
// Primary: a lighter, high-contrast teal that reads well on dark surfaces
val EmeraldLight = Color(0xFF4DB882)       // ~AA contrast on dark bg

// Backgrounds: true dark with enough separation between layers
val DarkBackground = Color(0xFF0F1412)     // deepest layer — page bg
val DarkSurface = Color(0xFF1A2120)        // cards / sheets — visibly lifted
val DarkSurfaceVariant = Color(0xFF243130) // icon chips, secondary containers

// Text: warm off-white so it doesn't feel clinical
val DarkOnSurface = Color(0xFFE8F0EF)

// Error: lighter red with enough luminance to be legible on dark
val DarkError = Color(0xFFFF6B6B)

