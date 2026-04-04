package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.helmy2.mudawama.designsystem.MudawamaTheme

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

@Composable
fun HomePlaceholderScreen() {
    PlaceholderContent(label = "Home")
}

@Composable
fun PrayerPlaceholderScreen() {
    PlaceholderContent(label = "Prayer")
}

@Composable
fun AthkarPlaceholderScreen() {
    PlaceholderContent(label = "Athkar")
}

@Composable
fun HabitsPlaceholderScreen() {
    PlaceholderContent(label = "Habits")
}

@Preview
@Composable
fun HomePlaceholderScreenPreview() {
    HomePlaceholderScreen()
}

@Preview
@Composable
fun PrayerPlaceholderScreenPreview() {
    PrayerPlaceholderScreen()
}

@Preview
@Composable
fun AthkarPlaceholderScreenPreview() {
    AthkarPlaceholderScreen()
}

@Preview
@Composable
fun HabitsPlaceholderScreenPreview() {
    HabitsPlaceholderScreen()
}

