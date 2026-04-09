package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.nav_tab_athkar
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

@Composable
fun QuranPlaceholderScreen() {
    PlaceholderContent(label = stringResource(Res.string.nav_tab_quran))
}

@Composable
fun AthkarPlaceholderScreen() {
    PlaceholderContent(label = stringResource(Res.string.nav_tab_athkar))
}

@Preview @Composable fun QuranPlaceholderScreenPreview() { QuranPlaceholderScreen() }
@Preview @Composable fun AthkarPlaceholderScreenPreview() { AthkarPlaceholderScreen() }
