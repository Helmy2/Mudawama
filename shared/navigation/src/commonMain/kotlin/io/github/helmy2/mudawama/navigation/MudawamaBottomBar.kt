package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.helmy2.mudawama.designsystem.components.BottomBarItem
import io.github.helmy2.mudawama.designsystem.components.MudawamaBottomBar
import org.jetbrains.compose.resources.stringResource

private val BarHorizontalPadding = 16.dp
private val BarVerticalPadding = 8.dp

@Composable
fun MudawamaBottomBar(
    currentRoute: NavKey?,
    onNavigate: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedItem: BottomNavItem? = BottomNavItem.entries.find { it.route == currentRoute }
    val selectedIndex = selectedItem?.let { BottomNavItem.entries.indexOf(it) } ?: 0

    val items = BottomNavItem.entries.map { item ->
        BottomBarItem(
            icon = item.icon,
            label = stringResource(item.labelRes),
        )
    }

    MudawamaBottomBar(
        items = items,
        selectedIndex = selectedIndex,
        onSelect = { index ->
            val route = BottomNavItem.entries[index].route
            onNavigate(route)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = BarHorizontalPadding, vertical = BarVerticalPadding),
    )
}
