package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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

// Active pill shape: rounded-square (12dp corners) matching the reference
private val ActivePillShape = RoundedCornerShape(16.dp)

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
 * Active tab renders as a deep-teal rounded-square pill with white icon + label,
 * matching the design reference (home_dashboard.png, daily_prayer_tracker.png).
 *
 * // FR-008: selectedItem is always derived by direct equality from currentRoute. No remember { mutableStateOf } anywhere in this file.
 *
 * @param currentRoute The currently active [NavKey] from the backstack.
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavItem.entries.forEach { item ->
                val isSelected = item == selectedItem
                BottomBarTab(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onNavigate(item.route) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomBarTab(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val label = stringResource(item.labelRes)
    val primary = MudawamaTheme.colors.primary
    val onPrimary = MudawamaTheme.colors.onPrimary
    val onSurface = MudawamaTheme.colors.onSurface

    if (isSelected) {
        // Active: teal rounded-square pill
        Column(
            modifier = Modifier
                .clip(ActivePillShape)
                .background(primary)
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = label,
                tint = onPrimary,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = onPrimary,
                ),
            )
        }
    } else {
        // Inactive: transparent, muted icon + label
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = label,
                tint = onSurface.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = onSurface.copy(alpha = 0.55f),
                ),
            )
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
