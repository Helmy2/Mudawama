package io.github.helmy2.mudawama.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.helmy2.mudawama.designsystem.MudawamaTheme
import mudawama.shared.designsystem.Res
import mudawama.shared.designsystem.nav_tab_athkar
import mudawama.shared.designsystem.nav_tab_quran
import mudawama.shared.designsystem.settings_placeholder_coming_soon
import mudawama.shared.designsystem.settings_placeholder_title
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.settings_placeholder_title),
                        style = MudawamaTheme.typography.h3,
                        color = MudawamaTheme.colors.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MudawamaTheme.colors.onSurface,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.settings_placeholder_coming_soon),
                style = MudawamaTheme.typography.body1,
                color = MudawamaTheme.colors.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

@Preview @Composable fun SettingsScreenPreview() { SettingsScreen() }
