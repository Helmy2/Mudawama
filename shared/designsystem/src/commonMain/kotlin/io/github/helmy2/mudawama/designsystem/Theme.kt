package io.github.helmy2.mudawama.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Mudawama color tokens are declared in Colors.kt (same package) and reused here.

/**
 * Common token holders exposed via CompositionLocal for commonMain consumption.
 *
 * [surfaceVariant] — slightly elevated surface used for icon chips, secondary containers,
 * and any element that needs to stand out from [surface] without using [primary].
 */
data class MudawamaColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val error: Color,
)

val LightMudawamaColors = MudawamaColors(
    primary = DeepTeal,
    onPrimary = PureWhiteSurface,
    background = OffWhiteBackground,
    surface = PureWhiteSurface,
    surfaceVariant = OffWhiteBackground,   // same off-white family — chips sit naturally
    onSurface = CharcoalText,
    error = MutedRedError,
)

val DarkMudawamaColors = MudawamaColors(
    primary = EmeraldLight,                // lighter teal — legible on dark surfaces
    onPrimary = Color(0xFF003828),         // deep teal for text on the bright primary
    background = DarkBackground,           // 0xFF0F1412 — deepest layer
    surface = DarkSurface,                 // 0xFF1A2120 — cards & sheets
    surfaceVariant = DarkSurfaceVariant,   // 0xFF243130 — icon chips, secondary containers
    onSurface = DarkOnSurface,             // 0xFFE8F0EF — warm off-white text
    error = DarkError,                     // 0xFFFF6B6B — bright red legible on dark
)

internal val LocalMudawamaColors = staticCompositionLocalOf { LightMudawamaColors }
internal val LocalMudawamaTypography = staticCompositionLocalOf { MudawamaTypography() }
internal val LocalMudawamaShapes = staticCompositionLocalOf { MudawamaShapes() }


/**
 * Public accessor object for tokens.
 */
object MudawamaTheme {
    val colors: MudawamaColors
        @Composable get() = LocalMudawamaColors.current

    val typography: MudawamaTypography
        @Composable get() = LocalMudawamaTypography.current

    val shapes: MudawamaShapes
        @Composable get() = LocalMudawamaShapes.current
}

/**
 * Theme composable (Theme-first shell). Accepts optional overrides but defaults to system dark mode.
 * It wires tokens into Material 3's MaterialTheme so Material3 components render consistently.
 */
@Composable
fun MudawamaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorOverrides: MudawamaColors? = null,
    typographyOverrides: MudawamaTypography? = null,
    shapesOverrides: MudawamaShapes? = null,
    content: @Composable () -> Unit
) {
    val m3Colors = if (darkTheme) {
        darkColorScheme(
            primary = DarkMudawamaColors.primary,
            onPrimary = DarkMudawamaColors.onPrimary,
            background = DarkMudawamaColors.background,
            surface = DarkMudawamaColors.surface,
            surfaceVariant = DarkMudawamaColors.surfaceVariant,
            onSurface = DarkMudawamaColors.onSurface,
            onSurfaceVariant = DarkMudawamaColors.onSurface,
            error = DarkMudawamaColors.error,
            onError = DarkMudawamaColors.onPrimary,
        )
    } else {
        lightColorScheme(
            primary = LightMudawamaColors.primary,
            onPrimary = LightMudawamaColors.onPrimary,
            background = LightMudawamaColors.background,
            surface = LightMudawamaColors.surface,
            surfaceVariant = LightMudawamaColors.surfaceVariant,
            onSurface = LightMudawamaColors.onSurface,
            onSurfaceVariant = LightMudawamaColors.onSurface,
            error = LightMudawamaColors.error,
            onError = LightMudawamaColors.onPrimary,
        )
    }

    val appliedColors = colorOverrides ?: if (darkTheme) DarkMudawamaColors else LightMudawamaColors
    val appliedTypography = typographyOverrides ?: MudawamaTypography()
    val appliedShapes = shapesOverrides ?: MudawamaShapes()

    // Convert our shape tokens to Material Shapes
    val materialShapes = Shapes(
        small = RoundedCornerShape(appliedShapes.small),
        medium = RoundedCornerShape(appliedShapes.medium),
        large = RoundedCornerShape(appliedShapes.large)
    )

    // Provide locals and call MaterialTheme. For Material3 typography we pass default Typography();
    // components should read MudawamaTheme.typography for token styles.
    CompositionLocalProvider(
        LocalMudawamaColors provides appliedColors,
        LocalMudawamaTypography provides appliedTypography,
        LocalMudawamaShapes provides appliedShapes
    ) {
        MaterialTheme(
            colorScheme = m3Colors,
            typography = Typography(),
            shapes = materialShapes,
            content = content
        )
    }
}

