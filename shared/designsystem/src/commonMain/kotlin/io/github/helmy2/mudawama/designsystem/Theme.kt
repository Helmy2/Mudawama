package io.github.helmy2.mudawama.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.theme.getDynamicColorScheme

private val MudawamaTypography = Typography()

private val MudawamaShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

data class MudawamaSpacing(
    val compact: Dp = 8.dp,
    val default: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val spacious: Dp = 32.dp,
)

data class MudawamaElevation(
    val none: Dp = 0.dp,
    val subtle: Dp = 1.dp,
    val low: Dp = 2.dp,
    val medium: Dp = 4.dp,
    val high: Dp = 8.dp,
)

data class MudawamaSize(
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 20.dp,
    val iconLarge: Dp = 24.dp,
    val buttonMinHeight: Dp = 40.dp,
    val buttonMinHeightLarge: Dp = 48.dp,
)

data class MudawamaBorderRadius(
    val none: Dp = 0.dp,
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val large: Dp = 12.dp,
    val extraLarge: Dp = 16.dp,
    val full: Dp = 24.dp,
    val extraFull: Dp = 28.dp,
)

data class MudawamaMotion(
    val durationFast: Int = 150,
    val durationDefault: Int = 300,
    val durationSlow: Int = 500,
)

val LocalSpacing = staticCompositionLocalOf { MudawamaSpacing() }
val LocalElevation = staticCompositionLocalOf { MudawamaElevation() }
val LocalSize = staticCompositionLocalOf { MudawamaSize() }
val LocalBorderRadius = staticCompositionLocalOf { MudawamaBorderRadius() }
val LocalMotion = staticCompositionLocalOf { MudawamaMotion() }

val MaterialTheme.spacing: MudawamaSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

val MaterialTheme.elevation: MudawamaElevation
    @Composable
    @ReadOnlyComposable
    get() = LocalElevation.current

val MaterialTheme.size: MudawamaSize
    @Composable
    @ReadOnlyComposable
    get() = LocalSize.current

val MaterialTheme.borderRadius: MudawamaBorderRadius
    @Composable
    @ReadOnlyComposable
    get() = LocalBorderRadius.current

val MaterialTheme.motion: MudawamaMotion
    @Composable
    @ReadOnlyComposable
    get() = LocalMotion.current

@Composable
fun MudawamaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = if (useDynamicTheme) getDynamicColorScheme(darkTheme) else null
    val colorScheme = dynamicColorScheme ?: (if (darkTheme) darkScheme else lightScheme)

    CompositionLocalProvider(
        LocalSpacing provides MudawamaSpacing(),
        LocalElevation provides MudawamaElevation(),
        LocalSize provides MudawamaSize(),
        LocalBorderRadius provides MudawamaBorderRadius(),
        LocalMotion provides MudawamaMotion(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MudawamaTypography,
            shapes = MudawamaShapes,
            content = content
        )
    }
}
