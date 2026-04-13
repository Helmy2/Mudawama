package io.github.helmy2.mudawama.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.borderRadius
import io.github.helmy2.mudawama.designsystem.elevation
import io.github.helmy2.mudawama.designsystem.size
import io.github.helmy2.mudawama.designsystem.spacing

data class BottomBarItem(
    val icon: ImageVector,
    val label: String,
)

@Composable
fun MudawamaBottomBar(
    items: List<BottomBarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.compact
            ),
        shape = RoundedCornerShape(MaterialTheme.borderRadius.extraFull),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = MaterialTheme.elevation.low,
        shadowElevation = MaterialTheme.elevation.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.compact,
                    vertical = MaterialTheme.spacing.compact
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                BottomBarTab(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onSelect(index) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomBarTab(
    item: BottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    if (isSelected) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(MaterialTheme.borderRadius.extraLarge))
                .background(primary)
                .clickable(onClick = onClick)
                .padding(
                    horizontal = MaterialTheme.spacing.default,
                    vertical = MaterialTheme.spacing.compact
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = onPrimary,
                modifier = Modifier.size(MaterialTheme.size.iconMedium),
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = onPrimary,
                ),
            )
        }
    } else {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(
                    horizontal = MaterialTheme.spacing.compact,
                    vertical = MaterialTheme.spacing.compact
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = onSurfaceVariant,
                modifier = Modifier.size(MaterialTheme.size.iconMedium),
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = onSurfaceVariant,
                ),
            )
        }
    }
}