package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.home_daily_habits_button
import mudawama.shared.designsystem.home_daily_habits_subtitle
import mudawama.shared.designsystem.nav_tab_athkar
import mudawama.shared.designsystem.nav_tab_home
import mudawama.shared.designsystem.nav_tab_prayers
import mudawama.shared.designsystem.nav_tab_quran
import org.jetbrains.compose.resources.stringResource

@Composable
private fun PlaceholderContent(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MudawamaTheme.typography.h1,
            color = MudawamaTheme.colors.onSurface
        )
    }
}

/**
 * Temporary Home placeholder.
 * Renders a tappable "Daily Habits" card so HabitsRoute is reachable while
 * the real Home screen is not yet implemented.
 */
@Composable
fun HomePlaceholderScreen(
    onNavigateToHabits: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.nav_tab_home),
            style = MudawamaTheme.typography.h1,
            color = MudawamaTheme.colors.onSurface,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onNavigateToHabits() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MudawamaTheme.colors.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MudawamaTheme.colors.primary,
                    modifier = Modifier.size(28.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.home_daily_habits_button),
                        style = MudawamaTheme.typography.h4,
                        color = MudawamaTheme.colors.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.home_daily_habits_subtitle),
                        style = MudawamaTheme.typography.body2,
                        color = MudawamaTheme.colors.onSurface.copy(alpha = 0.5f),
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MudawamaTheme.colors.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun PrayerPlaceholderScreen() {
    PlaceholderContent(label = stringResource(Res.string.nav_tab_prayers))
}

@Composable
fun QuranPlaceholderScreen() {
    PlaceholderContent(label = stringResource(Res.string.nav_tab_quran))
}

@Composable
fun AthkarPlaceholderScreen() {
    PlaceholderContent(label = stringResource(Res.string.nav_tab_athkar))
}

@Composable
fun HabitsPlaceholderScreen() {
    PlaceholderContent(label = stringResource(Res.string.home_daily_habits_button))
}

@Preview @Composable fun HomePlaceholderScreenPreview() { HomePlaceholderScreen(onNavigateToHabits = {}) }
@Preview @Composable fun PrayerPlaceholderScreenPreview() { PrayerPlaceholderScreen() }
@Preview @Composable fun QuranPlaceholderScreenPreview() { QuranPlaceholderScreen() }
@Preview @Composable fun AthkarPlaceholderScreenPreview() { AthkarPlaceholderScreen() }
@Preview @Composable fun HabitsPlaceholderScreenPreview() { HabitsPlaceholderScreen() }
