package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import org.jetbrains.compose.resources.stringResource

private val BarHorizontalPadding = 16.dp
private val BarVerticalPadding = 8.dp
private val BarCornerRadius = 28.dp
private val GlassBlurRadius = 20.dp
private const val GlassAlpha = 0.80f

/**
 * Layered Box implementing the glassmorphism floating bar background (FR-009, FR-010).
 * All colors sourced from [MudawamaTheme.colors] — no hex literals (FR-014).
 */
@Composable
internal fun GlassmorphismSurface(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(BarCornerRadius))
                .background(MudawamaTheme.colors.surface.copy(alpha = GlassAlpha))
                .blur(radius = GlassBlurRadius)
        )
        content()
    }
}

/**
 * Floating glassmorphism bottom navigation bar.
 *
 * // FR-008: selectedItem is always derived by direct equality from currentRoute. No remember { mutableStateOf } anywhere in this file.
 *
 * @param currentRoute The currently active [NavKey] from the backstack — drives tab selection via
 *   direct equality; pass `null` to render all tabs as unselected (graceful fallback).
 * @param onNavigate Callback invoked when the user taps a tab item.
 */
@Composable
fun MudawamaBottomBar(
    currentRoute: NavKey?,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedItem: BottomNavItem? = BottomNavItem.entries.find { it.route == currentRoute }

    GlassmorphismSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = BarHorizontalPadding, vertical = BarVerticalPadding)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = MudawamaTheme.colors.onSurface,
        ) {
            BottomNavItem.entries.forEach { item ->
                NavigationBarItem(
                    selected = item == selectedItem,
                    onClick = { onNavigate(item.route) },
                    icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                    label = { Text(stringResource(item.labelRes)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MudawamaTheme.colors.primary,
                        selectedTextColor = MudawamaTheme.colors.primary,
                        unselectedIconColor = MudawamaTheme.colors.onSurface,
                        unselectedTextColor = MudawamaTheme.colors.onSurface,
                        indicatorColor = MudawamaTheme.colors.primary.copy(alpha = 0.12f),
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
fun MudawamaBottomBarPreview() {
    MudawamaTheme {
        MudawamaBottomBar(currentRoute = HomeRoute, onNavigate = {})
    }
}

