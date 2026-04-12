package io.github.helmy2.mudawama.feature.qibla.presentation.qibla

import androidx.compose.runtime.Composable
import io.github.helmy2.mudawama.feature.qibla.presentation.viewmodel.QiblaViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
expect fun QiblaScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: QiblaViewModel = koinViewModel()
)
