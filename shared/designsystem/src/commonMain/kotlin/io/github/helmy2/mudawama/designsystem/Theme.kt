package io.github.helmy2.mudawama.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.unit.dp

// Mudawama color tokens are declared in Colors.kt (same package) and reused here.

/**
 * Common token holders exposed via CompositionLocal for commonMain consumption.
 */
data class MudawamaColors(
    val primary: androidx.compose.ui.graphics.Color,
    val onPrimary: androidx.compose.ui.graphics.Color,
    val background: androidx.compose.ui.graphics.Color,
    val surface: androidx.compose.ui.graphics.Color,
    val onSurface: androidx.compose.ui.graphics.Color,
    val error: androidx.compose.ui.graphics.Color
)

val LightMudawamaColors = MudawamaColors(
    primary = DeepTeal,
    onPrimary = PureWhiteSurface,
    background = OffWhiteBackground,
    surface = PureWhiteSurface,
    onSurface = CharcoalText,
    error = MutedRedError
)

val DarkMudawamaColors = MudawamaColors(
    primary = CalmEmerald, // slightly lighter tint for dark mode
    onPrimary = PureWhiteSurface,
    background = androidx.compose.ui.graphics.Color(0xFF0B0E0E),
    surface = androidx.compose.ui.graphics.Color(0xFF121414),
    onSurface = OffWhiteBackground,
    error = MutedRedError
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
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = DarkMudawamaColors.primary,
            onPrimary = DarkMudawamaColors.onPrimary,
            background = DarkMudawamaColors.background,
            surface = DarkMudawamaColors.surface,
            onSurface = DarkMudawamaColors.onSurface,
            error = DarkMudawamaColors.error
        )
    } else {
        lightColorScheme(
            primary = LightMudawamaColors.primary,
            onPrimary = LightMudawamaColors.onPrimary,
            background = LightMudawamaColors.background,
            surface = LightMudawamaColors.surface,
            onSurface = LightMudawamaColors.onSurface,
            error = LightMudawamaColors.error
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
            colorScheme = colors,
            typography = Typography(),
            shapes = materialShapes,
            content = content
        )
    }
}


